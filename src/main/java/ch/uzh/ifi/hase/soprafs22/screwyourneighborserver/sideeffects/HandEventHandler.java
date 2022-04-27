package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.sideeffects;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Hand;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Match;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.MatchState;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.HandRepository;
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
  private final HandRepository handRepository;

  public HandEventHandler(MatchRepository matchRepository, HandRepository handRepository) {
    this.matchRepository = matchRepository;
    this.handRepository = handRepository;
  }

  @SuppressWarnings("unused")
  @HandleAfterSave
  public void onAfterSave(Hand hand) {
    Match match = hand.getMatch();

    if (allPlayersAnnouncedScore(match)) {
      match.setMatchState(MatchState.PLAYING);
      matchRepository.save(match);
    }
  }

  /**
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
