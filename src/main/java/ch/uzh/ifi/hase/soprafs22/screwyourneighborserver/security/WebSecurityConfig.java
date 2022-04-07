package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.security;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity()
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.cors()
        .configurationSource(
            request -> {
              CorsConfiguration corsConfiguration = new CorsConfiguration();
              corsConfiguration.setAllowCredentials(true);
              corsConfiguration.setAllowedMethods(List.of(CorsConfiguration.ALL));
              corsConfiguration.setAllowedOrigins(
                  List.of(
                      "http://localhost:3000", "https://screw-your-neighbor-react.herokuapp.com"));
              corsConfiguration.setAllowedHeaders(List.of(CorsConfiguration.ALL));
              return corsConfiguration;
            });

    http.csrf().disable();

    http.headers()
        .contentSecurityPolicy(
            "form-action 'self' http://localhost:8080 screw-your-neighbor-server.herokuapp.com")
        .and()
        .frameOptions()
        .sameOrigin();

    http.sessionManagement(
        sessionConfig -> {
          sessionConfig.maximumSessions(1);
          sessionConfig.sessionCreationPolicy(SessionCreationPolicy.ALWAYS);
          sessionConfig.enableSessionUrlRewriting(false);
        });

    http.exceptionHandling();

    http.logout().disable();
  }
}
