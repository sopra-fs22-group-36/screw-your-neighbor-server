package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.api;

import static ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.SessionUtil.getSessionIdOf;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Game;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Participation;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Player;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.ClearDBAfterTestListener;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestExecutionListeners(
    value = {ClearDBAfterTestListener.class},
    mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class ParticipationIntegrationTest {

  @LocalServerPort private int port;

  private WebTestClient webTestClient;

  @Autowired private PlayerRepository playerRepository;
  @Autowired private GameRepository gameRepository;

  private static final Player PLAYER_1 = new Player();
  private static final Game GAME_1 = new Game();

  @BeforeEach
  public void setup() {
    webTestClient =
        WebTestClient.bindToServer()
            .responseTimeout(Duration.ofMinutes(1))
            .baseUrl("http://localhost:" + port)
            .build();

    PLAYER_1.setName("player1");
  }

  @Test
  public void join_existing_game() {
    HttpHeaders responseHeaders =
        webTestClient
            .post()
            .uri("/players")
            .body(BodyInserters.fromValue(PLAYER_1))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody()
            .returnResult()
            .getResponseHeaders();

    String sessionId = getSessionIdOf(responseHeaders);

    Player player = playerRepository.findAll().iterator().next();

    GAME_1.setName("game_1");
    gameRepository.saveAll(List.of(GAME_1));

    Participation participation = new Participation();
    participation.setGame(GAME_1);
    participation.setPlayer(player);

    webTestClient
        .post()
        .uri("/participations")
        .header(HttpHeaders.COOKIE, "JSESSIONID=%s".formatted(sessionId))
        .body(BodyInserters.fromValue(participation))
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody()
        .jsonPath("active")
        .isEqualTo(true);
  }
}
