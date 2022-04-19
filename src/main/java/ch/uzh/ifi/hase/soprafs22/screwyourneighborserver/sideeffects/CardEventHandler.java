package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.sideeffects;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Card;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Match;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Round;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.CardRepository;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.RoundRepository;
import javax.transaction.Transactional;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;

@RepositoryEventHandler
public class CardEventHandler {
  private CardRepository cardRepo;
  private RoundRepository roundRepo;

  public CardEventHandler(RoundRepository roundRepo, CardRepository cardRepo) {
    this.roundRepo = roundRepo;
    this.cardRepo = cardRepo;
  }

  @SuppressWarnings("unused")
  @HandleAfterSave
  @Transactional
  public void handleAfterSave(Card card) {
    Round currentRound = card.getRound();
    // card has been played:
    if (currentRound != null) {
      // check if every player has played their card
      if (getNumberOfPlayedCards(currentRound) >= getNumberOfPlayers(currentRound)) {
        Round newRound = createRound(currentRound.getMatch(), currentRound.getRoundNumber());
        setOldRoundToInactive(currentRound);
      }
    }
  }

  private void setOldRoundToInactive(Round round) {
    round.setActive(false);
    roundRepo.save(round);
  }

  private Round createRound(Match match, int lastRoundNumber) {
    Round round = new Round();
    round.setRoundNumber(lastRoundNumber + 1);
    round.setMatch(match);
    match.getRounds().add(round);
    roundRepo.save(round);
    return round;
  }

  private int getNumberOfPlayers(Round round) {
    return round.getMatch().getGame().getParticipations().size();
  }

  private int getNumberOfPlayedCards(Round round) {
    return cardRepo.countByRound(round).intValue();
  }
}
