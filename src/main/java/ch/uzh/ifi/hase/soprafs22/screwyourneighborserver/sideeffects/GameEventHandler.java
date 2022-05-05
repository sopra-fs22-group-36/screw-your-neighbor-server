package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.sideeffects;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.*;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.*;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import javax.transaction.Transactional;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

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
    game.setVideoChatName(UUID.randomUUID().toString());

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
      assignParticipationNumbers(game);

      // Create first match with first round and save them
      Match match = modelFactory.addMatch(game, 1);
      Round round = modelFactory.addRound(match, 1);

      // Create a standard card deck (currently there's only this one)
      CardDeck cardDeck = new StandardCardDeck();
      cardDeck.shuffle();
      // In the first round, 5 cards per player are distributed.
      int numOfCards = 5;

      // for each player that participates in the game we create a hand
      for (var participation : game.getParticipations()) {
        Hand hand = modelFactory.addHand(match, participation);

        for (int j = 0; j < numOfCards; j++) {
          modelFactory.addCardTo(hand, cardDeck.drawCard());
        }
      }
      gameRepository.saveAll(List.of(game));
    }
  }

  void assignParticipationNumbers(Game game) {
    Collection<Participation> part = game.getParticipations();
    int i = 0;
    for (var p : part) {
      p.setParticipationNumber(i);
      i++;
    }
  }
}
