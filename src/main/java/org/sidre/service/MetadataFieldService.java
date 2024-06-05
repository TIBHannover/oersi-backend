package org.sidre.service;

import lombok.Data;
import org.sidre.domain.BackendMetadata;
import org.springframework.data.elasticsearch.core.document.Document;

import java.util.List;
import java.util.Map;

public interface MetadataFieldService {
  @Data
  class MetadataSourceItem {
    public MetadataSourceItem(Object value) {
      this.value = value;
    }
    Object value;
  }
  BackendMetadata toMetadata(Map<String, Object> properties);
  String getIdentifier(Map<String, Object> properties);
  Document getBackendMetadataMapping();
  String getNamedMetadataSourceQueryField(String queryName);
  String getMetadataSourceIdentifierField();
  List<MetadataSourceItem> getMetadataSourceItems(Map<String, Object> data);
  List<String> getValues(MetadataSourceItem metadataSourceItem, String metadataSourceField);
  String getIdentifier(MetadataSourceItem metadataSourceItem);

  /**
   * Update the given metadata and set the new values from the given MetadataSourceItem.
   * @param data the data to update
   * @param metadataSourceItems update data with this values
   */
  void updateMetadataSource(Map<String, Object> data, List<MetadataSourceItem> metadataSourceItems);
}
