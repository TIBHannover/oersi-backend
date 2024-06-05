package org.sidre.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sidre.domain.BackendConfig;
import org.sidre.domain.BackendMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PublicMetadataIndexServiceImpl implements PublicMetadataIndexService {
  private final @NonNull ConfigService configService;
  private final @NonNull ElasticsearchOperations elasticsearchOperations;


  private IndexCoordinates getPublicMetadataIndexCoordinates() {
    BackendConfig metadataConfig = configService.getMetadataConfig();
    if (metadataConfig != null && !StringUtils.isEmpty(metadataConfig.getMetadataIndexName())) {
      return IndexCoordinates.of(metadataConfig.getMetadataIndexName());
    }
    return null;
  }
  private IndexCoordinates getPublicExtendedMetadataIndexCoordinates() {
    BackendConfig metadataConfig = configService.getMetadataConfig();
    if (metadataConfig != null && !StringUtils.isEmpty(metadataConfig.getExtendedMetadataIndexName())) {
      return IndexCoordinates.of(metadataConfig.getExtendedMetadataIndexName());
    }
    return null;
  }


  @Transactional
  @Override
  public void updatePublicIndices(List<BackendMetadata> backendMetadata) {
    IndexCoordinates metadataIndexCoordinates = getPublicMetadataIndexCoordinates();
    if (metadataIndexCoordinates != null) {
      List<UpdateQuery> updates = backendMetadata.stream()
        .map(m -> getUpdateQuery(m.getId(), m.getData())).filter(Objects::nonNull).toList();
      if (!updates.isEmpty()) {
        elasticsearchOperations.bulkUpdate(updates, metadataIndexCoordinates);
        elasticsearchOperations.indexOps(metadataIndexCoordinates).refresh();
      }
    }
    IndexCoordinates extendedMetadataIndexCoordinates = getPublicExtendedMetadataIndexCoordinates();
    if (extendedMetadataIndexCoordinates != null) {
      List<UpdateQuery> updatesInternal = backendMetadata.stream()
        .map(m -> getUpdateQuery(m.getId(), m.getExtendedData())).filter(Objects::nonNull).toList();
      if (!updatesInternal.isEmpty()) {
        elasticsearchOperations.bulkUpdate(updatesInternal, extendedMetadataIndexCoordinates);
        elasticsearchOperations.indexOps(extendedMetadataIndexCoordinates).refresh();
      }
    }
  }

  private UpdateQuery getUpdateQuery(String id, Map<String, Object> data) {
    if (data == null || data.isEmpty()) {
      return null;
    }
    return UpdateQuery.builder(id).withDocument(Document.from(data)).withDocAsUpsert(true).build();
  }

  @Transactional
  @Override
  public void deleteAll() {
    recreateIndex(getPublicMetadataIndexCoordinates());
    recreateIndex(getPublicExtendedMetadataIndexCoordinates());
  }

  @Transactional
  @Override
  public void delete(List<BackendMetadata> backendMetadata) {
    List<String> ids = backendMetadata.stream().map(BackendMetadata::getId).toList();
    delete(getPublicMetadataIndexCoordinates(), ids);
    delete(getPublicExtendedMetadataIndexCoordinates(), ids);
  }

  private void delete(IndexCoordinates coordinates, List<String> ids) {
    if (coordinates != null) {
      ids.forEach(id -> elasticsearchOperations.delete(id, coordinates));
      elasticsearchOperations.indexOps(coordinates).refresh();
    }
  }

  private void recreateIndex(IndexCoordinates coordinates) {
    if (coordinates != null) {
      log.info("Deleting index {}", coordinates.getIndexName());
      IndexOperations indexOperations = elasticsearchOperations.indexOps(coordinates);
      indexOperations.delete();
      indexOperations.create();
      elasticsearchOperations.indexOps(coordinates).refresh();
    }
  }

}
