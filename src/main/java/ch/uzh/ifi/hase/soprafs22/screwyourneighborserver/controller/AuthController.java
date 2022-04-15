package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.controller;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Player;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

@RestController
public class AuthController {
  private final PlayerRepository playerRepository;
  private final RepositoryEntityLinks repositoryEntityLinks;

  @Autowired
  public AuthController(
      PlayerRepository playerRepository, RepositoryEntityLinks repositoryEntityLinks) {
    this.playerRepository = playerRepository;
    this.repositoryEntityLinks = repositoryEntityLinks;
  }

  @GetMapping("/auth/session")
  public ResponseEntity<EntityModel<Player>> getCurrentSession() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Object principal = authentication.getPrincipal();
    if (!(principal instanceof Player)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    Player player =
        playerRepository
            .findById(((Player) principal).getId())
            .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND));
    EntityModel<Player> entityModel = EntityModel.of(player);
    Link linkToPlayer = repositoryEntityLinks.forType(Player::getId).linkToItemResource(player);
    entityModel.add(linkToPlayer.withSelfRel());
    entityModel.add(linkToPlayer.withRel("player"));
    return ResponseEntity.ok(entityModel);
  }

  @PostMapping("/auth/logout")
  public ResponseEntity<Void> logout() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Object principal = authentication.getPrincipal();
    if (!(principal instanceof Player)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    SecurityContextHolder.getContext().setAuthentication(null);
    return ResponseEntity.noContent().build();
  }
}
