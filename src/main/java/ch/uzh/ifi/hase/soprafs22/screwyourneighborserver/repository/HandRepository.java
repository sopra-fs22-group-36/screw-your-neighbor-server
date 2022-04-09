package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Hand;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.HandEmbedProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(excerptProjection = HandEmbedProjection.class)
public interface HandRepository extends JpaRepository<Hand, Long> {}
