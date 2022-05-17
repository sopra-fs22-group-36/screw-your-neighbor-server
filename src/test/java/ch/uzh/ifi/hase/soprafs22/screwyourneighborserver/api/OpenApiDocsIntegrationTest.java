package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.api;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenApiDocsIntegrationTest {
  @LocalServerPort private int port;

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
  void swagger_ui_is_available() {
    webTestClient.get().uri("/swagger-ui.html").exchange().expectStatus().isFound();
  }

  @Test
  void swagger_config_is_available() {
    webTestClient.get().uri("/v3/api-docs/swagger-config").exchange().expectStatus().isOk();
  }
}
