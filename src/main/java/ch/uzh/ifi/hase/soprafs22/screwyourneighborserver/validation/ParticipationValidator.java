package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.validation;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.GameState;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Participation;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

@Component
@RepositoryEventHandler
public class ParticipationValidator {

  @SuppressWarnings("unused")
  @HandleBeforeCreate
  public void onUpdateParticipation(Participation participation) {
    GameState participationGameState = participation.getGame().getGameState();

    if (participationGameState != GameState.FINDING_PLAYERS) {
      throw new HttpClientErrorException(
          HttpStatus.UNPROCESSABLE_ENTITY,
          "Player can't join a game in game state %s".formatted(participationGameState));
    }
  }
}
