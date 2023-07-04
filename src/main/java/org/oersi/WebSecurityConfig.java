package org.oersi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@PropertySource(value = "file:${envConfigDir:envConf/default/}oersi.properties")
public class WebSecurityConfig {

  private static final String ROLE_MANAGE_OERMETADATA = "MANAGE_OERMETADATA";

  @Value("${oermetadata.manage.user}")
  private String oermetadataUser;

  @Value("${oermetadata.manage.password}")
  private String oermetadataPassword;

  @Bean
  public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
    http.cors().and().csrf().disable().authorizeHttpRequests()
        .requestMatchers(
                "/api/search/**", "/api/label/**", "/api/deprecated/label/**", "/api/contact", "/api/oembed-json", "/api/oembed-xml"
        ).permitAll()
        .and().httpBasic()
        .and().authorizeHttpRequests()
        .requestMatchers("/api/metadata/**").hasRole(ROLE_MANAGE_OERMETADATA)
        .requestMatchers("/api/metadata-config/**").hasRole(ROLE_MANAGE_OERMETADATA)
        .requestMatchers("/api/vocab/**").hasRole(ROLE_MANAGE_OERMETADATA);
    return http.build();
  }

  @Bean
  public UserDetailsService userDetailsService() {
    UserDetails oerMetadataUser = User.withUsername(oermetadataUser)
        .passwordEncoder(PasswordEncoderFactories.createDelegatingPasswordEncoder()::encode)
        .password(oermetadataPassword)
        .roles(ROLE_MANAGE_OERMETADATA).build();

    InMemoryUserDetailsManager userDetailsManager = new InMemoryUserDetailsManager();
    userDetailsManager.createUser(oerMetadataUser);
    return userDetailsManager;
  }

}
