package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.validation;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Game;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.GameState;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Participation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

@Component
@RepositoryEventHandler
public class ParticipationValidator {
  protected static final int MAX_NUMBER_OF_PLAYERS = 5;

  private final OldStateFetcher oldStateFetcher;

  @Autowired
  public ParticipationValidator(OldStateFetcher oldStateFetcher) {
    this.oldStateFetcher = oldStateFetcher;
  }

  @SuppressWarnings("unused")
  @HandleBeforeCreate
  public void onBeforeCreateParticipation(Participation participation) {
    Game game = participation.getGame();
    GameState participationGameState = game.getGameState();

    if (participationGameState != GameState.FINDING_PLAYERS) {
      throw new HttpClientErrorException(
          HttpStatus.UNPROCESSABLE_ENTITY,
          "Player can't join a game in game state %s".formatted(participationGameState));
    }
    if (!game.getParticipations().contains(participation)) {
      game.getParticipations().add(participation);
    }
    if (game.getParticipations().stream().filter(Participation::isActive).count()
        > MAX_NUMBER_OF_PLAYERS) {
      throw new HttpClientErrorException(
          HttpStatus.UNPROCESSABLE_ENTITY,
          "Not more then %s players per game are allowed".formatted(MAX_NUMBER_OF_PLAYERS));
    }
  }

  @HandleBeforeSave
  public void onBeforeSaveParticipation(Participation participation) {
    Participation previousStateOf =
        oldStateFetcher.getPreviousStateOf(Participation.class, participation.getId());
    if (previousStateOf.isActive() == participation.isActive()) {
      return;
    }
    if (participation.isActive()) {
      onBeforeCreateParticipation(participation);
    }
  }
}
