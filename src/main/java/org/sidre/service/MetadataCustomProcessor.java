package org.sidre.service;

import org.sidre.domain.BackendMetadata;
import org.sidre.domain.OembedInfo;

public interface MetadataCustomProcessor {

  void process(BackendMetadata metadata);
  void postProcess(BackendMetadata metadata);
  OembedInfo processOembedInfo(OembedInfo oembedInfo, BackendMetadata metadata);

}
