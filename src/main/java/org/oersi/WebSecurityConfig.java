package org.oersi;

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
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@PropertySource(value = "file:${envConfigDir:envConf/default/}search_index.properties")
public class WebSecurityConfig {

  private static final String ROLE_MANAGE_METADATA = "MANAGE_OERMETADATA";

  @Value("${metadata.manage.user}")
  private String metadataManageUser;

  @Value("${metadata.manage.password}")
  private String metadataManagePassword;

  @Bean
  public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
    http
        .cors(withDefaults())
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                        AntPathRequestMatcher.antMatcher("/api/search/**"),
                        AntPathRequestMatcher.antMatcher("/api/label/**"),
                        AntPathRequestMatcher.antMatcher("/api/deprecated/label/**"),
                        AntPathRequestMatcher.antMatcher("/api/contact"),
                        AntPathRequestMatcher.antMatcher("/api/oembed-json"),
                        AntPathRequestMatcher.antMatcher("/api/oembed-xml"),
                        // swagger ui
                        AntPathRequestMatcher.antMatcher("/swagger-ui.html"),
                        AntPathRequestMatcher.antMatcher("/swagger-ui/**"),
                        AntPathRequestMatcher.antMatcher("/api-docs/**")
                ).permitAll()
        )
        .httpBasic(withDefaults())
        .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/metadata/**")).hasRole(ROLE_MANAGE_METADATA)
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/metadata-config/**")).hasRole(ROLE_MANAGE_METADATA)
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/vocab/**")).hasRole(ROLE_MANAGE_METADATA)
        );
    return http.build();
  }

  @Bean
  public UserDetailsService userDetailsService() {
    UserDetails metadataManageUser = User.withUsername(this.metadataManageUser)
        .passwordEncoder(PasswordEncoderFactories.createDelegatingPasswordEncoder()::encode)
        .password(metadataManagePassword)
        .roles(ROLE_MANAGE_METADATA).build();

    InMemoryUserDetailsManager userDetailsManager = new InMemoryUserDetailsManager();
    userDetailsManager.createUser(metadataManageUser);
    return userDetailsManager;
  }

}
