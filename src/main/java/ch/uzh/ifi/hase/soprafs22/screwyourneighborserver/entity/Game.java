package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.*;
import java.util.stream.Collectors;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Entity
public class Game {
  @Id @GeneratedValue private Long id;

  @NotBlank
  @Size(min = 3, max = 50)
  private String name;

  private GameState gameState = GameState.FINDING_PLAYERS;

  @OneToMany(mappedBy = "game")
  private Collection<Participation> participations = new ArrayList<>();

  @OneToMany(
      mappedBy = "game",
      cascade = {CascadeType.ALL})
  private Collection<Match> matches = new ArrayList<>();

  @OneToOne(targetEntity = Game.class)
  private Game nextGame;

  public Long getId() {
    return id;
  }

  @JsonIgnore
  public void setId(Long id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setGameState(GameState gameState) {
    this.gameState = gameState;
  }

  public GameState getGameState() {
    return gameState;
  }

  public Collection<Participation> getParticipations() {
    return participations;
  }

  public Game getNextGame() {
    return nextGame;
  }

  public Collection<Match> getMatches() {
    return matches;
  }

  @JsonIgnore
  public List<Match> getSortedMatches() {
    return getMatches().stream()
        .sorted(Comparator.comparingInt(Match::getMatchNumber))
        .collect(Collectors.toList());
  }

  @JsonIgnore
  public Optional<Match> getLastMatch() {
    List<Match> sortedMatches = getSortedMatches();
    if (sortedMatches.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(sortedMatches.get(sortedMatches.size() - 1));
  }
}
