package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Round;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.RoundEmbedProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.lang.NonNull;

@RepositoryRestResource(excerptProjection = RoundEmbedProjection.class)
public interface RoundRepository extends JpaRepository<Round, Long> {
  @Override
  @NonNull
  @RestResource(exported = false)
  <S extends Round> S save(@NonNull S entity);
}
