package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.api;

import static org.hamcrest.Matchers.*;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Player;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.PlayerRepository;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PlayerIntegrationTest {
  private static final ParameterizedTypeReference<List<Player>> PLAYER_LIST =
      new ParameterizedTypeReference<>() {};

  @LocalServerPort private int port;

  private WebTestClient webTestClient;

  @Autowired private PlayerRepository playerRepository;

  private static final Player PLAYER_1 = new Player();
  private static final Player PLAYER_2 = new Player();
  private static final Player PLAYER_3 = new Player();

  @BeforeEach
  public void setup() {
    webTestClient =
        WebTestClient.bindToServer()
            .responseTimeout(Duration.ofMinutes(1))
            .baseUrl("http://localhost:" + port)
            .build();

    playerRepository.deleteAll();
  }

  @Test
  public void return_found_all_players() {

    PLAYER_1.setName("My_first_Player");
    PLAYER_2.setName("My_second_Player");
    PLAYER_3.setName("My_third_Player");

    var playersList = List.of(PLAYER_1, PLAYER_2, PLAYER_3);
    playerRepository.saveAll(playersList);

    webTestClient
        .get()
        .uri("/players")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("_embedded.players")
        .value(hasSize(3));
  }

  private void assertTrue(boolean game_1) {}
}
