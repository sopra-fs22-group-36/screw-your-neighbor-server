package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.sideeffects;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.*;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler
public class CardEventHandler {

  private final ModelFactory modelFactory;
  private final GameRepository gameRepository;

  private final HashMap<Integer, Integer> mapMatchNoToNumberOfCards = new HashMap<>();

  public CardEventHandler(ModelFactory modelFactory, GameRepository gameRepository) {
    this.modelFactory = modelFactory;
    this.gameRepository = gameRepository;
    mapMatchNoToNumberOfCards.put(1, 5);
    mapMatchNoToNumberOfCards.put(2, 4);
    mapMatchNoToNumberOfCards.put(3, 3);
    mapMatchNoToNumberOfCards.put(4, 2);
    mapMatchNoToNumberOfCards.put(5, 1);
    mapMatchNoToNumberOfCards.put(6, 2);
    mapMatchNoToNumberOfCards.put(7, 3);
    mapMatchNoToNumberOfCards.put(8, 4);
    mapMatchNoToNumberOfCards.put(9, 5);
  }

  @SuppressWarnings("unused")
  @HandleAfterSave
  @Transactional
  public void handleAfterSave(Card card) {
    Round round = card.getRound();
    if (round == null) {
      return;
    }

    Match match = round.getMatch();
    Game game = match.getGame();
    int newRoundNumber = round.getRoundNumber() + 1;
    if (match.getLastRound().map(Round::getRoundNumber).orElse(-1) == newRoundNumber) {
      return;
    }

    int numberOfPlayedCardsInRound = round.getCards().size();
    long numberOfHands =
        match.getHands().stream()
            .filter(
                h ->
                    h.getCards().stream()
                            .filter(c -> c.getRound() == round || c.getRound() == null)
                            .count()
                        > 0)
            .count();
    if (numberOfPlayedCardsInRound < numberOfHands) {
      return;
    }

    int newMatchNumber = match.getMatchNumber() + 1;
    if (game.getLastMatch().map(Match::getMatchNumber).orElse(-1) == newMatchNumber) {
      return;
    }

    Match attachNewRoundTo = match;

    int numberOfCardsPerPlayer = card.getHand().getCards().size();
    int numberOfPlayedRounds = match.getRounds().size();
    Integer numOfCards = mapMatchNoToNumberOfCards.get(newMatchNumber);
    if (numberOfPlayedRounds >= numberOfCardsPerPlayer) {
      if (round.isStacked()) {
        // modelFactory.addRound(attachNewRoundTo, newRoundNumber);
        Collection<Card> highestCards = round.getHighestCards();
        CardDeck cardDeck = new StandardCardDeck();
        cardDeck.shuffle();
        for (Card c : highestCards) {
          Hand battlingHand = c.getHand();
          Card battlingCard = cardDeck.drawCard();
          battlingCard.setHand(battlingHand);
          battlingHand.getCards().add(battlingCard);
        }
      } else if (numOfCards != null) {
        Match newMatch = modelFactory.addMatch(game, newMatchNumber);
        attachNewRoundTo = newMatch;
        newRoundNumber = 0;
        CardDeck cardDeck = new StandardCardDeck();
        cardDeck.shuffle();
        for (Participation participation : game.getParticipations()) {
          Hand hand = modelFactory.addHand(newMatch, participation);
          for (int j = 0; j < numOfCards; j++) {
            modelFactory.addCardTo(hand, cardDeck.drawCard());
          }
        }
      } else {
        game.setGameState(GameState.CLOSED);
      }
    }
    if (game.getGameState() == GameState.PLAYING) {
      modelFactory.addRound(attachNewRoundTo, newRoundNumber);
    }
    gameRepository.saveAll(List.of(game));
  }
}
