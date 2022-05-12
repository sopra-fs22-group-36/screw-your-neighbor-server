package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Player;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;

@RepositoryRestResource
public interface PlayerRepository extends JpaRepository<Player, Long> {
  @SuppressWarnings("unused")
  Page<Player> findAllByName(String name, Pageable page);

  @RestResource(exported = false)
  Optional<Player> findByName(String name);

  @Override
  @PreAuthorize(
      "(hasRole('PLAYER') and #player.id == principal.id) or (!isAuthenticated() and #player.id == null)")
  <S extends Player> S save(@P("player") S entity);
}
