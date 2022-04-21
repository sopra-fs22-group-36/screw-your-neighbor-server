package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.sideeffects;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.*;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.*;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
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
  private final GameRepository gameRepository;

  private final ModelFactory modelFactory;

  public GameEventHandler(
      ParticipationRepository participationRepository,
      GameRepository gameRepository,
      ModelFactory modelFactory) {
    this.participationRepository = participationRepository;
    this.gameRepository = gameRepository;
    this.modelFactory = modelFactory;
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

  @SuppressWarnings("unused")
  @Transactional
  @HandleAfterSave
  public void handleAfterSave(Game game) {
    if (game.getGameState().equals(GameState.PLAYING) && game.getMatches().isEmpty()) {
      modelFactory.assignParticipationNumbers(game);

      // Create first match with first round and save them
      Match match = modelFactory.createMatch(game, 1);
      Round round = modelFactory.addRound(match, 1);

      // Create a standard card deck (currently there's only this one)
      CardDeck cardDeck = new StandardCardDeck();
      // In the first round, 5 cards per player are distributed.
      int numOfCards = 5;

      // for each player that participates in the game we create a hand
      for (var participation : game.getParticipations()) {
        Hand hand = modelFactory.createHand(match, participation);

        for (int j = 0; j < numOfCards; j++) {
          modelFactory.addCardTo(hand, cardDeck.drawCard());
        }
      }
      // Because we configured CascadeType.ALL from game downwards, we only need one save.
      // gameRepository.save(game);
      gameRepository.saveAll(List.of(game));
    }
  }
}
