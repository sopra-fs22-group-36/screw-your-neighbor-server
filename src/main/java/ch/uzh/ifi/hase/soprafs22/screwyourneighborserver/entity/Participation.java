package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;

@Entity
public class Participation {

  @Id @GeneratedValue private Long id;

  private boolean active;

  private Integer participationNumber;

  @ManyToOne(cascade = {CascadeType.PERSIST})
  private Game game;

  @ManyToOne private Player player;

  public Long getId() {
    return id;
  }

  @JsonIgnore
  public void setId(Long id) {
    this.id = id;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public Integer getParticipationNumber() {
    return participationNumber;
  }

  public Game getGame() {
    return game;
  }

  public void setGame(Game game) {
    this.game = game;
  }

  public Player getPlayer() {
    return player;
  }

  public void setPlayer(Player player) {
    this.player = player;
  }

  public void setParticipationNumber(Integer participationNumber) {
    this.participationNumber = participationNumber;
  }
}
