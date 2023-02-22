package org.oersi.service;

import org.oersi.domain.BackendMetadata;

import java.util.List;

public interface PublicMetadataIndexService {

  void updatePublicIndices(List<BackendMetadata> backendMetadata);
  void deleteAll();

  void delete(List<BackendMetadata> backendMetadata);

}
