package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.validation;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Game;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.GameState;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Participation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

@Component
@RepositoryEventHandler
public class GameValidator {
  private final OldStateFetcher oldStateFetcher;

  @Autowired
  public GameValidator(OldStateFetcher oldStateFetcher) {
    this.oldStateFetcher = oldStateFetcher;
  }

  @SuppressWarnings("unused")
  @HandleBeforeSave
  public void onUpdateGame(Game game) {
    GameState newGameState = game.getGameState();

    Game gameBefore = oldStateFetcher.getPreviousStateOf(game.getClass(), game.getId());
    GameState gameStateBefore = gameBefore.getGameState();

    if (gameStateBefore != newGameState) {
      if (gameStateBefore == GameState.PLAYING && newGameState != GameState.CLOSED) {
        throw new HttpClientErrorException(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "From %s you can change only to %s, your change was: %s"
                .formatted(GameState.PLAYING, GameState.CLOSED, newGameState));
      }

      if (gameStateBefore == GameState.CLOSED) {
        throw new HttpClientErrorException(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "GameState.CLOSED is the final state of the game and cannot be changed");
      }

      long numberOfActivePlayers =
          game.getParticipations().stream().filter(Participation::isActive).count();
      if (newGameState == GameState.PLAYING && numberOfActivePlayers < 2) {
        throw new HttpClientErrorException(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "You cannot start a game with less than 2 active participations");
      }
    }
  }
}
