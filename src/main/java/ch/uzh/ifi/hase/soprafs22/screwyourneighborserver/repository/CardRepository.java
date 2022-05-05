package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Card;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.CardEmbedProjection;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Round;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;

@RepositoryRestResource(excerptProjection = CardEmbedProjection.class)
public interface CardRepository extends JpaRepository<Card, Long> {

  @Override
  @SuppressWarnings({"SpringElInspection", "ELValidationInspection"})
  @PreAuthorize("belongsToGame(#card.game)")
  <S extends Card> S save(@P("card") S entity);

  @RestResource(exported = false)
  @Query("SELECT COUNT(c) FROM Card c WHERE c.round=:round")
  Long countByRound(@Param("round") Round round);
}
