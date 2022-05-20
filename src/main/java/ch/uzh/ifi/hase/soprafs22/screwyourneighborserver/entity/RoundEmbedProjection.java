package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import java.util.Collection;
import org.springframework.data.rest.core.config.Projection;

@Projection(
    name = "embed",
    types = {Round.class})
public interface RoundEmbedProjection {

  int getRoundNumber();

  Collection<CardEmbedProjection> getCards();

  Collection<Long> getTrickWinnerIds();

  boolean isStacked();
}
