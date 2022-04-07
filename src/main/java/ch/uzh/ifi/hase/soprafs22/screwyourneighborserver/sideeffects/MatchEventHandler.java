package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.sideeffects;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.*;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.ParticipationRepository;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.ScoreAnnouncementRepository;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication.getPrincipal() instanceof Player)) {
            throw new HttpClientErrorException(
                    HttpStatus.UNAUTHORIZED, "Cannot create game when not authorized");
        } else {
            if (match.getMatchState().equals(MatchState.ANNOUNCING)) {
                ScoreAnnouncement scoreAnn = new ScoreAnnouncement();
                scoreAnn.setAnnouncedScore(5);
                scoreAnnRepo.save(scoreAnn);
            }
        }
    }
}
