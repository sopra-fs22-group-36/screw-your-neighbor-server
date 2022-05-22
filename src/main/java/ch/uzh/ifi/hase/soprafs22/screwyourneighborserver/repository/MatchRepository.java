package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Match;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.MatchEmbedProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.lang.NonNull;

@RepositoryRestResource(excerptProjection = MatchEmbedProjection.class)
public interface MatchRepository extends JpaRepository<Match, Long> {
  @Override
  @NonNull
  @RestResource(exported = false)
  <S extends Match> S save(@NonNull S entity);
}
