package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.api;

import static ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.SessionUtil.getSessionIdOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Player;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.ClearDBAfterTestListener;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Streamable;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestExecutionListeners(
    value = {ClearDBAfterTestListener.class},
    mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class PlayerApiTest {
  private static final String ENDPOINT = "/players";

  private static final Player PLAYER_1 = new Player();
  private static final Player PLAYER_2 = new Player();

  @LocalServerPort private int port;

  @Autowired private PlayerRepository playerRepository;

  private WebTestClient webTestClient;

  @BeforeEach
  void setup() {
    webTestClient =
        WebTestClient.bindToServer()
            .responseTimeout(Duration.ofMinutes(1))
            .baseUrl(createBaseUrl())
            .build();

    PLAYER_1.setName("test");
    PLAYER_2.setName("test2");
  }

  @Test
  void get_all_players_is_empty_without_players() {
    webTestClient
        .get()
        .uri(ENDPOINT)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("_embedded.players")
        .value(is(emptyCollectionOf(Player.class)));
  }

  @Test
  void get_all_players_from_repository() {
    playerRepository.saveAll(List.of(PLAYER_1));
    playerRepository.saveAll(List.of(PLAYER_2));

    webTestClient
        .get()
        .uri(ENDPOINT)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("_embedded.players")
        .value(hasSize(2));
  }

  @Test
  void get_not_existing_player_fails() {
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
        .uri("%s%s/%s".formatted(createBaseUrl(), ENDPOINT, "1"))
        .header(HttpHeaders.COOKIE, "JSESSIONID=%s".formatted(sessionId))
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void create_a_player() {
    HttpHeaders headers =
        webTestClient
            .post()
            .uri(ENDPOINT)
            .body(BodyInserters.fromValue(Map.of("name", " %s \t\n".formatted(PLAYER_1.getName()))))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody()
            .jsonPath("name")
            .isEqualTo(PLAYER_1.getName())
            .returnResult()
            .getResponseHeaders();

    String sessionId = getSessionIdOf(headers);

    Iterable<Player> allPlayers = playerRepository.findAll();
    Player player = Streamable.of(allPlayers).stream().findFirst().orElseThrow();

    String playerUri = "%s%s/%s".formatted(createBaseUrl(), ENDPOINT, player.getId());
    webTestClient
        .get()
        .uri(playerUri)
        .header(HttpHeaders.COOKIE, "JSESSIONID=%s".formatted(sessionId))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("name")
        .isEqualTo(PLAYER_1.getName())
        .jsonPath("_links.self.href")
        .isEqualTo(playerUri);
  }

  @Test
  void create_a_player_with_too_short_name_fails() {
    PLAYER_1.setName("a");
    webTestClient
        .post()
        .uri(ENDPOINT)
        .body(BodyInserters.fromValue(PLAYER_1))
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  void create_2_players_with_the_same_session_fails() {
    HttpHeaders responseHeaders =
        webTestClient
            .post()
            .uri(ENDPOINT)
            .body(BodyInserters.fromValue(PLAYER_1))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody()
            .jsonPath("name")
            .isEqualTo(PLAYER_1.getName())
            .returnResult()
            .getResponseHeaders();

    String sessionId = getSessionIdOf(responseHeaders);

    webTestClient
        .post()
        .uri(ENDPOINT)
        .body(BodyInserters.fromValue(PLAYER_2))
        .header(HttpHeaders.COOKIE, "JSESSIONID=%s".formatted(sessionId))
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void patch_own_player() {
    HttpHeaders responseHeaders =
        webTestClient
            .post()
            .uri(ENDPOINT)
            .body(BodyInserters.fromValue(PLAYER_1))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody()
            .jsonPath("name")
            .isEqualTo(PLAYER_1.getName())
            .returnResult()
            .getResponseHeaders();

    String sessionId = getSessionIdOf(responseHeaders);

    Iterable<Player> allPlayers = playerRepository.findAll();
    Player player = Streamable.of(allPlayers).stream().findFirst().orElseThrow();
    String anotherName = "another name";
    PLAYER_1.setName(anotherName);

    webTestClient
        .patch()
        .uri("%s%s/%s".formatted(createBaseUrl(), ENDPOINT, player.getId()))
        .header(HttpHeaders.COOKIE, "JSESSIONID=%s".formatted(sessionId))
        .body(BodyInserters.fromValue(PLAYER_1))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("name")
        .isEqualTo(PLAYER_1.getName());

    allPlayers = playerRepository.findAll();
    player = Streamable.of(allPlayers).stream().findFirst().orElseThrow();

    assertThat(player.getName(), is(PLAYER_1.getName()));
  }

  @Test
  void patch_player_when_unauthorized_fails() {
    playerRepository.saveAll(List.of(PLAYER_1));

    PLAYER_1.setName("another name");
    Iterable<Player> allPlayers = playerRepository.findAll();
    Player player = Streamable.of(allPlayers).stream().findFirst().orElseThrow();

    webTestClient
        .patch()
        .uri("%s%s/%s".formatted(createBaseUrl(), ENDPOINT, player.getId()))
        .body(BodyInserters.fromValue(PLAYER_1))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void patch_other_player_fails() {
    playerRepository.saveAll(List.of(PLAYER_2));

    HttpHeaders responseHeaders =
        webTestClient
            .post()
            .uri(ENDPOINT)
            .body(BodyInserters.fromValue(PLAYER_1))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody()
            .jsonPath("name")
            .isEqualTo(PLAYER_1.getName())
            .returnResult()
            .getResponseHeaders();

    String sessionId = getSessionIdOf(responseHeaders);

    String anotherName = "another name";
    PLAYER_1.setName(anotherName);
    Iterable<Player> player2Collection =
        playerRepository.findAllByName(PLAYER_2.getName(), Pageable.unpaged());
    Player player = Streamable.of(player2Collection).stream().findFirst().orElseThrow();

    webTestClient
        .patch()
        .uri("%s%s/%s".formatted(createBaseUrl(), ENDPOINT, player.getId()))
        .header(HttpHeaders.COOKIE, "JSESSIONID=%s".formatted(sessionId))
        .body(BodyInserters.fromValue(PLAYER_1))
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  private String createBaseUrl() {
    return "http://localhost:" + port;
  }
}
