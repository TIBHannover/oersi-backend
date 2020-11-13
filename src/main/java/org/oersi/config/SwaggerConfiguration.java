package org.oersi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SuppressWarnings("SpellCheckingInspection")
@EnableSwagger2
@PropertySource("classpath:swagger.properties")
@ComponentScan(basePackages = "org.oersi")
@Configuration
public class SwaggerConfiguration {
    @Value("${swagger.version-api}")
    private  String swaggerApiVersion;
    @Value("${swagger.license-text}")
    private String licenseText ;
    @Value("${swagger.license-url}")
    private String licenseUrl ;
    @Value("${swagger.title}")
    private String title;
    @Value("${swagger.description}")
    private  String description ;
    @Value("${swagger.author.name}")
    private  String authorName;
    @Value("${swagger.author.email}")
    private  String authorEmail ;
    @Value("${swagger.author.url}")
    private  String authorUrl ;


    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title(this.title)
                .description(this.description)
                .license(licenseText)
                .licenseUrl(this.licenseUrl)
                .version(this.swaggerApiVersion)
                .contact(new Contact(this.authorName,this.authorUrl,this.authorEmail))
                .build();
    }

    @Bean
    public Docket productsApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .pathMapping("/")
                .select()
                .paths(PathSelectors.regex("/api.*"))
                .build();
    }
}
