package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.controller;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Player;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

  @Autowired
  public AuthController(PlayerRepository playerRepository) {
    this.playerRepository = playerRepository;
  }

  @GetMapping("/auth/session")
  public ResponseEntity<Player> getCurrentSession() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Object principal = authentication.getPrincipal();
    if (!(principal instanceof Player)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    Player player =
        playerRepository
            .findById(((Player) principal).getId())
            .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND));
    return ResponseEntity.ok(player);
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
