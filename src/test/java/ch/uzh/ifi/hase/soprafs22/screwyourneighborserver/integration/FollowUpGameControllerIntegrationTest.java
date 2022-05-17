package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.controller.FollowUpGameController;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Game;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.GameState;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Player;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.ParticipationRepository;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.ClearDBAfterTestListener;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.GameBuilder;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.WithPersistedPlayer;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.web.client.HttpClientErrorException;

@SpringBootTest
@TestExecutionListeners(
    value = {ClearDBAfterTestListener.class},
    mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class FollowUpGameControllerIntegrationTest {

  @Autowired private FollowUpGameController followUpGameController;
  @Autowired private PlayerRepository playerRepository;
  @Autowired private ParticipationRepository participationRepository;
  @Autowired private GameRepository gameRepository;
  private static final String PLAYER_NAME_1 = "player1";
  private static final String PLAYER_NAME_2 = "player2";

  private Long gameId;

  @BeforeEach
  void setup() {
    SecurityContext context = SecurityContextHolder.getContext();
    Object principal =
        Optional.ofNullable(context.getAuthentication())
            .map(Authentication::getPrincipal)
            .orElse(null);
    if (!(principal instanceof Player) || !PLAYER_NAME_1.equals(((Player) principal).getName())) {
      Player player = new Player();
      player.setName(PLAYER_NAME_1);
      playerRepository.saveAll(List.of(player));
      principal = player;
    }
    Player player1 = (Player) principal;

    Game game =
        GameBuilder.builder("game1", gameRepository, participationRepository, playerRepository)
            .withParticipationWith(player1)
            .withParticipation(PLAYER_NAME_2)
            .withGameState(GameState.CLOSED)
            .build();

    gameRepository.saveAll(List.of(game));
    gameId = game.getId();
  }

  @Test
  @WithPersistedPlayer(playerName = PLAYER_NAME_1)
  void throws_notFound_if_game_with_id_does_not_exist() {
    try {
      followUpGameController.createNextGame(gameId + 1);
      fail("no exception thrown");
    } catch (HttpClientErrorException e) {
      assertThat(e.getStatusCode(), is(NOT_FOUND));
    }

    assertThat(gameRepository.findAll(), hasSize(1));
  }

  @Test
  @WithPersistedPlayer(playerName = PLAYER_NAME_1)
  void throws_error_if_nextGame_already_exists() {
    followUpGameController.createNextGame(gameId);

    try {
      followUpGameController.createNextGame(gameId);
      fail("no exception thrown");
    } catch (HttpClientErrorException e) {
      assertThat(e.getStatusCode(), is(UNPROCESSABLE_ENTITY));
    }
    assertThat(gameRepository.findAll(), hasSize(2));
  }

  @Test
  @WithAnonymousUser
  void throws_if_not_authenticated() {
    assertThrows(AccessDeniedException.class, () -> followUpGameController.createNextGame(gameId));
  }

  @Test
  @WithPersistedPlayer(playerName = "another player")
  void throws_if_player_is_not_part_of_the_game() {
    assertThrows(AccessDeniedException.class, () -> followUpGameController.createNextGame(gameId));
  }
}
