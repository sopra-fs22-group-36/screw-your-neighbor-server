package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.*;

@Entity
public class Game {
  @Id @GeneratedValue private Long id;

  private String name;

  private GameState gameState = GameState.FINDING_PLAYERS;

  @OneToMany(mappedBy = "game")
  private Collection<Participation> participations = new ArrayList<>();

  @OneToMany(mappedBy = "game")
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
}
