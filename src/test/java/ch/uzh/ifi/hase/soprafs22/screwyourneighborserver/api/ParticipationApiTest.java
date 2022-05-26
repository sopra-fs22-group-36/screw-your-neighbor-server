package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.api;

import static ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.CardValue.*;
import static ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.CardValue.QUEEN_OF_CLUBS;
import static ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.SessionUtil.getSessionIdOf;
import static org.hamcrest.Matchers.*;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Game;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Player;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.ParticipationRepository;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.ClearDBAfterTestListener;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.GameBuilder;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestExecutionListeners(
    value = {ClearDBAfterTestListener.class},
    mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class ParticipationApiTest {

  @LocalServerPort private int port;

  private WebTestClient webTestClient;

  @Autowired private PlayerRepository playerRepository;
  @Autowired private GameRepository gameRepository;
  @Autowired private ParticipationRepository participationRepository;

  private static final String PLAYER_NAME_1 = "player1";
  private static final Player PLAYER_1 = new Player();
  private static final String PLAYER_NAME_2 = "player2";

  private static Game GAME_1;

  @BeforeEach
  void setup() {
    webTestClient =
        WebTestClient.bindToServer()
            .responseTimeout(Duration.ofMinutes(1))
            .baseUrl("http://localhost:" + port)
            .build();

    PLAYER_1.setName(PLAYER_NAME_1);
    GAME_1 = new Game();
  }

  @Test
  void join_existing_game() {
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

    webTestClient
        .post()
        .uri("/participations")
        .header(HttpHeaders.COOKIE, "JSESSIONID=%s".formatted(sessionId))
        .body(
            BodyInserters.fromValue(
                Map.of(
                    "game",
                    "/games/%s".formatted(GAME_1.getId()),
                    "player",
                    "/players/%s".formatted(player.getId()))))
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody()
        .jsonPath("active")
        .isEqualTo(true);
  }

  @Test
  void join_existing_full_game_fails() {
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

    GameBuilder gameBuilder =
        GameBuilder.builder("game", gameRepository, participationRepository, playerRepository);
    IntStream.range(0, 5).forEach(i -> gameBuilder.withParticipation("player" + i));
    GAME_1 = gameBuilder.build();
    GAME_1 = gameRepository.saveAll(List.of(GAME_1)).get(0);

    webTestClient
        .get()
        .uri("/participations")
        .header(HttpHeaders.COOKIE, "JSESSIONID=%s".formatted(sessionId))
        .exchange()
        .expectStatus()
        .isOk();

    webTestClient
        .post()
        .uri("/participations")
        .header(HttpHeaders.COOKIE, "JSESSIONID=%s".formatted(sessionId))
        .body(
            BodyInserters.fromValue(
                Map.of(
                    "game",
                    "/games/%s".formatted(GAME_1.getId()),
                    "player",
                    "/players/%s".formatted(player.getId()))))
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
  }

  @Test
  void return_correct_number_of_points() {
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
    Player createdPlayer = playerRepository.findAll().iterator().next();

    Game game =
        GameBuilder.builder("test", gameRepository, participationRepository, playerRepository)
            .withParticipationWith(createdPlayer)
            .withParticipation(PLAYER_NAME_2)
            .withMatch()
            .withHandForPlayer(PLAYER_NAME_1)
            .withCards(KING_OF_HEARTS, SEVEN_OF_CLUBS)
            .withAnnouncedScore(1)
            .finishHand()
            .withHandForPlayer(PLAYER_NAME_2)
            .withCards(QUEEN_OF_CLUBS, ACE_OF_SPADES)
            .withAnnouncedScore(2)
            .finishHand()
            .withRound()
            .withPlayedCard(PLAYER_NAME_1, KING_OF_HEARTS)
            .withPlayedCard(PLAYER_NAME_2, ACE_OF_SPADES)
            .finishRound()
            .withRound()
            .withPlayedCard(PLAYER_NAME_1, SEVEN_OF_CLUBS)
            .withPlayedCard(PLAYER_NAME_2, QUEEN_OF_CLUBS)
            .finishRound()
            .finishMatch()
            .build();

    gameRepository.saveAll(List.of(game));

    String uri = "games/" + game.getId();

    webTestClient
        .get()
        .uri(uri)
        .header(HttpHeaders.COOKIE, "JSESSIONID=%s".formatted(sessionId))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("_embedded.participations[*].points")
        .value(containsInAnyOrder(-1, 4));
  }
}
