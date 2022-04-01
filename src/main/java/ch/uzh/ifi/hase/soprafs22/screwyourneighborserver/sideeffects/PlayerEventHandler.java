package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.sideeffects;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Player;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.security.Authorities;
import java.util.List;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler
public class PlayerEventHandler {

  @SuppressWarnings("unused")
  @HandleAfterCreate
  public void handlePlayerCreated(Player player) {
    SecurityContext context = SecurityContextHolder.getContext();
    RememberMeAuthenticationToken authentication =
        new RememberMeAuthenticationToken(
            player.getId().toString(),
            player,
            List.of(new SimpleGrantedAuthority(Authorities.ROLE_PLAYER.name())));
    context.setAuthentication(authentication);
  }
}
