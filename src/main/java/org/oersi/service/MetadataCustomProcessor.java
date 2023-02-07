package org.oersi.service;

import org.oersi.domain.BackendMetadata;
import org.oersi.domain.OembedInfo;

public interface MetadataCustomProcessor {

  void process(BackendMetadata metadata);
  void postProcess(BackendMetadata metadata);
  OembedInfo processOembedInfo(OembedInfo oembedInfo, BackendMetadata metadata);

}
