package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;

@Entity
public class Card implements Comparable<Card> {
  @Id @GeneratedValue private Long id;

  private CardRank cardRank;

  private CardSuit cardSuit;

  @ManyToOne(cascade = {CascadeType.PERSIST})
  private Hand hand;

  @OneToOne(cascade = {CascadeType.PERSIST})
  private Turn turn;

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

  public Turn getTurn() {
    return turn;
  }

  public CardRank getCardRank() {
    return this.cardRank;
  }

  public CardSuit getCardSuit() {
    return this.cardSuit;
  }

  public String getCardRankName() {
    return this.cardRank.name();
  }

  public String getCardSuitName() {
    return this.cardSuit.name();
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

  public void setTurn(Turn turn) {
    this.turn = turn;
  }
}
