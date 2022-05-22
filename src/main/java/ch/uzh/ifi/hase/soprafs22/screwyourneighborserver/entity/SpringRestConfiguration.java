package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.ConfigurableHttpMethods;
import org.springframework.data.rest.core.mapping.ExposureConfiguration;
import org.springframework.data.rest.core.mapping.ResourceMetadata;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Configuration
public class SpringRestConfiguration {
  private static final List<Class<?>> ENTITIES_WITH_DELETE = List.of(Player.class);

  @Bean
  public RepositoryRestConfigurer repositoryRestConfigurer() {
    return new RepositoryRestConfigurer() {
      @Override
      public void configureRepositoryRestConfiguration(
          RepositoryRestConfiguration config, CorsRegistry cors) {
        RepositoryRestConfigurer.super.configureRepositoryRestConfiguration(config, cors);
        ExposureConfiguration exposureConfiguration = config.getExposureConfiguration();
        exposureConfiguration.withItemExposure(SpringRestConfiguration.this::disableMethods);
      }
    };
  }

  private ConfigurableHttpMethods disableMethods(
      ResourceMetadata resourceMetadata, ConfigurableHttpMethods httpMethods) {
    httpMethods = httpMethods.disable(HttpMethod.PUT);
    if (!ENTITIES_WITH_DELETE.contains(resourceMetadata.getDomainType())) {
      httpMethods = httpMethods.disable(HttpMethod.DELETE);
    }
    return httpMethods;
  }
}
