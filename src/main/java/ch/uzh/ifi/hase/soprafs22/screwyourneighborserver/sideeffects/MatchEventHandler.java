package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.sideeffects;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.*;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.ScoreAnnouncementRepository;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler
public class MatchEventHandler {
  private final ScoreAnnouncementRepository scoreAnnRepo;

  public MatchEventHandler(ScoreAnnouncementRepository scoreAnnRepo) {
    this.scoreAnnRepo = scoreAnnRepo;
  }

  @SuppressWarnings("unsued")
  @HandleAfterCreate
  public void handleAfterSave(Match match) {
    if (match.getMatchState().equals(MatchState.ANNOUNCING)) {
      ScoreAnnouncement scoreAnn = new ScoreAnnouncement();
      scoreAnn.setAnnouncedScore(5);
      scoreAnnRepo.save(scoreAnn);
    }
  }
}
