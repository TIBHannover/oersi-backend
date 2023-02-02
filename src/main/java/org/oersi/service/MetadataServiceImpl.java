package org.oersi.service;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.oersi.domain.BackendMetadata;
import org.oersi.domain.OembedInfo;
import org.oersi.repository.EsMetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHitsIterator;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@PropertySource(value = "file:${envConfigDir:envConf/default/}oersi.properties")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MetadataServiceImpl implements MetadataService {

  private static final String METADATA_PROPERTY_NAME_MAIN_ENTITY_OF_PAGE = "mainEntityOfPage";

  private final @NonNull EsMetadataRepository metadataRepository;
  private final @NonNull PublicMetadataIndexService publicMetadataIndexService;
  private final @NonNull MetadataCustomProcessor metadataCustomProcessor;
  private final @NonNull MetadataAutoUpdater metadataAutoUpdater;
  private final @NonNull ElasticsearchOperations elasticsearchOperations;

  @Value("classpath:backend-index-mapping.json")
  private Resource indexMapping;

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

      // TODO
//      ValidatorResult validatorResult = new MetadataValidator(metadata).validate();
//      if (!validatorResult.isValid()) {
//        log.debug("invalid data: {}, violations: {}", metadata, validatorResult.getViolations());
//        result.setSuccess(false);
//        result.addMessages(validatorResult.getViolations());
//        results.add(result);
//        continue;
//      }
      metadataAutoUpdater.initAutoUpdateInfo(metadata);
      metadataCustomProcessor.process(metadata);

      OembedInfo oembedInfo = metadataAutoUpdater.initOembedInfo(metadata);
      oembedInfo = metadataCustomProcessor.processOembedInfo(oembedInfo, metadata);
      metadata.setOembedInfo(oembedInfo);

      BackendMetadata existingMetadata = findMatchingMetadata(metadata);
      if (existingMetadata != null) {
        log.debug("existing data: {}", existingMetadata);
        metadata.getData().put(METADATA_PROPERTY_NAME_MAIN_ENTITY_OF_PAGE, mergeMainEntityOfPageList(existingMetadata, metadata));
      }
      metadata.setDateModified(LocalDateTime.now());

      results.add(result);
    }
    List<BackendMetadata> dataToUpdate = results.stream().filter(MetadataUpdateResult::getSuccess).map(MetadataUpdateResult::getMetadata).collect(Collectors.toList());
    if (!dataToUpdate.isEmpty()) {
      metadataRepository.saveAll(dataToUpdate);
      publicMetadataIndexService.updatePublicIndices(dataToUpdate);
    }
    return results;
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
   * Merge existing mainEntityOfPage list and new mainEntityOfPage list. Entries in new list will override existing ones (based on
   * identifier).
   *
   * @param existingMetadata existing metadata which contains mainEntityOfPage list
   * @param newMetadata      new metadata which contains mainEntityOfPage list
   * @return merged mainEntityOfPage list
   */
  private List<Map<String, Object>> mergeMainEntityOfPageList(final BackendMetadata existingMetadata, final BackendMetadata newMetadata) {
    List<Map<String, Object>> existingMainEntityOfPage = MetadataHelper.parseList(existingMetadata.getData(), METADATA_PROPERTY_NAME_MAIN_ENTITY_OF_PAGE, new TypeReference<>() {});
    List<Map<String, Object>> newMainEntityOfPage = MetadataHelper.parseList(newMetadata.getData(), METADATA_PROPERTY_NAME_MAIN_ENTITY_OF_PAGE, new TypeReference<>() {});
    if (existingMainEntityOfPage == null) {
      return newMainEntityOfPage;
    } else if (newMainEntityOfPage == null) {
      return existingMainEntityOfPage;
    }
    Set<Object> newIds = newMainEntityOfPage.stream()
      .map(m -> m.get("id"))
      .collect(Collectors.toSet());
    List<Map<String, Object>> keepEntries = existingMainEntityOfPage.stream()
      .filter(m -> !newIds.contains(m.get("id")))
      .collect(Collectors.toList());
    return Stream.concat(newMainEntityOfPage.stream(), keepEntries.stream()).collect(Collectors.toList());
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

  @Transactional(readOnly = true)
  @Override
  public List<BackendMetadata> findByMainEntityOfPageId(final String mainEntityOfPageId) {
    return metadataRepository.findByMainEntityOfPageId(mainEntityOfPageId);
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
    IndexOperations indexOperations = elasticsearchOperations.indexOps(BackendMetadata.class);
    indexOperations.delete();
    indexOperations.create();
    initIndexMapping();
    if (updatePublicIndices) {
      publicMetadataIndexService.deleteAll();
    }
  }

  @Transactional
  @Override
  public void deleteMainEntityOfPageByProviderName(String providerName, boolean updatePublicIndices) {
    log.info("delete mainEntityOfPage in metadata for provider {}", providerName);
    final int pageSize = 100;
    Query searchQuery = NativeQuery.builder()
      .withQuery(q -> q.match(m -> m.field("data.mainEntityOfPage.provider.name").query(providerName)))
      .withPageable(PageRequest.of(0, pageSize))
      .build();
    SearchHitsIterator<BackendMetadata> stream = elasticsearchOperations.searchForStream(searchQuery, BackendMetadata.class);
    while (stream.hasNext()) {
      BackendMetadata data = stream.next().getContent();
      List<Map<String, Object>> mainEntityOfPage = MetadataHelper.parseList(data.getData(), METADATA_PROPERTY_NAME_MAIN_ENTITY_OF_PAGE, new TypeReference<>() {});
      mainEntityOfPage.removeIf(m -> {
        Map<String, Object> provider = MetadataHelper.parse(m, "provider", new TypeReference<>() {});
        return provider != null && providerName.equals(provider.get("name"));
      });
      if (mainEntityOfPage.isEmpty()) {
        delete(data, updatePublicIndices);
      } else {
        updateMainEntityOfPage(data, mainEntityOfPage);
        metadataRepository.save(data);
        publicMetadataIndexService.updatePublicIndices(List.of(data));
      }
    }
    stream.close();
  }

  @Transactional
  @Override
  public boolean deleteMainEntityOfPageByIdentifier(String mainEntityOfPageId, boolean updatePublicIndices) {
    List<BackendMetadata> metadata = findByMainEntityOfPageId(mainEntityOfPageId);
    if (metadata.isEmpty()) {
      return false;
    }
    List<BackendMetadata> metadataToDelete = new ArrayList<>();
    metadata.forEach(data -> {
      List<Map<String, Object>> mainEntityOfPage = MetadataHelper.parseList(data.getData(), METADATA_PROPERTY_NAME_MAIN_ENTITY_OF_PAGE, new TypeReference<>() {});
      mainEntityOfPage.removeIf(m -> m.get("id").equals(mainEntityOfPageId));
      if (mainEntityOfPage.isEmpty()) {
        metadataToDelete.add(data);
      } else {
        updateMainEntityOfPage(data, mainEntityOfPage);
      }
    });
    metadataRepository.saveAll(metadata);
    publicMetadataIndexService.updatePublicIndices(metadata);
    if (!metadataToDelete.isEmpty()) {
      delete(metadataToDelete, updatePublicIndices);
    }
    return true;
  }

  private void updateMainEntityOfPage(BackendMetadata metadata, List<Map<String, Object>> mainEntityOfPage) {
    metadata.getData().put(METADATA_PROPERTY_NAME_MAIN_ENTITY_OF_PAGE, mainEntityOfPage);
    if (metadata.getAdditionalData() != null && metadata.getAdditionalData().get(METADATA_PROPERTY_NAME_MAIN_ENTITY_OF_PAGE) != null) {
      metadata.getAdditionalData().put(METADATA_PROPERTY_NAME_MAIN_ENTITY_OF_PAGE, mainEntityOfPage);
    }
    metadata.setDateModified(LocalDateTime.now());
  }

  @Override
  public void initIndexMapping() {
    Document mapping;
    try {
      mapping = Document.parse(IOUtils.toString(indexMapping.getInputStream()));
    } catch (IOException e) {
      throw new IllegalStateException("index mapping cannot be loaded");
    }
    IndexOperations indexOperations = elasticsearchOperations.indexOps(BackendMetadata.class);
    indexOperations.putMapping(mapping);
  }

}
