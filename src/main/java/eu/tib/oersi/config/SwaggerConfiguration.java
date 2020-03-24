package eu.tib.oersi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SuppressWarnings("SpellCheckingInspection")
@PropertySource("classpath:swagger.properties")
@ComponentScan(basePackages = "eu.tib.oersi")
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


    private Info apiInfo() {
        return new Info()
                .title(this.title)
                .description(this.description)
                .license(new License().name(licenseText).url(this.licenseUrl))
                .version(this.swaggerApiVersion)
                .termsOfService("")
                .contact(new Contact()
                          .name(this.authorName)
                          .email(this.authorEmail)
                          .url(this.authorUrl));
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components().addSecuritySchemes("basicScheme",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")))
                .info(apiInfo());
    }


}
