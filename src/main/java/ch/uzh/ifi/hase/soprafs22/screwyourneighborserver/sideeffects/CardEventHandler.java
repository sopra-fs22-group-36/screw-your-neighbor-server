package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.sideeffects;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Card;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Match;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Round;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.CardRepository;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.ParticipationRepository;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.RoundRepository;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;

@RepositoryEventHandler
public class CardEventHandler {

  private final RoundRepository roundRepo;
  private final CardRepository cardRepo;
  private final ParticipationRepository participationRepo;

  public CardEventHandler(
      RoundRepository roundRepo,
      CardRepository cardRepo,
      ParticipationRepository participationRepo) {
    this.roundRepo = roundRepo;
    this.cardRepo = cardRepo;
    this.participationRepo = participationRepo;
  }

  @HandleAfterSave
  public void handleAfterSave(Card card) {
    Round currentRound = card.getRound();
    // card has been played:
    if (currentRound != null) {
      // check if every player has played his card i.e. as many cards are assigned to the current
      // round as we
      // have players in the game
      int numberOfPlayers = currentRound.getMatch().getGame().getParticipations().size();
      int numberOfPlayedCards = cardRepo.countByRound(currentRound).intValue();
      if (numberOfPlayedCards == numberOfPlayers) {
        Round newRound = createRound(currentRound.getMatch(), currentRound.getRoundNumber());
      }
    }
  }

  private Round createRound(Match match, int lastRoundNumber) {
    Round round = new Round();
    round.setRoundNumber(lastRoundNumber + 1);
    round.setMatch(match);
    match.getRounds().add(round);
    return round;
  }
}
