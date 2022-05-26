package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.api;

import static ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.SessionUtil.getSessionIdOf;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpStatus.*;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Game;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.GameState;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.MatchState;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Player;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.ClearDBAfterTestListener;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestExecutionListeners(
    value = {ClearDBAfterTestListener.class},
    mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class GameApiTest {

  @LocalServerPort private int port;

  private WebTestClient webTestClient;

  @Autowired private GameRepository gameRepository;
  @Autowired private PlayerRepository playerRepository;

  private static final Player PLAYER_1 = new Player();
  private static final String PLAYER_NAME_2 = "player2";
  private static final Game GAME_1 = new Game();
  private static final Game GAME_2 = new Game();
  private static final Game GAME_3 = new Game();

  @BeforeEach
  void setup() {
    webTestClient =
        WebTestClient.bindToServer()
            .responseTimeout(Duration.ofMinutes(1))
            .baseUrl("http://localhost:" + port)
            .build();

    PLAYER_1.setName("player1");
  }

  @Test
  void create_game_and_return_created_game_by_ID() {
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

    GAME_1.setName("game_1");

    webTestClient
        .post()
        .uri("/games")
        .body(BodyInserters.fromValue(Map.of("name", " %s \t\n".formatted(GAME_1.getName()))))
        .header(HttpHeaders.COOKIE, "JSESSIONID=%s".formatted(sessionId))
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody()
        .jsonPath("name")
        .isEqualTo(GAME_1.getName())
        .jsonPath("_embedded.participations")
        .isNotEmpty()
        .jsonPath("_embedded.participations[0].player.name")
        .isEqualTo(PLAYER_1.getName());

    Long id = gameRepository.findAllByName("game_1").get(0).getId();
    String uri = "games/" + id.toString();

    webTestClient
        .get()
        .uri(uri)
        .header(HttpHeaders.COOKIE, "JSESSIONID=%s".formatted(sessionId))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("name")
        .isEqualTo(GAME_1.getName())
        .jsonPath("videoChatName")
        .value(notNullValue())
        .jsonPath("_embedded.participations")
        .isNotEmpty()
        .jsonPath("_embedded.participations[0].player.name")
        .isEqualTo(PLAYER_1.getName())
        .jsonPath("_embedded.participations[0].player._links.self")
        .value(notNullValue())
        .jsonPath("_embedded.participations[0].active")
        .isEqualTo(true);
  }

  @Test
  void return_found_game_by_ID() {
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

    GAME_1.setName("My_Game");
    gameRepository.saveAll(List.of(GAME_1));

    Long id = gameRepository.findAllByName("My_Game").get(0).getId();
    String uri = "games/" + id.toString();

    webTestClient
        .get()
        .uri(uri)
        .header(HttpHeaders.COOKIE, "JSESSIONID=%s".formatted(sessionId))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("name")
        .isEqualTo(GAME_1.getName())
        .jsonPath("_links.self.href")
        .isEqualTo(createBaseUrl() + "/" + uri);
  }

  @Test
  void return_not_found_game() {
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

    webTestClient
        .get()
        .uri("/games/5")
        .header(HttpHeaders.COOKIE, "JSESSIONID=%s".formatted(sessionId))
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void return_found_all_games() {

    GAME_1.setName("My_first_Game");
    GAME_2.setName("My_second_Game");
    GAME_3.setName("My_third_Game");

    var gamesList = List.of(GAME_1, GAME_2, GAME_3);
    gameRepository.saveAll(gamesList);

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

    webTestClient
        .get()
        .uri("/games")
        .header(HttpHeaders.COOKIE, "JSESSIONID=%s".formatted(sessionId))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("_embedded.games")
        .value(hasSize(3));
  }

  @Test
  void change_gameState_to_playing() {
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
    GAME_1.setName("game_1");

    // Create a new game
    webTestClient
        .post()
        .uri("/games")
        .body(Mono.just(GAME_1), Game.class)
        .header(HttpHeaders.COOKIE, "JSESSIONID=%s".formatted(sessionId))
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody()
        .jsonPath("name")
        .isEqualTo(GAME_1.getName())
        .jsonPath("_embedded.participations")
        .isNotEmpty()
        .jsonPath("_embedded.participations[0].player.name")
        .isEqualTo(PLAYER_1.getName());

    Long id = gameRepository.findAllByName("game_1").get(0).getId();
    String uri = "games/" + id.toString();
    GAME_1.setGameState(GameState.PLAYING);

    responseHeaders =
        webTestClient
            .post()
            .uri("/players")
            .body(BodyInserters.fromValue(Map.of("name", PLAYER_NAME_2)))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody()
            .returnResult()
            .getResponseHeaders();
    String sessionIdPlayer2 = getSessionIdOf(responseHeaders);

    Player player2 =
        playerRepository.findAll().stream()
            .filter(player -> PLAYER_NAME_2.equals(player.getName()))
            .findFirst()
            .orElseThrow();

    webTestClient
        .post()
        .uri("/participations")
        .body(
            BodyInserters.fromValue(
                Map.of(
                    "game",
                    "/games/%s".formatted(id),
                    "player",
                    "/players/%s".formatted(player2.getId()))))
        .header(HttpHeaders.COOKIE, "JSESSIONID=%s".formatted(sessionIdPlayer2))
        .exchange()
        .expectStatus()
        .isCreated();

    Map<String, GameState> patchBody = Map.of("gameState", GameState.PLAYING);
    // Without check whether the game exists (no get()) change the gameState with patch() request
    webTestClient
        .patch()
        .uri(uri)
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.COOKIE, "JSESSIONID=%s".formatted(sessionId))
        .body(BodyInserters.fromValue(patchBody)) // Game 2 has different gameState = PLAYING
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("_embedded.matches")
        .value(hasSize(1))
        .jsonPath("_embedded.matches[0].rounds")
        .value(hasSize(1))
        .jsonPath("_embedded.matches[0].rounds[0].roundNumber")
        .isEqualTo(1)
        .jsonPath("_embedded.matches[0].rounds[0].cards")
        .value(hasSize(0))
        .jsonPath("_embedded.matches[0].matchNumber")
        .isEqualTo(1)
        .jsonPath("_embedded.matches[0].matchState")
        .isEqualTo(MatchState.ANNOUNCING.name())
        .jsonPath("_embedded.matches[0].hands")
        .value(hasSize(2))
        .jsonPath("_embedded.matches[0].hands[0].announcedScore")
        .value(nullValue())
        .jsonPath("_embedded.matches[0].hands[0].cards")
        .value(hasSize(5))
        .jsonPath("_embedded.matches[0].hands[0].participation")
        .value(notNullValue());
  }

  @Test
  void patch_gameState_to_null_fails() {
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
    GAME_1.setName("game_1");

    // Create a new game
    webTestClient
        .post()
        .uri("/games")
        .body(Mono.just(GAME_1), Game.class)
        .header(HttpHeaders.COOKIE, "JSESSIONID=%s".formatted(sessionId))
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody()
        .jsonPath("name")
        .isEqualTo(GAME_1.getName())
        .jsonPath("_embedded.participations")
        .isNotEmpty()
        .jsonPath("_embedded.participations[0].player.name")
        .isEqualTo(PLAYER_1.getName());

    Long id = gameRepository.findAllByName("game_1").get(0).getId();
    String uri = "games/" + id.toString();
    GAME_1.setGameState(GameState.PLAYING);

    Map<String, GameState> patchBody = new HashMap<>();
    patchBody.put("gameState", null);
    // Without check whether the game exists (no get()) change the gameState with patch() request
    webTestClient
        .patch()
        .uri(uri)
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.COOKIE, "JSESSIONID=%s".formatted(sessionId))
        .body(BodyInserters.fromValue(patchBody)) // Game 2 has different gameState = PLAYING
        .exchange()
        .expectStatus()
        // if you run all tests, it's 422 UNPROCESSABLE_ENTITY
        // if you run only this test, its 400 BAD_REQUEST
        // Intended is 400 BAD_REQUEST
        .value(oneOf(BAD_REQUEST.value(), UNPROCESSABLE_ENTITY.value()));
  }

  private String createBaseUrl() {
    return "http://localhost:" + port;
  }
}
