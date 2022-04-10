package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.*;

@Entity
public class Round {
  @Id @GeneratedValue private Long id;

  private int roundNumber;

  // ID(s) of the player(s) that played the highest card(s)
  @Transient Collection<Long> TrickWinnerIds = new ArrayList<>();

  @OneToMany(
      mappedBy = "round",
      cascade = {CascadeType.ALL})
  private Collection<Card> cards = new ArrayList<>();

  @JsonBackReference @ManyToOne private Match match;

  public Long getId() {
    return id;
  }

  public int getRoundNumber() {
    return roundNumber;
  }

  public Collection<Card> getCards() {
    return cards;
  }

  public Match getMatch() {
    return match;
  }

  @JsonIgnore
  public void setId(Long id) {
    this.id = id;
  }

  public void setRoundNumber(int roundNumber) {
    this.roundNumber = roundNumber;
  }

  public void setCards(Collection<Card> cards) {
    this.cards = cards;
  }

  public void setMatch(Match match) {
    this.match = match;
  }

  public void setTrickWinnerIds() {
    CardRank cardRank = CardRank.SIX;
    CardSuit cardSuit = CardSuit.HEART;
    Collection<Card> highestCard = new ArrayList<>();
    highestCard.add(new Card(cardRank, cardSuit));
    for (Card card : this.cards) {
      Card currentHighestCard = highestCard.iterator().next();
      if (card.isGreaterThan(currentHighestCard)) {
        highestCard.clear();
        highestCard.add(card);
      } else if (card.isEqualTo(currentHighestCard)) {
        highestCard.add(card);
      }
    }
    for (Card card : highestCard) {
      this.TrickWinnerIds.add(card.getHand().getParticipation().getPlayer().getId());
    }
  }

  @JsonProperty
  public Collection<Long> getTrickWinnerIds() {
    return this.TrickWinnerIds;
  }
}
