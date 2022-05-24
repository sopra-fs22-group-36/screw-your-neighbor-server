package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Comparator;
import javax.persistence.*;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"hand_id", "round_id"})})
public class Card implements Comparable<Card>, BelongsToGame {
  @Id @GeneratedValue private Long id;

  @Column(updatable = false)
  private CardRank cardRank;

  @Column(updatable = false)
  private CardSuit cardSuit;

  @JsonBackReference("card-hand")
  @ManyToOne()
  private Hand hand;

  @ManyToOne() private Round round;

  public Card() {}

  public Card(CardRank cardRank, CardSuit cardSuit) {
    this.cardRank = cardRank;
    this.cardSuit = cardSuit;
  }

  /**
   * equals and hashCode are not overridden because it's a lot easier to work without persistence in
   * tests. In production thanks to ORM cache, the same object pointer is used for the same entity.
   */
  @SuppressWarnings("java:S1210")
  public int compareTo(Card c) {
    return Comparator.nullsFirst(Comparator.comparingInt(CardRank::ordinal))
        .compare(cardRank, c.cardRank);
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

  @JsonProperty
  public boolean isHighestCardInRound() {
    if (round == null) {
      return false;
    }
    if (cardRank == null) {
      return false;
    }
    return round.getHighestCards().contains(this);
  }

  @Override
  @JsonIgnore
  public Game getGame() {
    return hand.getGame();
  }
}
