package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Card {
  @Id @GeneratedValue private Long id;

  private CardRank cardRank;

  private CardSuit cardSuit;

  public Card(CardRank cardRank, CardSuit cardSuit) {
    this.cardRank = cardRank;
    this.cardSuit = cardSuit;
  }

  public Long getId() {
    return this.id;
  }

  public String getCardRank() {
    return this.cardRank.name().toString();
  }

  public String getCardSuit() {
    return this.cardSuit.name().toString();
  }
}
