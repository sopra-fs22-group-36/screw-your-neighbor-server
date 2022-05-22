package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Card;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.CardEmbedProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;

@RepositoryRestResource(excerptProjection = CardEmbedProjection.class)
public interface CardRepository extends JpaRepository<Card, Long> {
  @Override
  @SuppressWarnings({"SpringElInspection", "ELValidationInspection"})
  @PreAuthorize("hasRole('PLAYER') and isOwnCard(#card)")
  <S extends Card> S save(@P("card") S entity);
}
