package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.validation;

import javax.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ValidationConfig {
  @Bean
  public OldStateFetcher oldStateFetcher(EntityManager entityManager) {
    EntityManager secondEntityManager =
        entityManager.getEntityManagerFactory().createEntityManager();
    return new OldStateFetcher(secondEntityManager);
  }
}
