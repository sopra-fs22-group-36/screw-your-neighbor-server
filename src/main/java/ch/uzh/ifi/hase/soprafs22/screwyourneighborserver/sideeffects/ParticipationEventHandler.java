package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.sideeffects;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.*;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.validation.OldStateFetcher;
import java.util.Collection;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler
public class ParticipationEventHandler {
  private final OldStateFetcher oldStateFetcher;
  private final HandEventHandler handEventHandler;
  private final CardEventHandler cardEventHandler;

  @Autowired
  public ParticipationEventHandler(
      OldStateFetcher oldStateFetcher,
      HandEventHandler handEventHandler,
      CardEventHandler cardEventHandler) {
    this.oldStateFetcher = oldStateFetcher;
    this.cardEventHandler = cardEventHandler;
    this.handEventHandler = handEventHandler;
  }

  @HandleBeforeSave
  public void onBeforeSave(Participation participation) {
    Participation previousStateOf =
        oldStateFetcher.getPreviousStateOf(Participation.class, participation.getId());
    if (previousStateOf.isActive() == participation.isActive()) {
      return;
    }
    if (participation.isActive()) {
      return;
    }
    Game game = participation.getGame();
    if (GameState.PLAYING.equals(game.getGameState())) {
      Optional<Match> lastMatch = game.getLastMatch();
      lastMatch.map(Match::getSortedActiveHands).stream()
          .flatMap(Collection::stream)
          .findFirst()
          .ifPresent(handEventHandler::onAfterSave);

      lastMatch.flatMap(Match::getLastRound).stream()
          .map(Round::getCards)
          .flatMap(Collection::stream)
          .findFirst()
          .ifPresent(cardEventHandler::handleAfterSave);
    }
  }
}
