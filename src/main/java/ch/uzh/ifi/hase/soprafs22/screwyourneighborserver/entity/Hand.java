package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.*;

@Entity
public class Hand {
  @Id @GeneratedValue private Long id;

  private Integer announcedScore;

  @OneToMany(mappedBy = "hand", cascade = CascadeType.ALL)
  private Collection<Card> cards = new ArrayList<>();

  @JsonBackReference @ManyToOne private Match match;

  @ManyToOne private Participation participation;

  public Long getId() {
    return id;
  }

  public Integer getAnnouncedScore() {
    return announcedScore;
  }

  public void setAnnouncedScore(Integer announcedScore) {
    this.announcedScore = announcedScore;
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

  public Participation getParticipation() {
    return participation;
  }

  public void setParticipation(Participation participation) {
    this.participation = participation;
  }
}
