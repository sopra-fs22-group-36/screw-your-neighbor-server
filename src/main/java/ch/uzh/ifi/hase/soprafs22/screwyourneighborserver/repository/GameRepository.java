package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Game;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.GameEmbedProjection;
import java.util.List;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

@RepositoryRestResource(excerptProjection = GameEmbedProjection.class)
public interface GameRepository extends PagingAndSortingRepository<Game, Long> {
  List<Game> findAllByName(@Param("name") String name);

  @Override
  @PreAuthorize("hasRole('PLAYER')")
  <S extends Game> S save(S entity);
}
