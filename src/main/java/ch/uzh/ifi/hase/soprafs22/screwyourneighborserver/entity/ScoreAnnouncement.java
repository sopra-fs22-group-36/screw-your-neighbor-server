package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Entity
public class ScoreAnnouncement {
  @Id @GeneratedValue private Long id;

  private int announcedScore;

  @ManyToOne
  private Participation participation;

  @ManyToOne
  private Match match;

  public Long getId() {
    return id;
  }

  @JsonIgnore
  public void setId(Long id) {
    this.id = id;
  }

  public int getAnnouncedScore() {
    return announcedScore;
  }

  public void setAnnouncedScore(int announcedScore) {
    this.announcedScore = announcedScore;
  }

  public Participation getParticipation() {
    return participation;
  }

  public void setParticipation(Participation participation) {
    this.participation = participation;
  }

  public Match getMatch() {
    return match;
  }

  public void setMatch(Match match) {
    this.match = match;
  }
}
