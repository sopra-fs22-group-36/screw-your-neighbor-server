package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Card;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.CardEmbedProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(excerptProjection = CardEmbedProjection.class)
public interface CardRepository extends JpaRepository<Card, Long> {}
