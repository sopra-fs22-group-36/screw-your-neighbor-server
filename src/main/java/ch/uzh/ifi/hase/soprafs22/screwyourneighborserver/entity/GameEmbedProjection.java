package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import java.util.Collection;
import org.springframework.data.rest.core.config.Projection;

@Projection(
    name = "embed",
    types = {Game.class})
public interface GameEmbedProjection {
  Long getId();

  String getName();

  GameState getGameState();

  Collection<ParticipationEmbedProjection> getParticipations();

  Game getNextGame();
}
