package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;
import javax.persistence.*;

@Entity
public class Participation {

  @Id @GeneratedValue private Long id;

  private boolean active = true;

  private Integer participationNumber;

  @JsonBackReference("participation-game")
  @ManyToOne(fetch = FetchType.LAZY)
  private Game game;

  @JsonBackReference("participation-player")
  @ManyToOne
  private Player player;

  @OneToMany(mappedBy = "participation")
  private Collection<Hand> hands = new ArrayList<>();

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

  public Collection<Hand> getHands() {
    return hands;
  }

  @JsonIgnore
  public Hand getActiveHand() {
    for (Hand hand : hands) {
      if (hand.isActive()) {
        return hand;
      }
    }
    throw new NoSuchElementException();
  }
}
