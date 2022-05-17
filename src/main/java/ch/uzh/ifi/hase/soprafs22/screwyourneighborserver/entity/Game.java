package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.validation.bean.NoHtml;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.*;
import java.util.stream.Collectors;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
public class Game implements BelongsToGame {
  @Id @GeneratedValue private Long id;

  @NotBlank
  @Size(min = 3, max = 50)
  @NoHtml
  private String name;

  @NotNull private GameState gameState = GameState.FINDING_PLAYERS;

  private String videoChatName;

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "game")
  private Collection<Participation> participations = new ArrayList<>();

  @OneToMany(
      mappedBy = "game",
      cascade = {CascadeType.ALL})
  @LazyCollection(LazyCollectionOption.FALSE)
  private Collection<Match> matches = new ArrayList<>();

  @OneToOne(targetEntity = Game.class)
  private Game nextGame;

  @SuppressWarnings("unused")
  @Version
  @JsonIgnore
  private int version;

  public Long getId() {
    return id;
  }

  @JsonIgnore
  public void setId(Long id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = Objects.requireNonNull(name).trim();
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

  @JsonProperty
  public String getVideoChatName() {
    return videoChatName;
  }

  @JsonIgnore
  public void setVideoChatName(String videoChatId) {
    this.videoChatName = videoChatId;
  }

  public Collection<Participation> getParticipations() {
    return participations;
  }

  @JsonIgnore
  public void setParticipations(Collection<Participation> participations) {
    this.participations = participations;
  }

  public void setNextGame(Game nextGame) {
    this.nextGame = nextGame;
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

  @JsonIgnore
  @Override
  public Game getGame() {
    return this;
  }
}
