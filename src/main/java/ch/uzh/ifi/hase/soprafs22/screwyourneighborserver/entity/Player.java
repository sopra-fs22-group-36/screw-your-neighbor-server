package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.validation.bean.NoHtml;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Entity
public class Player {
  @Id @GeneratedValue private Long id;

  @NotBlank
  @Size(min = 3, max = 50)
  @NoHtml
  private String name;

  public Long getId() {
    return id;
  }

  @JsonIgnore
  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = Objects.requireNonNull(name).trim();
  }
}
