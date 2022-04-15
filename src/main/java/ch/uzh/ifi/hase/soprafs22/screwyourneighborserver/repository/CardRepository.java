package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Card;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.CardEmbedProjection;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Round;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(excerptProjection = CardEmbedProjection.class)
public interface CardRepository extends JpaRepository<Card, Long> {
  @Query("SELECT COUNT(c) FROM Card c WHERE c.round=:round")
  Long countByRound(@Param("round") Round round);
}
