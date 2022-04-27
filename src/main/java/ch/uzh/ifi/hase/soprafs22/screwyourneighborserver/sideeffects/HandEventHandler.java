package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.sideeffects;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Hand;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Match;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.MatchState;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.MatchRepository;
import javax.transaction.Transactional;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler
@Transactional
public class HandEventHandler {

  private final MatchRepository matchRepository;

  public HandEventHandler(MatchRepository matchRepository) {
    this.matchRepository = matchRepository;
  }

  @SuppressWarnings("unused")
  @HandleAfterSave
  public void onAfterSave(Hand hand) {
    // find match by the hand
    Match match = hand.getMatch();

    if (allPlayersAnnouncedScore(match)) {
      match.setMatchState(MatchState.PLAYING);
      matchRepository.save(match);
    }
  }

  /**
   * At least one hand must part of the match, check whether all have announced
   *
   * @param match
   * @return
   */
  private boolean allPlayersAnnouncedScore(Match match) {
    if (match.getHands().stream().count() <= 0) return false;
    for (Hand hand : match.getHands()) {
      if (hand.getAnnouncedScore() == null) return false;
    }
    return true;
  }
}
