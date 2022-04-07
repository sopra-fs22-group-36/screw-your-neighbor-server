package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import java.util.Collection;
import org.springframework.data.rest.core.config.Projection;

@Projection(
    name = "embed",
    types = {Participation.class})
public interface ParticipationEmbedProjection {
  Long getId();

  boolean isActive();

  Integer getParticipationNumber();

  Game getGame();

  Player getPlayer();

  Collection<HandEmbedProjection> getHands();

  // todo:Collection<ScoreAnnouncements> getScoreAnnouncements();

}
