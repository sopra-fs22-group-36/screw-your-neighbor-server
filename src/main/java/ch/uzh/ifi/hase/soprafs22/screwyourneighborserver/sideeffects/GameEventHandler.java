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
public class GameEventHandler {
  private final ParticipationRepository participationRepository;
  private final ScoreAnnouncementRepository scoreAnnRepo;

  public GameEventHandler(
      ParticipationRepository participationRepository, ScoreAnnouncementRepository scoreAnnRepo) {
    this.participationRepository = participationRepository;
    this.scoreAnnRepo = scoreAnnRepo;
  }

  @SuppressWarnings("unused")
  @HandleAfterCreate
  public void onAfterCreate(Game game) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (!(authentication.getPrincipal() instanceof Player)) {
      throw new HttpClientErrorException(
          HttpStatus.UNAUTHORIZED, "Cannot create game when not authorized");
    }
    Player player = (Player) authentication.getPrincipal();
    Participation participation = new Participation();
    participation.setGame(game);
    participation.setPlayer(player);
    participation.setParticipationNumber(1);
    game.getParticipations().add(participation);
    participationRepository.save(participation);
  }

  @SuppressWarnings("unsued")
  @HandleAfterSave
  public void handleAfterSave(Game game) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (!(authentication.getPrincipal() instanceof Player)) {
      throw new HttpClientErrorException(
          HttpStatus.UNAUTHORIZED, "Cannot create game when not authorized");
    } else {
      if (game.getGameState().equals(GameState.PLAYING)) {
        ScoreAnnouncement scoreAnn = new ScoreAnnouncement();
        scoreAnn.setAnnouncedScore(5);
        scoreAnnRepo.save(scoreAnn);
      }
    }
  }
}
