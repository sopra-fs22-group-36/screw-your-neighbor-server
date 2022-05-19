package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Game;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.GameEmbedProjection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;

@RepositoryRestResource(excerptProjection = GameEmbedProjection.class)
public interface GameRepository extends JpaRepository<Game, Long> {
  List<Game> findAllByName(@Param("name") String name);

  @Override
  @SuppressWarnings({"SpringElInspection", "ELValidationInspection"})
  @PreAuthorize("hasRole('PLAYER') && (#game.id == null || playsIn(#game))")
  <S extends Game> S save(@P("game") S entity);
}
