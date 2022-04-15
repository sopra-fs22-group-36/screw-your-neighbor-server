package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import java.util.Collection;
import org.springframework.data.rest.core.config.Projection;

@Projection(
    name = "embed",
    types = {Match.class})
public interface MatchEmbedProjection {

  int getMatchNumber();

  MatchState getMatchState();

  Collection<RoundEmbedProjection> getRounds();

  Collection<HandEmbedProjection> getHands();
}
