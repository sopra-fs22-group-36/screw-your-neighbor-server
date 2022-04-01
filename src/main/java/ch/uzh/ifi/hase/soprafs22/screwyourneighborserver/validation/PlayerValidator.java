package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.validation;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Player;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

@Component
@RepositoryEventHandler
public class PlayerValidator {

  @SuppressWarnings("unused")
  @HandleBeforeCreate
  public void onBeforeCreatePlayer(Player player) {
    SecurityContext context = SecurityContextHolder.getContext();
    Authentication authentication = context.getAuthentication();
    if (authentication == null) {
      return;
    }
    Object principal = authentication.getPrincipal();
    if (principal instanceof Player) {
      throw new HttpClientErrorException(
          HttpStatus.UNPROCESSABLE_ENTITY,
          "This session already has a player. Only one player can be created per session.");
    }
  }

  @SuppressWarnings("unused")
  @HandleBeforeSave
  public void onBeforeUpdatePlayer(Player player) {
    SecurityContext context = SecurityContextHolder.getContext();
    Authentication authentication = context.getAuthentication();
    if (authentication == null) {
      throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }
    Object principal = authentication.getPrincipal();
    if (!(principal instanceof Player)) {
      throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }

    Player securityContextPlayer = (Player) authentication.getPrincipal();
    if (!securityContextPlayer.getId().equals(player.getId())) {
      throw new HttpClientErrorException(
          HttpStatus.FORBIDDEN, "You are not allowed to update another player");
    }
  }
}
