package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.*;

@Entity
public class Hand {
  @Id @GeneratedValue private Long id;

  @OneToMany(mappedBy = "hand")
  private Collection<Card> cards = new ArrayList<>();

  @ManyToOne(cascade = {CascadeType.PERSIST})
  private Match match;

  public Long getId() {
    return id;
  }

  public Collection<Card> getCards() {
    return cards;
  }

  public Match getMatch() {
    return match;
  }

  @JsonIgnore
  public void setId(Long id) {
    this.id = id;
  }

  public void setCards(Collection<Card> cards) {
    this.cards = cards;
  }

  public void setMatch(Match match) {
    this.match = match;
  }
}
