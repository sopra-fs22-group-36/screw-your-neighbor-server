package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.sideeffects;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.*;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.*;
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
  private final MatchRepository matchRepo;
  private final HandRepository handRepo;
  private final CardRepository cardRepo;
  private final RoundRepository roundRepo;
  private final GameRepository gameRepo;
  private CardDeck cardDeck;
  private final ModelFactory modelFactory;

  public GameEventHandler(
      ParticipationRepository participationRepository,
      MatchRepository matchRepo,
      HandRepository handRepo,
      CardRepository cardRepo,
      RoundRepository roundRepo,
      GameRepository gameRepo) {
    this.participationRepository = participationRepository;
    this.matchRepo = matchRepo;
    this.handRepo = handRepo;
    this.cardRepo = cardRepo;
    this.roundRepo = roundRepo;
    this.gameRepo = gameRepo;
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
      instanceCreator = new InstanceCreator(roundRepo, cardRepo, matchRepo, handRepo);
      instanceCreator.assignParticipationNumbers(game);

      // Create first match with first round and save them
      Match match = instanceCreator.createMatch(game);
      Round round = instanceCreator.createRound(match, 0);

      // Create a standard card deck (currently there's only this one)
      cardDeck = new StandardCardDeck();
      // In the first round, 5 cards per player are distributed.
      int numOfCards = 5;

      // for each player that participates in the game we create a hand
      for (var participation : game.getParticipations()) {
        Hand hand = instanceCreator.createHand(match, participation);

        for (int j = 0; j < numOfCards; j++) {
          Card card = instanceCreator.createCard(hand, cardDeck);
        }
      }
      // Because we configured CascadeType.ALL from game downwards, we only need one save.
      gameRepo.save(game);
    }
  }
}
