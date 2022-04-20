package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.sideeffects;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.*;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.*;
import javax.transaction.Transactional;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;

@RepositoryEventHandler
public class CardEventHandler {
  private final CardRepository cardRepo;
  private final RoundRepository roundRepo;
  private final MatchRepository matchRepo;
  private final HandRepository handRepo;
  private final GameRepository gameRepo;
  private InstanceCreator instanceCreator;

  public CardEventHandler(
      RoundRepository roundRepo,
      CardRepository cardRepo,
      MatchRepository matchRepo,
      HandRepository handRepo,
      GameRepository gameRepo) {
    this.roundRepo = roundRepo;
    this.cardRepo = cardRepo;
    this.matchRepo = matchRepo;
    this.handRepo = handRepo;
    this.gameRepo = gameRepo;
  }

  @SuppressWarnings("unused")
  @HandleAfterSave
  @Transactional
  public void handleAfterSave(Card card) {
    Round currentRound = card.getRound();
    int numberOfPlayedCards = getNumberOfPlayedCards(currentRound);
    int numberOfPlayers = getNumberOfPlayers(currentRound);
    // last card in round was played
    if (currentRound != null && numberOfPlayedCards >= numberOfPlayers) {
      instanceCreator = new InstanceCreator(roundRepo, cardRepo, matchRepo, handRepo);
      int numberOfCardsPerPlayer = getNumberOfCardsPerPlayer(card);
      int numberOfPlayedRounds = getNumberOfPlayedRounds(card);
      // no cards remaining in any hand
      if (numberOfPlayedRounds >= numberOfCardsPerPlayer) {
        Game game = getGame(currentRound);
        Match match = instanceCreator.createMatch(game);
        CardDeck cardDeck = new StandardCardDeck();
        int numOfCards = numberOfCardsPerPlayer - 1;
        for (var participation : game.getParticipations()) {
          Hand hand = instanceCreator.createHand(match, participation);
          for (int j = 0; j < numOfCards; j++) {
            Card newCard = instanceCreator.createCard(hand, cardDeck);
          }
          Round round = instanceCreator.createRound(match, 0);
        }
      } else {
        Round newRound =
            instanceCreator.createRound(currentRound.getMatch(), currentRound.getRoundNumber());
      }
    }
  }

  private int getNumberOfPlayers(Round round) {
    return round.getMatch().getGame().getParticipations().size();
  }

  private int getNumberOfPlayedCards(Round round) {
    return cardRepo.countByRound(round).intValue();
  }

  private int getNumberOfPlayedRounds(Card card) {
    return card.getHand().getMatch().getRounds().size();
  }

  private int getNumberOfCardsPerPlayer(Card card) {
    return card.getHand().getCards().size();
  }

  private Game getGame(Round round) {
    return round.getMatch().getGame();
  }
}
