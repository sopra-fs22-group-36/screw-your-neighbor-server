package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.controller;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HelloWorldControllerIntegrationTest {
  @LocalServerPort private int port;

  private WebTestClient webTestClient;

  @BeforeEach
  public void setup() {
    webTestClient =
        WebTestClient.bindToServer()
            .responseTimeout(Duration.ofMinutes(1))
            .baseUrl("http://localhost:" + port)
            .build();
  }

  @Test
  void return_200_for_get_root() {
    webTestClient.get().uri("/").exchange().expectStatus().isOk();
  }
}
