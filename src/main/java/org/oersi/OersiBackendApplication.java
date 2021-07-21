package org.oersi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AutoUpdateProperties.class)
public class OersiBackendApplication {

  public static void main(final String[] args) {
    SpringApplication.run(OersiBackendApplication.class, args);
  }

}
