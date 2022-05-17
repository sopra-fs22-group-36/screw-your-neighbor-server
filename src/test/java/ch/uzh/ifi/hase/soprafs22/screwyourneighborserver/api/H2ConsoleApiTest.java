package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.api;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class H2ConsoleApiTest {
  @LocalServerPort private int port;

  @Value("${spring.h2.console.path}")
  private String h2ConsolePath;

  private WebTestClient webTestClient;

  @BeforeEach
  void setup() {
    webTestClient =
        WebTestClient.bindToServer()
            .responseTimeout(Duration.ofMinutes(1))
            .baseUrl("http://localhost:" + port)
            .build();
  }

  @Test
  void h2_console_is_available() {
    webTestClient.get().uri(h2ConsolePath).exchange().expectStatus().isFound();
  }
}
