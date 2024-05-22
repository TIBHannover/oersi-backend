package org.oersi;

import org.oersi.config.BaseFieldConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@EnableConfigurationProperties({AutoUpdateProperties.class, BaseFieldConfig.class})
public class OersiBackendApplication {

  public static void main(final String[] args) {
    SpringApplication.run(OersiBackendApplication.class, args);
  }

}
