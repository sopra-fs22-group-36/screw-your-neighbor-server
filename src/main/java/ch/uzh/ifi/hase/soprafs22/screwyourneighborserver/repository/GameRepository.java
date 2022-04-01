package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Game;
import java.util.List;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface GameRepository extends PagingAndSortingRepository<Game, Long> {
  List<Game> findAllByName(@Param("name") String name);
}
