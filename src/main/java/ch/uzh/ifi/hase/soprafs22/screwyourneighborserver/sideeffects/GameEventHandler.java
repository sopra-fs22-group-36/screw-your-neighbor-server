package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.sideeffects;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.*;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.*;
import java.util.ArrayList;
import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
  private CardDeck myDeck;

  public GameEventHandler(
      ParticipationRepository participationRepository,
      MatchRepository matchRepo,
      HandRepository handRepo,
      CardRepository cardRepo,
      RoundRepository roundRepo) {
    this.participationRepository = participationRepository;
    this.matchRepo = matchRepo;
    this.handRepo = handRepo;
    this.cardRepo = cardRepo;
    this.roundRepo = roundRepo;
    myDeck = new StandardCardDeck();
  }

  @SuppressWarnings("unused")
  @HandleAfterCreate
  public void onAfterCreate(Game game) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (!(authentication.getPrincipal() instanceof Player)) {
      throw new HttpClientErrorException(
          HttpStatus.UNAUTHORIZED, "Cannot create game when not authorized");
    }
    long partNum = 1;
    Player player = (Player) authentication.getPrincipal();
    Participation participation = new Participation();
    participation.setGame(game);
    participation.setPlayer(player);
    participation.setParticipationNumber(1);
    game.getParticipations().add(participation);
    participationRepository.save(participation);
  }

  @SuppressWarnings("unsued")
  @HandleAfterSave
  public void handleAfterSave(Game game) {
    if (game.getGameState().equals(GameState.PLAYING)) {
      reorganizeParticipationNumber(game);

      // Create a match and Round
      Match match = createMatch(game);
      // matchRepo.save(match); (machen wir schon in create Methode)
      Round round = createRound(match);
      // roundRepo.save(round); (machen wir schon in create Methode)

      // Create Hands according number of players
      int numOfCards = 2;
      Collection<Card> cardsPerHand = new ArrayList<>();

      // for each player
      for (var participation : game.getParticipations()) {
        Hand hand = createHand(match, participation);

        // draw a number of cards
        for (int j = 0; j < numOfCards; j++) {
          Card card = createCard(hand);
          // cardsPerHand.add(card);
          // card.setHand(hand);
          cardRepo.save(card);
        }
        // hand.setCards(cardsPerHand);

        // round.setCards(cardsPerHand);
        // roundRepo.save(round);
      }
      // match.setMatchState(MatchState.ANNOUNCING);
      // matchRepo.save(match);
    }
  }

  /**
   * Create a new card, drawn from a deck
   *
   * @param round
   * @return random drawn card
   */
  private Card createCard(Hand hand) {
    Card card = myDeck.drawCard();
    card.setHand(hand);
    // card.setRound(round);
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
    hand.setMatch(match);
    // Link hand to player
    hand.setParticipation(participation);
    handRepo.save(hand);
    return hand;
  }

  /** @return */
  private Round createRound(Match match) {
    Round round = new Round();
    round.setRoundNumber(1986);
    round.setMatch(match);
    roundRepo.save(round);
    return round;
  }

  /**
   * After a Game is started, each player which joined to the game will reorganize in which order
   * the player is in the
   *
   * @param game
   */
  private void reorganizeParticipationNumber(Game game) {
    Page<Participation> part = participationRepository.findAllByGame(game, Pageable.unpaged());
    int i = 0;
    for (var el : part) {
      el.setParticipationNumber(i);
      i++;
    }
  }

  private Match createMatch(Game game) {
    Match match = new Match();
    match.setGame(game);
    match.setMatchNumber(123);
    // match.setRounds(Arrays.asList(round));
    // wenn wir Karten in derselben Transaktion verteilen, wie wir den Match erstellen, dann wird
    // der Status DISTRIBUTE
    // gar nie relevant sein. Darum lasse ich ihn mal weg und geh direkt auf ANNOUNCING
    // match.setMatchState(MatchState.DISTRIBUTE);
    match.setMatchState(MatchState.ANNOUNCING);
    matchRepo.save(match);
    return match;
  }
}