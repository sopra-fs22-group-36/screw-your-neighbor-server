package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Match;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.MatchEmbedProjection;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(excerptProjection = MatchEmbedProjection.class)
public interface MatchRepository extends CrudRepository<Match, Long> {
  List<Match> findAllByMatchNumber(@Param("matchNumber") int matchNumber);

  List<Match> findAll();

  Match findById(long id);
}
