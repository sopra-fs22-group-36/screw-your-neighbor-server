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

  @OneToMany(
      mappedBy = "round",
      cascade = {CascadeType.ALL})
  private Collection<Card> cards = new ArrayList<>();

  @JsonBackReference("round-match")
  @ManyToOne
  private Match match;

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

  @JsonProperty
  public Collection<Long> getTrickWinnerIds() {
    Collection<Long> trickWinnerIds = new ArrayList<>();
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
      // if only cards with rank 6 are played, the dummy card with no references is still available
      // and must be overjumped
      if (card.getHand() != null) {
        trickWinnerIds.add(card.getHand().getParticipation().getPlayer().getId());
      }
    }
    return trickWinnerIds;
  }
}
