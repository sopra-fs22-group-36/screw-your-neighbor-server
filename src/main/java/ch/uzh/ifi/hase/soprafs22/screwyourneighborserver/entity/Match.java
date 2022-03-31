package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.*;

@Entity
public class Match {
  @Id @GeneratedValue private Long id;

  private int match_number;

  @OneToMany(mappedBy = "match")
  private Collection<Round> rounds = new ArrayList<>();

  @OneToMany(mappedBy = "match")
  private Collection<Hand> hands = new ArrayList<>();

  @ManyToOne(cascade = {CascadeType.PERSIST})
  private Game game;

  public Long getId() {
    return id;
  }

  public int getMatch_number() {
    return match_number;
  }

  public Collection<Round> getRounds() {
    return rounds;
  }

  public Game getGame() {
    return game;
  }

  @JsonIgnore
  public void setId(Long id) {
    this.id = id;
  }

  public void setMatch_number(int match_number) {
    this.match_number = match_number;
  }

  public void setRounds(Collection<Round> rounds) {
    this.rounds = rounds;
  }

  public void setGame(Game game) {
    this.game = game;
  }
}