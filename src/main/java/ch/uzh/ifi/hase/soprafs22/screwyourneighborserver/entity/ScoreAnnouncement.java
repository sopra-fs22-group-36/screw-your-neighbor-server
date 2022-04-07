package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class ScoreAnnouncement {
  @Id @GeneratedValue private Long id;

  private int announcedScore;

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
}
