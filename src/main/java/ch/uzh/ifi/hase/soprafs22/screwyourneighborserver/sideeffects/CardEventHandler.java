package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.sideeffects;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.*;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.*;
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

  private final HashMap<Integer, Integer> mapMatchNoToNumberOfCards = new HashMap();

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

    int numberOfPlayedCardsInRound = round.getCards().size();
    long numberOfHands = match.getHands().size();
    // last card in round was played
    if (numberOfPlayedCardsInRound >= numberOfHands) {
      int numberOfCardsPerPlayer = card.getHand().getCards().size();
      int numberOfPlayedRounds = match.getRounds().size();
      // no cards remaining in any hand
      if (numberOfPlayedRounds >= numberOfCardsPerPlayer) {
        Match newMatch = modelFactory.addMatch(game, match.getMatchNumber() + 1);
        CardDeck cardDeck = new StandardCardDeck();
        int numOfCards = mapMatchNoToNumberOfCards.get(match.getMatchNumber() + 1);
        for (Participation participation : game.getParticipations()) {
          Hand hand = modelFactory.addHand(newMatch, participation);
          for (int j = 0; j < numOfCards; j++) {
            modelFactory.addCardTo(hand, cardDeck.drawCard());
          }
        }
        modelFactory.addRound(newMatch, round.getRoundNumber() + 1);
      } else {
        modelFactory.addRound(match, round.getRoundNumber() + 1);
      }
      gameRepository.saveAll(List.of(game));
    }
  }
}
