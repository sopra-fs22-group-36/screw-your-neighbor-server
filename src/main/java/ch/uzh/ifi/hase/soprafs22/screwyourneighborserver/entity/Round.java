package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.*;

@Entity
public class Round {
  @Id @GeneratedValue private Long id;

  private int round_number;

  @OneToMany(mappedBy = "round")
  private Collection<Turn> turns = new ArrayList<>();

  @ManyToOne(cascade = {CascadeType.PERSIST})
  private Match match;

  public Long getId() {
    return id;
  }

  public int getRound_number() {
    return round_number;
  }

  public Collection<Turn> getTurns() {
    return turns;
  }

  public Match getMatch() {
    return match;
  }

  @JsonIgnore
  public void setId(Long id) {
    this.id = id;
  }

  public void setRound_number(int round_number) {
    this.round_number = round_number;
  }

  public void setTurns(Collection<Turn> turns) {
    this.turns = turns;
  }

  public void setMatch(Match match) {
    this.match = match;
  }
}
