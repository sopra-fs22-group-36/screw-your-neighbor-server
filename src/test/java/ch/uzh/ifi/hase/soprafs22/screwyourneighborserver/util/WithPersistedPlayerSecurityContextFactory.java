package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Player;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.security.Authorities;
import java.util.List;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithPersistedPlayerSecurityContextFactory
    implements WithSecurityContextFactory<WithPersistedPlayer> {
  private final PlayerRepository playerRepository;

  public WithPersistedPlayerSecurityContextFactory(PlayerRepository playerRepository) {
    this.playerRepository = playerRepository;
  }

  @Override
  public SecurityContext createSecurityContext(WithPersistedPlayer withPersistedPlayer) {
    Player newPlayer = new Player();
    Player player = playerRepository.findByName(withPersistedPlayer.playerName()).orElse(newPlayer);
    player.setName(withPersistedPlayer.playerName());
    playerRepository.saveAll(List.of(player));

    SecurityContext context = SecurityContextHolder.getContext();
    AbstractAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(
            newPlayer,
            newPlayer,
            List.of(new SimpleGrantedAuthority(Authorities.ROLE_PLAYER.name())));
    context.setAuthentication(authentication);

    return context;
  }
}
