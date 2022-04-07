package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.sideeffects;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.*;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.*;
import java.util.ArrayList;
import java.util.Arrays;
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
  private CardDeck myDeck;

  public GameEventHandler(
      ParticipationRepository participationRepository,
      MatchRepository matchRepo,
      HandRepository handRepo,
      CardRepository cardRepo) {
    this.participationRepository = participationRepository;
    this.matchRepo = matchRepo;
    this.handRepo = handRepo;
    this.cardRepo = cardRepo;
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
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (!(authentication.getPrincipal() instanceof Player)) {
      throw new HttpClientErrorException(
          HttpStatus.UNAUTHORIZED, "Cannot create game when not authorized");
    } else {
      if (game.getGameState().equals(GameState.PLAYING)) {
        // Create a match
        Match match = new Match();
        match.setGame(game);
        match.setRounds(Arrays.asList(new Round()));
        match.setMatchState(MatchState.DISTRIBUTE);
        matchRepo.save(match);

        // Create Hands according number of players
        int numOfCards = 2;
        Collection<Card> cardsPerHand = new ArrayList<>();

        // for each player
        for (var el : game.getParticipations()) {
          Hand hand = new Hand();
          hand.setMatch(match);
          // Link hand to player
          hand.setParticipation(el);
          // draw a number of cards
          for (int j = 0; j < numOfCards; j++) {
            Card card = myDeck.drawCard();
            cardsPerHand.add(card);
            cardRepo.save(card);
          }
          hand.setCards(cardsPerHand);
          handRepo.save(hand);
        }
      }
    }
  }
}
