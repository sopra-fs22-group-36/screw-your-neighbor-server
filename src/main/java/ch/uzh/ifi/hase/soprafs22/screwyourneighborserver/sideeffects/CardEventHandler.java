package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.sideeffects;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.*;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.*;
import javax.transaction.Transactional;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RepositoryEventHandler
public class CardEventHandler {

  private final ModelFactory modelFactory;
  private final GameRepository gameRepository;

  public CardEventHandler(ModelFactory modelFactory, GameRepository gameRepository) {
    this.modelFactory = modelFactory;
    this.gameRepository = gameRepository;
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
    //noinspection ConstantConditions @BacLuc: i don't know why this is needed in my intellij
    if (numberOfPlayedCardsInRound >= numberOfHands) {
      int numberOfCardsPerPlayer = card.getHand().getCards().size();
      int numberOfPlayedRounds = match.getRounds().size();
      // no cards remaining in any hand
      if (numberOfPlayedRounds >= numberOfCardsPerPlayer) {
        Match newMatch = modelFactory.createMatch(game);
        CardDeck cardDeck = new StandardCardDeck();
        int numOfCards = numberOfCardsPerPlayer - 1;
        for (var participation : game.getParticipations()) {
          Hand hand = modelFactory.createHand(newMatch, participation);
          for (int j = 0; j < numOfCards; j++) {
            modelFactory.addCardTo(hand, cardDeck.drawCard());
          }
          modelFactory.addRound(newMatch, round.getRoundNumber());
        }
      } else {
        modelFactory.addRound(match, round.getRoundNumber());
      }
    }
    gameRepository.saveAll(List.of(game));
  }
}
