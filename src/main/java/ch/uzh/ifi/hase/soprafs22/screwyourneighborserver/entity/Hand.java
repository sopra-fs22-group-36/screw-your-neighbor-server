package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.*;
import javax.persistence.*;

@Entity
public class Hand {
  @Id @GeneratedValue private Long id;

  private Integer announcedScore;

  @OneToMany(mappedBy = "hand", cascade = CascadeType.ALL)
  private Collection<Card> cards = new ArrayList<>();

  @JsonBackReference("hand-match")
  @ManyToOne
  private Match match;

  @ManyToOne private Participation participation;

  public Long getId() {
    return id;
  }

  public Integer getAnnouncedScore() {
    return announcedScore;
  }

  public void setAnnouncedScore(Integer announcedScore) {
    this.announcedScore = announcedScore;
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

  public void setCards(Collection<Card> cards) {
    this.cards = cards;
  }

  public void setMatch(Match match) {
    this.match = match;
  }

  public Participation getParticipation() {
    return participation;
  }

  public void setParticipation(Participation participation) {
    this.participation = participation;
  }

  public boolean isTurnActive() {
    Match activeMatch = match.getGame().getLastMatch().orElse(null);
    if (activeMatch == null) {
      return false;
    }
    if (activeMatch != match) {
      return false;
    }

    List<Hand> sortedHands = activeMatch.getSortedHands();
    if (!activeMatch.allScoresAnnounced()) {
      Hand handWithActiveTurn =
          sortedHands.stream()
              .filter(hand -> hand.getAnnouncedScore() == null)
              .findFirst()
              .orElseThrow();
      return this == handWithActiveTurn;
    }

    Optional<Round> activeRound = activeMatch.getLastRound();
    if (activeRound.isEmpty()) {
      return false;
    }
    Optional<Hand> firstHandWhichDidNotPlayCard =
        sortedHands.stream()
            .filter(
                hand ->
                    hand.getCards().stream()
                        .noneMatch(card -> card.getRound() == activeRound.get()))
            .findFirst();
    if (firstHandWhichDidNotPlayCard.isEmpty()) {
      return false;
    }
    return this == firstHandWhichDidNotPlayCard.get();
  }

  @JsonProperty
  public int getNumberOfWonTricks() {
    List<Round> sortedRounds = match.getSortedRounds();
    int numberOfWonTricks = 0;
    for (Round round : sortedRounds) {
      ArrayList<Card> highestCards = new ArrayList<>(round.getHighestCards());
      if (!highestCards.isEmpty() && cards.contains(highestCards.get(highestCards.size() - 1))) {
        numberOfWonTricks++;
      }
    }
    return numberOfWonTricks;
  }

  @JsonProperty
  public Integer getPoints() {
    if (announcedScore == null) {
      return null;
    }
    Round round = match.getLastRound().orElse(null);
    if (round == null || round.getCards().size() < match.getHands().size()) {
      return null;
    }
    int difference = Math.abs(announcedScore - getNumberOfWonTricks());
    if (difference == 0) {
      return announcedScore * announcedScore;
    }
    return -difference;
  }
}
