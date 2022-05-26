package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.*;
import javax.persistence.*;

@Entity
public class Hand implements BelongsToGame {
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
    List<Hand> sortedHands = activeMatch.getSortedActiveHands();
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
    List<Round> sortedRounds = activeMatch.getSortedRounds();

    List<Hand> handsStartingWithPreviousWinner = rotateHandsByLastWinner(sortedHands, sortedRounds);
    Optional<Hand> firstHandWhichDidNotPlayCard =
        handsStartingWithPreviousWinner.stream()
            .filter(Hand::hasCardToPlay)
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
    Iterator<Round> roundIterator = sortedRounds.iterator();
    while (roundIterator.hasNext()) {
      Round round = roundIterator.next();
      if (round.isStacked()) {
        int numberOfStackedRounds = 1;
        while (roundIterator.hasNext() && round.isStacked()) {
          round = roundIterator.next();
          numberOfStackedRounds += 1;
        }
        // only count the stacked points, if the round after the stacked one(s) was won
        if (hasHandWon(round, this)) {
          numberOfWonTricks += numberOfStackedRounds;
        }
      } else if (hasHandWon(round, this)) {
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
    boolean allCardsPlayed =
        match.getHands().stream()
            .map(Hand::getCards)
            .flatMap(Collection::stream)
            .map(Card::getRound)
            .allMatch(Objects::nonNull);
    if (!allCardsPlayed) {
      return null;
    }
    int difference = Math.abs(announcedScore - getNumberOfWonTricks());
    if (difference == 0) {
      return announcedScore * announcedScore;
    }
    return -difference;
  }

  private static List<Hand> rotateHandsByLastWinner(
      List<Hand> sortedHands, List<Round> sortedRounds) {
    List<Hand> handsStartingWithPreviousWinner = new ArrayList<>(sortedHands);
    if (sortedRounds.size() <= 1) {
      return handsStartingWithPreviousWinner;
    }
    List<Round> previousRounds = sortedRounds.subList(0, sortedRounds.size() - 1);
    Collections.reverse(previousRounds);
    previousRounds.removeIf(Round::isStacked);
    for (Round round : previousRounds) {
      Optional<Hand> winnerHand =
          sortedHands.stream().filter(hand -> hasHandWon(round, hand)).findFirst();
      if (winnerHand.isPresent()) {
        int winnerHandIndex = sortedHands.indexOf(winnerHand.get());
        Collections.rotate(handsStartingWithPreviousWinner, -winnerHandIndex);
        break;
      }
    }
    return handsStartingWithPreviousWinner;
  }

  private static boolean hasHandWon(Round round, Hand hand) {
    ArrayList<Card> highestCards = new ArrayList<>(round.getHighestCards());
    return !highestCards.isEmpty()
        && hand.cards.contains(highestCards.get(highestCards.size() - 1));
  }

  @JsonIgnore
  private boolean hasCardToPlay() {
    return getCards().stream().anyMatch(c -> c.getRound() == null);
  }

  @Override
  @JsonIgnore
  public Game getGame() {
    return match.getGame();
  }
}
