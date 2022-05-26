package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.sideeffects;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.*;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.*;
import java.util.Collection;
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
    int newRoundNumber = round.getRoundNumber() + 1;
    if (match.getLastRound().map(Round::getRoundNumber).orElse(-1) == newRoundNumber) {
      return;
    }

    int numberOfPlayedCardsInRound = round.getCards().size();
    // In case we have an additional battling round (after stacking), we have to count not all the
    // hands in the match, but only the ones that are involved in the battling round.
    long numberOfHands =
        match.getSortedActiveHands().stream()
            .filter(
                h ->
                    h.getCards().stream()
                        .anyMatch(c -> c.getRound() == round || c.getRound() == null))
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
    Integer numOfCards = Match.matchNoToNumberOfCards.get(newMatchNumber);
    if (numberOfPlayedRounds >= numberOfCardsPerPlayer) {
      if (round.isStacked()) {
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
