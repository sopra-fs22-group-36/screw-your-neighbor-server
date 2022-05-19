package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.api;

import static ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.CardRank.*;
import static ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.CardSuit.*;
import static ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.CardValue.*;
import static ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.SessionUtil.getSessionIdOf;
import static org.hamcrest.Matchers.*;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.*;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.CardRepository;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.ParticipationRepository;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.ClearDBAfterTestListener;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.GameBuilder;
import java.time.Duration;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestExecutionListeners(
    value = {ClearDBAfterTestListener.class},
    mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class CardApiTest {
  @LocalServerPort private int port;

  private WebTestClient webTestClient;

  @Autowired private PlayerRepository playerRepository;
  @Autowired private ParticipationRepository participationRepository;
  @Autowired private GameRepository gameRepository;
  @Autowired CardRepository cardRepository;
  private static final String PLAYER_NAME_1 = "player1";
  private static final String PLAYER_NAME_2 = "player2";

  private Player PLAYER_1;
  private Player PLAYER_2;

  @BeforeEach
  void setup() {
    webTestClient =
        WebTestClient.bindToServer()
            .responseTimeout(Duration.ofMinutes(1))
            .baseUrl("http://localhost:" + port)
            .build();

    PLAYER_1 = new Player();
    PLAYER_1.setName(PLAYER_NAME_1);

    PLAYER_2 = new Player();
    PLAYER_2.setName(PLAYER_NAME_2);
  }

  @Test
  void hide_correct_cards() {
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

    String sessionIdPlayer1 = getSessionIdOf(responseHeaders);

    responseHeaders =
        webTestClient
            .post()
            .uri("/players")
            .body(BodyInserters.fromValue(PLAYER_2))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody()
            .returnResult()
            .getResponseHeaders();

    String sessionIdPlayer2 = getSessionIdOf(responseHeaders);

    Player player1 =
        playerRepository.findAll().stream()
            .filter(player -> player.getName().equals(PLAYER_NAME_1))
            .findFirst()
            .orElseThrow();

    Player player2 =
        playerRepository.findAll().stream()
            .filter(player -> player.getName().equals(PLAYER_NAME_2))
            .findFirst()
            .orElseThrow();

    Game game =
        GameBuilder.builder("game1", gameRepository, participationRepository, playerRepository)
            .withParticipationWith(player1)
            .withParticipationWith(player2)
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
            .withPlayedCard(PLAYER_NAME_1, ACE_OF_CLUBS)
            .finishRound()
            .finishMatch()
            .build();

    gameRepository.saveAll(List.of(game));

    String uri = "games/" + game.getId();

    webTestClient
        .get()
        .uri(uri)
        .header(HttpHeaders.COOKIE, "JSESSIONID=%s".formatted(sessionIdPlayer1))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("_embedded.matches[*].hands[*].cards[*].cardRank")
        .value(containsInAnyOrder(ACE.name(), QUEEN.name(), null, null))
        .jsonPath("_embedded.matches[*].hands[*].cards[*].cardSuit")
        .value(containsInAnyOrder(CLUB.name(), CLUB.name(), null, null))
        .jsonPath("_embedded.matches[*].rounds[0].cards[0].cardRank")
        .isEqualTo(ACE.name())
        .jsonPath("_embedded.matches[*].rounds[0].cards[0].cardSuit")
        .isEqualTo(CLUB.name());

    webTestClient
        .get()
        .uri(uri)
        .header(HttpHeaders.COOKIE, "JSESSIONID=%s".formatted(sessionIdPlayer2))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("_embedded.matches[*].hands[*].cards[*].cardRank")
        .value(containsInAnyOrder(ACE.name(), null, KING.name(), JACK.name()))
        .jsonPath("_embedded.matches[*].hands[*].cards[*].cardSuit")
        .value(containsInAnyOrder(CLUB.name(), null, CLUB.name(), CLUB.name()))
        .jsonPath("_embedded.matches[*].rounds[0].cards[0].cardRank")
        .isEqualTo(ACE.name())
        .jsonPath("_embedded.matches[*].rounds[0].cards[0].cardSuit")
        .isEqualTo(CLUB.name());
  }

  @Test
  void allows_playing_a_valid_card() {

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

    String sessionIdPlayer1 = getSessionIdOf(responseHeaders);

    Player player1 =
        playerRepository.findAll().stream()
            .filter(player -> player.getName().equals(PLAYER_NAME_1))
            .findFirst()
            .orElseThrow();

    Game game =
        GameBuilder.builder("game1", gameRepository, participationRepository, playerRepository)
            .withParticipationWith(player1)
            .withGameState(GameState.PLAYING)
            .withMatch()
            .withMatchState(MatchState.PLAYING)
            .withHandForPlayer(PLAYER_NAME_1)
            .withAnnouncedScore(1)
            .withCards(ACE_OF_CLUBS, QUEEN_OF_CLUBS)
            .finishHand()
            .withRound()
            .finishRound()
            .finishMatch()
            .build();

    game = gameRepository.saveAll(List.of(game)).get(0);
    Card card =
        cardRepository.findAll().stream()
            .filter(c -> c.getRound() == null)
            .filter(c -> c.getCardRank().equals(CardRank.ACE))
            .findAny()
            .orElseThrow();
    Round round = game.getLastMatch().orElseThrow().getLastRound().orElseThrow();

    String uri = "cards/" + card.getId().toString();
    webTestClient
        .patch()
        .uri(uri)
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.COOKIE, "JSESSIONID=%s".formatted(sessionIdPlayer1))
        .body(BodyInserters.fromValue(Map.of("round", "/rounds/%s".formatted(round.getId()))))
        .exchange()
        .expectStatus()
        .isOk();
  }
}
