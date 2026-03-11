package org.sidre;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@PropertySource(value = "file:${envConfigDir:envConf/default/}search_index.properties")
public class WebSecurityConfig {

  private static final String ROLE_MANAGE_METADATA = "MANAGE_METADATA";

  @Value("${metadata.manage.user}")
  private String metadataManageUser;

  @Value("${metadata.manage.password}")
  private String metadataManagePassword;

  @Bean
  public SecurityFilterChain filterChain(final HttpSecurity http) {
    http
        .cors(withDefaults())
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                        PathPatternRequestMatcher.pathPattern("/api/search/**"),
                        PathPatternRequestMatcher.pathPattern("/api/label/**"),
                        PathPatternRequestMatcher.pathPattern("/api/contact"),
                        PathPatternRequestMatcher.pathPattern("/api/oembed-json"),
                        PathPatternRequestMatcher.pathPattern("/api/oembed-xml"),
                        // swagger ui
                        PathPatternRequestMatcher.pathPattern("/swagger-ui.html"),
                        PathPatternRequestMatcher.pathPattern("/swagger-ui/**"),
                        PathPatternRequestMatcher.pathPattern("/api-docs/**")
                ).permitAll()
        )
        .httpBasic(withDefaults())
        .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(PathPatternRequestMatcher.pathPattern("/api/metadata/**")).hasRole(ROLE_MANAGE_METADATA)
                .requestMatchers(PathPatternRequestMatcher.pathPattern("/api/metadata-enrichment/**")).hasRole(ROLE_MANAGE_METADATA)
                .requestMatchers(PathPatternRequestMatcher.pathPattern("/api/metadata-config/**")).hasRole(ROLE_MANAGE_METADATA)
                .requestMatchers(PathPatternRequestMatcher.pathPattern("/api/vocab/**")).hasRole(ROLE_MANAGE_METADATA)
        );
    return http.build();
  }

  @Bean
  public UserDetailsService userDetailsService() {
    UserDetails metadataManageUserDetails = User.withUsername(this.metadataManageUser)
        .passwordEncoder(PasswordEncoderFactories.createDelegatingPasswordEncoder()::encode)
        .password(metadataManagePassword)
        .roles(ROLE_MANAGE_METADATA).build();

    InMemoryUserDetailsManager userDetailsManager = new InMemoryUserDetailsManager();
    userDetailsManager.createUser(metadataManageUserDetails);
    return userDetailsManager;
  }

}
