package org.oersi;

import org.oersi.controller.SearchController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
@EnableWebSecurity
@PropertySource(value = "file:${envConfigDir:envConf/default/}oersi.properties")
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  private static final String ROLE_MANAGE_OERMETADATA = "MANAGE_OERMETADATA";

  @Value("${oermetadata.manage.user}")
  private String oermetadataUser;

  @Value("${oermetadata.manage.password}")
  private String oermetadataPassword;

  @Override
  protected void configure(final HttpSecurity http) throws Exception {
    http.csrf().disable().authorizeRequests()
        .antMatchers(SearchController.BASE_PATH + "/**").permitAll()
        .and().httpBasic()
        .and().authorizeRequests()
        .antMatchers("/api/metadata/**").hasRole(ROLE_MANAGE_OERMETADATA)
        .antMatchers("/api/labeldefinition/**").hasRole(ROLE_MANAGE_OERMETADATA);
  }

  @Bean
  @Override
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
