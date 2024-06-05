package org.sidre;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.sidre.service.MetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ElasticsearchStartupApplicationListener implements ApplicationListener<ApplicationReadyEvent> {

  private final @NonNull MetadataService metadataService;

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    metadataService.initIndexMapping();
  }
}
