package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.*;

@Entity
public class Match {
  @Id @GeneratedValue private Long id;

  private int match_number;

  @OneToMany(mappedBy = "match")
  private Collection<Round> rounds = new ArrayList<>();

  @ManyToOne(cascade = {CascadeType.PERSIST})
  private Game game;
}
