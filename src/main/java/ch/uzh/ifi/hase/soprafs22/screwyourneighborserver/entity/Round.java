package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import javax.persistence.*;

@Entity
public class Round implements BelongsToGame {
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
    for (Card card : getHighestCards()) {
      trickWinnerIds.add(card.getHand().getParticipation().getPlayer().getId());
    }
    return trickWinnerIds;
  }

  @JsonIgnore
  // suppress warning for use of implementation instead of interface
  // here it's important that the set is ordered.
  @SuppressWarnings("java:S1319")
  public LinkedHashSet<Card> getHighestCards() {
    LinkedHashSet<Card> highestCards = new LinkedHashSet<>();
    for (Card card : this.cards) {
      if (highestCards.isEmpty()) {
        highestCards.add(card);
      } else {
        Card currentHighestCard = highestCards.iterator().next();
        if (card.isGreaterThan(currentHighestCard)) {
          highestCards.clear();
          highestCards.add(card);
        } else if (card.isEqualTo(currentHighestCard)) {
          highestCards.add(card);
        }
      }
    }
    return highestCards;
  }

  public boolean isStacked() {
    if (cards.size() < match.getSortedActiveHands().size()) {
      return false;
    }
    return getHighestCards().size() > 1;
  }

  @Override
  @JsonIgnore
  public Game getGame() {
    return match.getGame();
  }
}
