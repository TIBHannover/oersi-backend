package eu.tib.oersi;

import eu.tib.oersi.controller.SearchController;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(final HttpSecurity http) throws Exception {
    http.csrf().disable().authorizeRequests()
        .antMatchers(SearchController.BASE_PATH + "/**").permitAll();
  }

  @Override
  protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
    // TODO
    auth.inMemoryAuthentication().withUser("test").password("test").roles("TEST");
  }

}
