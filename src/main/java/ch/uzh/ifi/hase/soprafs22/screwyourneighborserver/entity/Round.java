package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

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
  private Game match;
}
