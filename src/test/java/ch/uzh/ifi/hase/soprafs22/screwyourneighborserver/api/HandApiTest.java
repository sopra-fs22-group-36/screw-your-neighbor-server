package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.api;

import static ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.CardValue.*;
import static ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.CardValue.JACK_OF_CLUBS;
import static ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.SessionUtil.getSessionIdOf;
import static org.hamcrest.Matchers.*;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.*;
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
class HandApiTest {

  @LocalServerPort private int port;

  private WebTestClient webTestClient;

  @Autowired private PlayerRepository playerRepository;
  @Autowired private ParticipationRepository participationRepository;
  @Autowired private GameRepository gameRepository;

  private static final String PLAYER_NAME_1 = "player1";
  private static final String PLAYER_NAME_2 = "player2";

  private Player PLAYER_1;

  @BeforeEach
  void setup() {
    webTestClient =
        WebTestClient.bindToServer()
            .responseTimeout(Duration.ofMinutes(1))
            .baseUrl("http://localhost:" + port)
            .build();

    PLAYER_1 = new Player();
    PLAYER_1.setName(PLAYER_NAME_1);
  }

  @Test
  void return_correct_turnActive_for_announcing_score() {
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
        GameBuilder.builder("game1", gameRepository, participationRepository, playerRepository)
            .withParticipationWith(createdPlayer)
            .withParticipation(PLAYER_NAME_2)
            .withGameState(GameState.PLAYING)
            .withMatch()
            .withMatchState(MatchState.ANNOUNCING)
            .withHandForPlayer(PLAYER_NAME_1)
            .withCards(ACE_OF_CLUBS, QUEEN_OF_CLUBS)
            .finishHand()
            .withHandForPlayer(PLAYER_NAME_2)
            .withCards(KING_OF_CLUBS, JACK_OF_CLUBS)
            .finishHand()
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
        .jsonPath("_embedded.matches[0].hands[0].participation.participationNumber")
        .isEqualTo(0)
        .jsonPath("_embedded.matches[0].hands[0].turnActive")
        .isEqualTo(true)
        .jsonPath("_embedded.matches[0].hands[1].participation.participationNumber")
        .isEqualTo(1)
        .jsonPath("_embedded.matches[0].hands[1].turnActive")
        .isEqualTo(false);
  }

  @Test
  void return_correct_turnActive_for_playing_card() {
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
        GameBuilder.builder("game1", gameRepository, participationRepository, playerRepository)
            .withParticipationWith(createdPlayer)
            .withParticipation(PLAYER_NAME_2)
            .withGameState(GameState.PLAYING)
            .withMatch()
            .withMatchState(MatchState.PLAYING)
            .withHandForPlayer(PLAYER_NAME_1)
            .withCards(ACE_OF_CLUBS, QUEEN_OF_CLUBS)
            .withAnnouncedScore(1)
            .finishHand()
            .withHandForPlayer(PLAYER_NAME_2)
            .withCards(KING_OF_CLUBS, JACK_OF_CLUBS)
            .withAnnouncedScore(0)
            .finishHand()
            .withRound()
            .withPlayedCard(PLAYER_NAME_1, ACE_OF_CLUBS)
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
        .jsonPath("_embedded.matches[0].hands[0].participation.participationNumber")
        .isEqualTo(0)
        .jsonPath("_embedded.matches[0].hands[0].turnActive")
        .isEqualTo(false)
        .jsonPath("_embedded.matches[0].hands[1].participation.participationNumber")
        .isEqualTo(1)
        .jsonPath("_embedded.matches[0].hands[1].turnActive")
        .isEqualTo(true);
  }

  @Test
  void return_correct_number_of_won_tricks_and_points_is_null() {
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
            .withCards(KING_OF_HEARTS, SEVEN_OF_CLUBS, JACK_OF_CLUBS)
            .withAnnouncedScore(1)
            .finishHand()
            .withHandForPlayer(PLAYER_NAME_2)
            .withAnnouncedScore(1)
            .withCards(QUEEN_OF_CLUBS, ACE_OF_SPADES, QUEEN_OF_SPADES)
            .finishHand()
            .withRound()
            .withPlayedCard(PLAYER_NAME_1, KING_OF_HEARTS)
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
        .jsonPath("_embedded.matches[0].hands[0].numberOfWonTricks")
        .isEqualTo(0)
        .jsonPath("_embedded.matches[0].hands[0].points")
        .value(nullValue())
        .jsonPath("_embedded.matches[0].hands[1].numberOfWonTricks")
        .isEqualTo(2)
        .jsonPath("_embedded.matches[0].hands[1].points")
        .value(nullValue());
  }

  @Test
  void return_correct_number_of_won_tricks_and_points() {
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
        .jsonPath("_embedded.matches[0].hands[0].numberOfWonTricks")
        .isEqualTo(0)
        .jsonPath("_embedded.matches[0].hands[0].points")
        .isEqualTo(-1)
        .jsonPath("_embedded.matches[0].hands[1].numberOfWonTricks")
        .isEqualTo(2)
        .jsonPath("_embedded.matches[0].hands[1].points")
        .isEqualTo(4);
  }

  @Test
  void return_correct_number_of_won_tricks_and_points_when_stacked() {
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
            .withCards(ACE_OF_CLUBS, QUEEN_OF_CLUBS)
            .withAnnouncedScore(2)
            .finishHand()
            .withHandForPlayer(PLAYER_NAME_2)
            .withCards(SEVEN_OF_CLUBS, ACE_OF_SPADES)
            .withAnnouncedScore(1)
            .finishHand()
            .withRound()
            .withPlayedCard(PLAYER_NAME_1, ACE_OF_CLUBS)
            .withPlayedCard(PLAYER_NAME_2, ACE_OF_SPADES)
            .finishRound()
            .withRound()
            .withPlayedCard(PLAYER_NAME_1, QUEEN_OF_CLUBS)
            .withPlayedCard(PLAYER_NAME_2, SEVEN_OF_CLUBS)
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
        .jsonPath("_embedded.matches[0].hands[0].numberOfWonTricks")
        .isEqualTo(2)
        .jsonPath("_embedded.matches[0].hands[0].points")
        .isEqualTo(4)
        .jsonPath("_embedded.matches[0].hands[1].numberOfWonTricks")
        .isEqualTo(0)
        .jsonPath("_embedded.matches[0].hands[1].points")
        .isEqualTo(-1);
  }
}
