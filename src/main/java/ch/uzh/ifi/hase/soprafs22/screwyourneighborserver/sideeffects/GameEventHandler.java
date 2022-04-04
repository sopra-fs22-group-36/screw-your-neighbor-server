package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.sideeffects;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Game;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Participation;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Player;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.ParticipationRepository;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

@Component
@RepositoryEventHandler
public class GameEventHandler {
  private final ParticipationRepository participationRepository;

  public GameEventHandler(ParticipationRepository participationRepository) {
    this.participationRepository = participationRepository;
  }

  @SuppressWarnings("unused")
  @HandleAfterCreate
  public void onAfterCreate(Game game) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (!(authentication.getPrincipal() instanceof Player)) {
      throw new HttpClientErrorException(
          HttpStatus.UNAUTHORIZED, "Cannot create game when not authorized");
    }
    Player player = (Player) authentication.getPrincipal();
    Participation participation = new Participation();
    participation.setGame(game);
    participation.setPlayer(player);
    participation.setParticipationNumber(1);
    game.getParticipations().add(participation);
    participationRepository.save(participation);
  }
}
