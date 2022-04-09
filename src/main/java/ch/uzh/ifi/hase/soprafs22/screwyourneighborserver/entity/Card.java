package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;

@Entity
public class Card implements Comparable<Card> {
  @Id @GeneratedValue private Long id;

  private CardRank cardRank;

  private CardSuit cardSuit;

  @Transient private Participation trickWinner;

  @JsonBackReference @ManyToOne() private Hand hand;

  @ManyToOne() private Round round;

  public Card() {}

  public Card(CardRank cardRank, CardSuit cardSuit) {
    this.cardRank = cardRank;
    this.cardSuit = cardSuit;
  }

  public int compareTo(Card c) {
    if (c.cardRank.ordinal() == (this.cardRank.ordinal())) {
      return 0;
    } else if (c.cardRank.ordinal() < this.cardRank.ordinal()) {
      return 1;
    } else {
      return -1;
    }
  }

  public boolean isGreaterThan(Card c) {
    return (this.compareTo(c) > 0);
  }

  public boolean isEqualTo(Card c) {
    return (c.compareTo(this) == 0);
  }

  public Long getId() {
    return this.id;
  }

  public Hand getHand() {
    return hand;
  }

  public Round getRound() {
    return round;
  }

  public CardRank getCardRank() {
    return this.cardRank;
  }

  public CardSuit getCardSuit() {
    return this.cardSuit;
  }

  @JsonIgnore
  public void setId(Long id) {
    this.id = id;
  }

  public void setCardRank(CardRank cardRank) {
    this.cardRank = cardRank;
  }

  public void setCardSuit(CardSuit cardSuit) {
    this.cardSuit = cardSuit;
  }

  public void setHand(Hand hand) {
    this.hand = hand;
  }

  public void setRound(Round round) {
    this.round = round;
  }
}
