package org.sidre.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sidre.domain.BackendConfig;
import org.sidre.domain.BackendMetadata;
import org.sidre.domain.OembedInfo;
import org.sidre.repository.MetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHitsIterator;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@PropertySource(value = "file:${envConfigDir:envConf/default/}search_index.properties")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MetadataServiceImpl implements MetadataService {

  private final @NonNull MetadataRepository metadataRepository;
  private final @NonNull PublicMetadataIndexService publicMetadataIndexService;
  private final @NonNull MetadataCustomProcessor metadataCustomProcessor;
  private final @NonNull MetadataAutoUpdater metadataAutoUpdater;
  private final @NonNull ElasticsearchOperations elasticsearchOperations;
  private final @NonNull MetadataValidator metadataValidator;
  private final @NonNull MetadataFieldService metadataFieldService;
  private final @NonNull MetadataEnrichmentService metadataEnrichmentService;
  private final @NonNull ConfigService configService;

  @Value("${feature.add_metadata_enrichments}")
  private boolean featureAddMetadataEnrichments;

  @Transactional
  @Override
  public MetadataUpdateResult createOrUpdate(BackendMetadata metadata) {
    return createOrUpdate(List.of(metadata)).get(0);
  }

  @Transactional
  @Override
  public List<MetadataUpdateResult> createOrUpdate(final List<BackendMetadata> records) {
    List<MetadataUpdateResult> results = new ArrayList<>();
    for (BackendMetadata metadata : records) {
      MetadataUpdateResult result = new MetadataUpdateResult(metadata);
      if (isBlackListed(metadata)) {
        log.debug("Blacklisted resource: {}", metadata);
        result.setSuccess(false);
        result.setMessages(List.of("Blacklisted resource"));
        results.add(result);
        continue;
      }
      BackendMetadata existingMetadata = findMatchingMetadata(metadata);
      if (existingMetadata != null) {
        metadataFieldService.updateMetadataSource(metadata.getData(), mergeMetadataSources(existingMetadata, metadata));
      }

      metadataAutoUpdater.initAutoUpdateInfo(metadata);
      metadataCustomProcessor.process(metadata);
      metadataAutoUpdater.addMissingInfos(metadata);
      metadataCustomProcessor.postProcess(metadata);
      if (featureAddMetadataEnrichments) {
        metadataEnrichmentService.addMetadataEnrichments(metadata);
      }

      ValidatorResult validatorResult = metadataValidator.validate(metadata);
      if (!validatorResult.isValid()) {
        log.debug("invalid data: {}, violations: {}", metadata, validatorResult.getViolations());
        result.setSuccess(false);
        result.addMessages(validatorResult.getViolations());
      } else {
        OembedInfo oembedInfo = metadataAutoUpdater.initOembedInfo(metadata);
        oembedInfo = metadataCustomProcessor.processOembedInfo(oembedInfo, metadata);
        metadata.setOembedInfo(oembedInfo);

        metadata.setDateModified(LocalDateTime.now());
      }

      results.add(result);
    }
    List<BackendMetadata> dataToUpdate = results.stream().filter(MetadataUpdateResult::getSuccess).map(MetadataUpdateResult::getMetadata).toList();
    if (!dataToUpdate.isEmpty()) {
      metadataRepository.saveAll(dataToUpdate);
      publicMetadataIndexService.updatePublicIndices(dataToUpdate);
    }
    return results;
  }

  private boolean isBlackListed(BackendMetadata metadata) {
    BackendConfig config = configService.getMetadataConfig();
    List<String> blacklist = config == null ? null : config.getMetadataBlacklist();
    return blacklist != null && blacklist.stream().anyMatch(blacklistItem -> blacklistItem.equals(metadata.getId()));
  }

  @Transactional
  @Override
  public void persist(List<BackendMetadata> metadata) {
    List<BackendMetadata> filteredMetadata = metadata.stream().filter(m -> !isBlackListed(m)).toList();
    if (!filteredMetadata.isEmpty()) {
      filteredMetadata.forEach(m -> m.setDateModified(LocalDateTime.now()));
      metadataRepository.saveAll(filteredMetadata);
      publicMetadataIndexService.updatePublicIndices(filteredMetadata);
    }
  }

  @Transactional(readOnly = true)
  @Override
  public BackendMetadata findById(String id) {
    if (id == null) {
      return null;
    }
    return metadataRepository.findById(id).orElse(null);
  }


  /**
   * Merge existing MetadataSource list and new MetadataSource list. Entries in new list will override existing ones (based on
   * identifier).
   *
   * @param existingMetadata existing metadata which contains MetadataSource
   * @param newMetadata      new metadata which contains MetadataSource
   * @return merged MetadataSource list
   */
  private List<MetadataFieldService.MetadataSourceItem> mergeMetadataSources(final BackendMetadata existingMetadata, final BackendMetadata newMetadata) {
    List<MetadataFieldService.MetadataSourceItem> existingMetadataSources = metadataFieldService.getMetadataSourceItems(existingMetadata.getData());
    List<MetadataFieldService.MetadataSourceItem> newMetadataSources = metadataFieldService.getMetadataSourceItems(newMetadata.getData());
    if (existingMetadataSources == null) {
      return newMetadataSources;
    } else if (newMetadataSources == null) {
      return existingMetadataSources;
    }
    Set<Object> newIds = newMetadataSources.stream()
      .map(metadataFieldService::getIdentifier)
      .filter(m -> !StringUtils.isEmpty(m))
      .collect(Collectors.toSet());
    List<MetadataFieldService.MetadataSourceItem> keepEntries = existingMetadataSources.stream()
      .filter(m -> !newIds.contains(metadataFieldService.getIdentifier(m)))
      .toList();
    return Stream.concat(newMetadataSources.stream(), keepEntries.stream()).toList();
  }

  /**
   * Find an existing {@link BackendMetadata} that matches the given {@link BackendMetadata}.
   *
   * @param metadata existing data has to match this data
   * @return existing data or null, if not existing
   */
  private BackendMetadata findMatchingMetadata(final BackendMetadata metadata) {
    return metadataRepository.findById(metadata.getId()).orElse(null);
  }

  @Override
  public void delete(BackendMetadata metadata, boolean updatePublicIndices) {
    log.debug("delete metadata with identifier {}", metadata.getId());
    delete(List.of(metadata), updatePublicIndices);
  }

  private void delete(final List<BackendMetadata> metadata, boolean updatePublicIndices) {
    metadataRepository.deleteAll(metadata);
    if (updatePublicIndices) {
      publicMetadataIndexService.delete(metadata);
    }
  }

  @Transactional
  @Override
  public void deleteAll(boolean updatePublicIndices) {
    log.info("delete all metadata");
    Document mapping = metadataFieldService.getBackendMetadataMapping();
    IndexOperations indexOperations = elasticsearchOperations.indexOps(BackendMetadata.class);
    indexOperations.delete();
    indexOperations.create(indexOperations.createSettings(), mapping);
    indexOperations.refresh();
    if (updatePublicIndices) {
      publicMetadataIndexService.deleteAll();
    }
  }

  @Transactional
  @Override
  public boolean deleteSourceEntriesByNamedQuery(String queryName, String queryParam, boolean updatePublicIndices) {
    String queryField = metadataFieldService.getNamedMetadataSourceQueryField(queryName);
    return deleteSourceEntries(queryField, queryParam, updatePublicIndices);
  }

  @Transactional
  @Override
  public boolean deleteSourceEntryByIdentifier(String sourceInfoIdentifier, boolean updatePublicIndices) {
    String identifierField = metadataFieldService.getMetadataSourceIdentifierField();
    return deleteSourceEntries(identifierField, sourceInfoIdentifier, updatePublicIndices);
  }

  private boolean deleteSourceEntries(String queryField, String queryValue, boolean updatePublicIndices) {
    log.debug("delete source entries in metadata for params {}, {}", queryField, queryValue);
    final int pageSize = 100;
    if (StringUtils.isEmpty(queryField)) {
      log.debug("query field is empty");
      return false;
    }
    Query searchQuery = NativeQuery.builder()
            .withQuery(q -> q.match(m -> m.field(BackendMetadata.mapToElasticsearchPath(queryField)).query(queryValue)))
            .withPageable(PageRequest.of(0, pageSize))
            .build();
    try (SearchHitsIterator<BackendMetadata> stream = elasticsearchOperations.searchForStream(searchQuery, BackendMetadata.class)) {
      if (!stream.hasNext()) {
        return false;
      }
      while (stream.hasNext()) {
        BackendMetadata data = stream.next().getContent();
        List<MetadataFieldService.MetadataSourceItem> metadataSourceItems = metadataFieldService.getMetadataSourceItems(data.getData());
        if (metadataSourceItems.isEmpty()) {
          continue;
        }
        metadataSourceItems = metadataSourceItems.stream().filter(e -> !metadataFieldService.getValues(e, queryField).contains(queryValue)).toList();
        if (metadataSourceItems.isEmpty()) {
          delete(data, updatePublicIndices);
        } else {
          updateMetadataSourceItems(data, metadataSourceItems);
          metadataRepository.save(data);
          if (updatePublicIndices) {
            publicMetadataIndexService.updatePublicIndices(List.of(data));
          }
        }
      }
    }
    return true;
  }

  private void updateMetadataSourceItems(BackendMetadata metadata, List<MetadataFieldService.MetadataSourceItem> metadataSourceItems) {
    metadataFieldService.updateMetadataSource(metadata.getData(), metadataSourceItems);
    metadataFieldService.updateMetadataSource(metadata.getExtendedData(), metadataSourceItems);
    metadata.setDateModified(LocalDateTime.now());
  }

  @Transactional
  @Override
  public void initIndexMapping() {
    Document mapping = metadataFieldService.getBackendMetadataMapping();
    IndexOperations indexOperations = elasticsearchOperations.indexOps(BackendMetadata.class);
    indexOperations.putMapping(mapping);
    indexOperations.refresh();
  }

}
