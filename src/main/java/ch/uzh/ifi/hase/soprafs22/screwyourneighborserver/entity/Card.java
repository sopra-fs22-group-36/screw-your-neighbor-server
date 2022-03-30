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

  public Card(CardRank cardRank, CardSuit cardSuit) {
    this.cardRank = cardRank;
    this.cardSuit = cardSuit;
  }

  public int compareTo(Card c) {
    if (c.cardRank.getValue() == this.cardRank.getValue()) {
      return 0;
    } else if (c.cardRank.getValue() < this.cardRank.getValue()) {
      return 1;
    } else {
      return -1;
    }
  }

  public boolean isGreaterThan(Card c) {
    if (this.compareTo(c) == 1) {
      // this object is greater than specified object
      return true;
    }
    return false;
  }

  public boolean isEqualTo(Card c) {
    if (c.compareTo(this) == 0) {
      return true;
    }
    return false;
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
}
