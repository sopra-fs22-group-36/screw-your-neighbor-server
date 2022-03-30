package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import javax.persistence.*;

@Entity
public class Turn {
  @Id @GeneratedValue private Long id;

  @ManyToOne(cascade = {CascadeType.PERSIST})
  private Game round;
}
