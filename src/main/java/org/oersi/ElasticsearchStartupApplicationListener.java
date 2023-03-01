package org.oersi;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.oersi.service.LabelService;
import org.oersi.service.MetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ElasticsearchStartupApplicationListener implements ApplicationListener<ApplicationReadyEvent> {

  private final @NonNull MetadataService metadataService;
  private final @NonNull LabelService labelService;

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    metadataService.initIndexMapping();
    labelService.init();
  }
}
