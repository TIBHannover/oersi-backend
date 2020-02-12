package eu.tib.oersi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
//@PropertySource(value = "file:${envConfigDir:envConf/default/}profileservice.properties")
public class WebConfig {

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

}
