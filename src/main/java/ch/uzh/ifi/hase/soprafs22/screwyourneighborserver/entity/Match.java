package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.*;
import java.util.stream.Collectors;
import javax.persistence.*;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
public class Match implements BelongsToGame {

  public static final Map<Integer, Integer> matchNoToNumberOfCards =
      Map.of(1, 5, 2, 4, 3, 3, 4, 2, 5, 1, 6, 2, 7, 3, 8, 4, 9, 5);

  @Id @GeneratedValue private Long id;

  private int matchNumber;

  @OneToMany(
      mappedBy = "match",
      cascade = {CascadeType.ALL})
  private Collection<Round> rounds = new ArrayList<>();

  @OneToMany(
      mappedBy = "match",
      cascade = {CascadeType.ALL})
  @LazyCollection(LazyCollectionOption.FALSE)
  private Collection<Hand> hands = new ArrayList<>();

  @JsonBackReference("match-game")
  @ManyToOne(fetch = FetchType.LAZY)
  private Game game;

  private MatchState matchState;

  public Long getId() {
    return id;
  }

  public int getMatchNumber() {
    return matchNumber;
  }

  public Collection<Round> getRounds() {
    return rounds;
  }

  public Game getGame() {
    return game;
  }

  public MatchState getMatchState() {
    return matchState;
  }

  @JsonIgnore
  public void setId(Long id) {
    this.id = id;
  }

  public void setMatchNumber(int matchNumber) {
    this.matchNumber = matchNumber;
  }

  public void setRounds(Collection<Round> rounds) {
    this.rounds = rounds;
  }

  public void setGame(Game game) {
    this.game = game;
  }

  public void setMatchState(MatchState matchState) {
    this.matchState = matchState;
  }

  public Collection<Hand> getHands() {
    return hands;
  }

  @JsonIgnore
  public List<Hand> getSortedActiveHands() {
    List<Hand> sortedHands =
        hands.stream()
            .sorted(
                Comparator.comparingInt(hand -> hand.getParticipation().getParticipationNumber()))
            .filter(hand -> hand.getParticipation().isActive())
            .collect(Collectors.toList());
    Collections.rotate(sortedHands, -1 * (matchNumber - 1));
    return sortedHands;
  }

  public boolean isLastAnnouncement() {
    int count = getSortedActiveHands().size();
    int announcedHands = 0;
    for (Hand hand : getSortedActiveHands()) {
      if (hand.getAnnouncedScore() != null) {
        announcedHands++;
      }
    }
    return (count == announcedHands);
  }

  public int getSumOfScoreAnnouncement() {
    int countedScoreAnnouncements = 0;
    for (Hand hand : hands) {
      countedScoreAnnouncements += hand.getAnnouncedScore() != null ? hand.getAnnouncedScore() : 0;
    }

    return countedScoreAnnouncements;
  }

  public List<Round> getSortedRounds() {
    return rounds.stream()
        .sorted(Comparator.comparingInt(Round::getRoundNumber))
        .collect(Collectors.toList());
  }

  @JsonIgnore
  public boolean allScoresAnnounced() {
    return getSortedActiveHands().stream().allMatch(hand -> hand.getAnnouncedScore() != null);
  }

  @JsonIgnore
  public Optional<Round> getLastRound() {
    List<Round> roundsSorted = getSortedRounds();
    if (roundsSorted.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(roundsSorted.get(roundsSorted.size() - 1));
  }
}
