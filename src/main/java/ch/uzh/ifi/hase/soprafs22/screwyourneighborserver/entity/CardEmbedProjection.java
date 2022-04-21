package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import org.springframework.data.rest.core.config.Projection;

@Projection(
    name = "embed",
    types = {Card.class})
public interface CardEmbedProjection {
  Hand getHand();

  Round getRound();

  CardRank getCardRank();

  CardSuit getCardSuit();

  boolean isHighestCardInRound();
}
