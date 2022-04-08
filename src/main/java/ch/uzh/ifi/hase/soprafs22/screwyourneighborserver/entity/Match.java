package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.*;

@Entity
public class Match {
  @Id @GeneratedValue private Long id;

  private int matchNumber;

  @OneToMany(
      mappedBy = "match",
      cascade = {CascadeType.PERSIST})
  private Collection<Round> rounds = new ArrayList<>();

  @OneToMany(mappedBy = "match")
  private Collection<Hand> hands = new ArrayList<>();

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
}
