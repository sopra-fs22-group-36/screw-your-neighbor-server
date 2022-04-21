package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.api;

import static ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.CardValue.*;
import static ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.SessionUtil.getSessionIdOf;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Game;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Player;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.ParticipationRepository;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.ClearDBAfterTestListener;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.GameBuilder;
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
class RoundIntegrationTest {

  @LocalServerPort private int port;

  private WebTestClient webTestClient;

  @Autowired private PlayerRepository playerRepository;
  @Autowired private ParticipationRepository participationRepository;
  @Autowired private GameRepository gameRepository;

  private static final String PLAYER_NAME_1 = "player1";
  private static final String PLAYER_NAME_2 = "player2";

  private Player player1;

  @BeforeEach
  void setup() {
    webTestClient =
        WebTestClient.bindToServer()
            .responseTimeout(Duration.ofMinutes(1))
            .baseUrl("http://localhost:" + port)
            .build();

    player1 = new Player();
    player1.setName(PLAYER_NAME_1);
  }

  @Test
  void return_correct_isStacked() {
    HttpHeaders responseHeaders =
        webTestClient
            .post()
            .uri("/players")
            .body(BodyInserters.fromValue(player1))
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
            .withCards(ACE_OF_CLUBS, SEVEN_OF_CLUBS, JACK_OF_CLUBS)
            .finishHand()
            .withHandForPlayer(PLAYER_NAME_2)
            .withCards(QUEEN_OF_CLUBS, ACE_OF_SPADES, QUEEN_OF_SPADES)
            .finishHand()
            .withRound()
            .withPlayedCard(PLAYER_NAME_1, ACE_OF_CLUBS)
            .withPlayedCard(PLAYER_NAME_2, ACE_OF_SPADES)
            .finishRound()
            .withRound()
            .withPlayedCard(PLAYER_NAME_1, SEVEN_OF_CLUBS)
            .withPlayedCard(PLAYER_NAME_2, QUEEN_OF_CLUBS)
            .finishRound()
            .withRound()
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
        .jsonPath("_embedded.matches[0].rounds[0].stacked")
        .isEqualTo(true)
        .jsonPath("_embedded.matches[0].rounds[1].stacked")
        .isEqualTo(false)
        .jsonPath("_embedded.matches[0].rounds[2].stacked")
        .isEqualTo(false);
  }
}
