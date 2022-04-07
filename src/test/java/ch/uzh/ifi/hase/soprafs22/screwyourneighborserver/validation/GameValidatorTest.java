package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Game;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.web.client.HttpClientErrorException;

class GameValidatorTest {
  private static final Game GAME_BEFORE = new Game();
  private static final Game NEW_GAME = new Game();
  private GameValidator gameValidator;

  @BeforeEach
  public void setup() {
    OldStateFetcher oldStateFetcher = mock(OldStateFetcher.class);
    when(oldStateFetcher.getPreviousStateOf(notNull(), notNull())).thenReturn(GAME_BEFORE);

    gameValidator = new GameValidator(oldStateFetcher);

    GAME_BEFORE.setId(1L);
    NEW_GAME.setId(1L);
  }

  @ParameterizedTest
  @CsvSource({"FINDING_PLAYERS, PLAYING", "FINDING_PLAYERS, CLOSED", "PLAYING, CLOSED"})
  public void accept_valid_state_changes(GameState gameStateBefore, GameState newGameState) {
    GAME_BEFORE.setGameState(gameStateBefore);
    NEW_GAME.setGameState(newGameState);

    assertDoesNotThrow(() -> gameValidator.onUpdateGame(NEW_GAME));
  }

  @ParameterizedTest
  @CsvSource({
    "PLAYING, FINDING_PLAYERS",
    "CLOSED, FINDING_PLAYERS",
    "CLOSED, PLAYING",
  })
  public void throw_exception_on_invalid_state_changes(
      GameState gameStateBefore, GameState newGameState) {
    GAME_BEFORE.setGameState(gameStateBefore);
    NEW_GAME.setGameState(newGameState);

    assertThrows(HttpClientErrorException.class, () -> gameValidator.onUpdateGame(NEW_GAME));
  }
}
