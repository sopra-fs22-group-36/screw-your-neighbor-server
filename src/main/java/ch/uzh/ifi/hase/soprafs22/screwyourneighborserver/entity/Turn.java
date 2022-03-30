package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;

@Entity
public class Turn {
  @Id @GeneratedValue private Long id;

  @OneToOne(mappedBy = "turn")
  private Card card;

  @ManyToOne(cascade = {CascadeType.PERSIST})
  private Game round;

  public Long getId() {
    return id;
  }

  public Card getCard() {
    return card;
  }

  public Game getRound() {
    return round;
  }

  @JsonIgnore
  public void setId(Long id) {
    this.id = id;
  }

  public void setCard(Card card) {
    this.card = card;
  }

  public void setRound(Game round) {
    this.round = round;
  }
}
