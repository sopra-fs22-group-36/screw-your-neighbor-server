package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import java.util.Collection;
import org.springframework.data.rest.core.config.Projection;

@Projection(
    name = "embed",
    types = {Hand.class})
public interface HandEmbedProjection {

  Integer getAnnouncedScore();

  boolean isTurnActive();

  Collection<CardEmbedProjection> getCards();

  ParticipationEmbedProjection getParticipation();

  int getNumberOfWonTricks();

  Integer getPoints();
}
