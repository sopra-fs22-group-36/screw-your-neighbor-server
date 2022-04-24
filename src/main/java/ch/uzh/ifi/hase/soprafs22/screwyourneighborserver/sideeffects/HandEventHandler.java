package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.sideeffects;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Hand;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Match;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.MatchState;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.HandRepository;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.MatchRepository;
import java.util.Collection;
import javax.persistence.EntityNotFoundException;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler
public class HandEventHandler {

  private final MatchRepository matchRepository;
  private final HandRepository handRepository;

  public HandEventHandler(MatchRepository matchRepository, HandRepository handRepository) {
    this.matchRepository = matchRepository;
    this.handRepository = handRepository;
  }

  @SuppressWarnings("unused")
  @HandleAfterSave
  public void onAfterSave(Hand hand) {
    long idMatch = hand.getMatch().getId();
    Match myMatch =
        matchRepository
            .findById(idMatch)
            .orElseThrow(() -> new EntityNotFoundException(idMatch + ""));
    Collection<Hand> allHands = getAllHandsFromMatch(hand);
    if (allPlayersAnnouncedScore(allHands)) {
      myMatch.setMatchState(MatchState.PLAYING);
      matchRepository.save(myMatch);
    }
  }

  /**
   * Checks whether all players from the match did the announcing
   *
   * @param hands
   * @return
   */
  private boolean allPlayersAnnouncedScore(Collection<Hand> hands) {
    for (Hand el : hands) {
      if (el.getAnnouncedScore() == null) return false;
    }
    return true;
  }

  /**
   * Find all Hands in the repository which belongs to this match
   *
   * @param hand announced score from a user hand in a game
   * @return List from all hands
   */
  private Collection<Hand> getAllHandsFromMatch(Hand hand) {
    Collection<Hand> allHands = handRepository.findAll();
    for (Hand el : allHands) {
      if (el.getMatch() == hand.getMatch()) allHands.add(el);
    }
    return allHands;
  }
}
