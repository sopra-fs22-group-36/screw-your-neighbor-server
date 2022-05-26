package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.validation;

import static ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.GameState.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.*;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Game;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.GameState;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Participation;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.web.client.HttpClientErrorException;

class GameValidatorTest {
  private static Game GAME_BEFORE;
  private static Game NEW_GAME;
  private GameValidator gameValidator;

  @BeforeEach
  void setup() {
    OldStateFetcher oldStateFetcher = mock(OldStateFetcher.class);
    doAnswer(__ -> GAME_BEFORE).when(oldStateFetcher).getPreviousStateOf(notNull(), notNull());

    gameValidator = new GameValidator(oldStateFetcher);

    GAME_BEFORE = new Game();
    GAME_BEFORE.setId(1L);
    NEW_GAME = new Game();
    NEW_GAME.setId(1L);
  }

  @ParameterizedTest
  @EnumSource(GameState.class)
  void accepts_changing_name(GameState gameState) {
    GAME_BEFORE.setGameState(gameState);
    NEW_GAME.setGameState(gameState);
    NEW_GAME.setName("another name");

    assertDoesNotThrow(() -> gameValidator.onUpdateGame(NEW_GAME));
  }

  @ParameterizedTest
  @CsvSource({"FINDING_PLAYERS, PLAYING", "FINDING_PLAYERS, CLOSED", "PLAYING, CLOSED"})
  void accept_valid_state_changes(GameState gameStateBefore, GameState newGameState) {
    GAME_BEFORE.setGameState(gameStateBefore);
    NEW_GAME.setGameState(newGameState);
    List<Participation> participations = List.of(new Participation(), new Participation());
    participations.forEach(participation -> participation.setActive(true));
    NEW_GAME.getParticipations().addAll(participations);

    assertDoesNotThrow(() -> gameValidator.onUpdateGame(NEW_GAME));
  }

  @ParameterizedTest
  @CsvSource({
    "PLAYING, FINDING_PLAYERS",
    "CLOSED, FINDING_PLAYERS",
    "CLOSED, PLAYING",
  })
  void throw_exception_on_invalid_state_changes(GameState gameStateBefore, GameState newGameState) {
    GAME_BEFORE.setGameState(gameStateBefore);
    NEW_GAME.setGameState(newGameState);

    assertThrows(HttpClientErrorException.class, () -> gameValidator.onUpdateGame(NEW_GAME));
  }

  @Test
  void throws_exception_when_starting_game_with_less_then_2_active_participations() {
    GAME_BEFORE.setGameState(FINDING_PLAYERS);
    NEW_GAME.setGameState(PLAYING);

    assertThrows(HttpClientErrorException.class, () -> gameValidator.onUpdateGame(NEW_GAME));

    Participation participation = new Participation();
    participation.setActive(true);
    NEW_GAME.getParticipations().add(participation);

    assertThrows(HttpClientErrorException.class, () -> gameValidator.onUpdateGame(NEW_GAME));

    Participation participation2 = new Participation();
    participation2.setActive(false);
    NEW_GAME.getParticipations().add(participation2);

    assertThrows(HttpClientErrorException.class, () -> gameValidator.onUpdateGame(NEW_GAME));
  }
}
