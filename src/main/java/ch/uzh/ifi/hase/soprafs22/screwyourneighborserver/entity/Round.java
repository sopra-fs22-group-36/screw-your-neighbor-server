package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.*;

@Entity
public class Round {
  @Id @GeneratedValue private Long id;

  private int roundNumber;

  @OneToMany(mappedBy = "round")
  private Collection<Turn> turns = new ArrayList<>();

  @ManyToOne(cascade = {CascadeType.PERSIST})
  private Match match;

  public Long getId() {
    return id;
  }

  public int getRoundNumber() {
    return roundNumber;
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

  public void setRoundNumber(int roundNumber) {
    this.roundNumber = roundNumber;
  }

  public void setTurns(Collection<Turn> turns) {
    this.turns = turns;
  }

  public void setMatch(Match match) {
    this.match = match;
  }
}
