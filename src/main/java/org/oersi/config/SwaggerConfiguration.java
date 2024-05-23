package org.oersi.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@PropertySource("classpath:swagger.properties")
@Configuration
@OpenAPIDefinition
public class SwaggerConfiguration {
  @Value("${swagger.version-api}")
  private String swaggerApiVersion;
  @Value("${swagger.license-text}")
  private String licenseText;
  @Value("${swagger.license-url}")
  private String licenseUrl;
  @Value("${swagger.title}")
  private String title;
  @Value("${swagger.description}")
  private String description;


  private Info apiInfo() {
    return new Info()
      .title(this.title)
      .description(this.description)
      .license(new License().url(this.licenseUrl).name(this.licenseText))
      .version(this.swaggerApiVersion);
  }

  @Bean
  public OpenAPI productsApi() {
    return new OpenAPI().info(apiInfo());
  }

}
