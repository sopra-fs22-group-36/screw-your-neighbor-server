package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import org.springframework.data.rest.core.config.Projection;

@Projection(
    name = "embed",
    types = {Player.class})
public interface PlayerEmbedProjection {
  String getName();
}
