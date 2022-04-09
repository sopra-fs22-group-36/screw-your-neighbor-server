package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.sideeffects;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.*;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.*;
import java.util.Collection;
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
  @HandleAfterSave
  public void handleAfterSave(Game game) {
    if (game.getGameState().equals(GameState.PLAYING) && game.getMatches().isEmpty()) {
      reorganizeParticipationNumber(game);

      // Create first match with first round and save them
      Match match = createMatch(game);
      Round round = createRound(match);

      // Create a standard card deck (currently there's only this one)
      cardDeck = new StandardCardDeck();
      // In the first round, 5 cards per player are distributed.
      int numOfCards = 5;

      // for each player that participates in the game we create a hand
      for (var participation : game.getParticipations()) {
        Hand hand = createHand(match, participation);

        for (int j = 0; j < numOfCards; j++) {
          Card card = createCard(hand);
        }
      }
      gameRepo.save(game);
    }
  }

  /**
   * Create a new card, drawn from a deck
   *
   * @return random drawn card
   */
  private Card createCard(Hand hand) {
    Card card = cardDeck.drawCard();
    card.setHand(hand);
    hand.getCards().add(card);
    return card;
  }

  /**
   * Create a hand and add the hand to a player
   *
   * @param match
   * @param participation
   * @return player hand
   */
  private Hand createHand(Match match, Participation participation) {
    Hand hand = new Hand();
    hand.setParticipation(participation);
    participation.getHands().add(hand);
    hand.setMatch(match);
    match.getHands().add(hand);
    return hand;
  }

  /** @return */
  private Round createRound(Match match) {
    Round round = new Round();
    round.setRoundNumber(1);
    round.setMatch(match);
    match.getRounds().add(round);
    return round;
  }

  /**
   * After a Game is started numbers are distributed to each player, to define the playing order
   * (who's turn it is, who's next etc.)
   *
   * @param game
   */
  private void reorganizeParticipationNumber(Game game) {
    Collection<Participation> part = game.getParticipations();
    int i = 0;
    for (var p : part) {
      p.setParticipationNumber(i);
      i++;
    }
  }

  private Match createMatch(Game game) {
    Match match = new Match();
    match.setMatchNumber(1);
    match.setMatchState(MatchState.ANNOUNCING);
    match.setGame(game);
    game.getMatches().add(match);
    return match;
  }
}
