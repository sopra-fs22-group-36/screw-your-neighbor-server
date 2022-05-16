package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Game;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.GameState;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Participation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.web.client.HttpClientErrorException;

class ParticipationValidatorTest {
  private static final Game GAME_FINDING_PLAYERS = new Game();
  private static final Game GAME_ILLEGAL_STATE = new Game();

  private static final Participation PARTICIPATION_1 = new Participation();
  private static final Participation PARTICIPATION_2 = new Participation();
  private static final Participation PARTICIPATION_3 = new Participation();
  private ParticipationValidator participationValidator;

  @BeforeEach
  void setup() {
    GAME_FINDING_PLAYERS.setGameState(GameState.FINDING_PLAYERS);
    GAME_ILLEGAL_STATE.setGameState(GameState.PLAYING);

    GAME_FINDING_PLAYERS.setId(1L);
    GAME_ILLEGAL_STATE.setId(2L);

    PARTICIPATION_1.setId(1L);
    PARTICIPATION_2.setId(2L);
    PARTICIPATION_3.setId(3L);

    participationValidator = new ParticipationValidator();
  }

  @Test
  void player_join_game_valid_game_state() {
    PARTICIPATION_1.setParticipationNumber(0);
    PARTICIPATION_2.setParticipationNumber(1);
    PARTICIPATION_3.setParticipationNumber(3);

    PARTICIPATION_1.setGame(GAME_FINDING_PLAYERS);
    PARTICIPATION_2.setGame(GAME_FINDING_PLAYERS);
    PARTICIPATION_3.setGame(GAME_FINDING_PLAYERS);

    assertDoesNotThrow(() -> participationValidator.onUpdateParticipation(PARTICIPATION_1));
    assertDoesNotThrow(() -> participationValidator.onUpdateParticipation(PARTICIPATION_2));
    assertDoesNotThrow(() -> participationValidator.onUpdateParticipation(PARTICIPATION_3));
  }

  @Test
  void player_join_game_valid_game_state_then_start_game_after_one_illegal_join() {
    PARTICIPATION_1.setParticipationNumber(0);
    PARTICIPATION_2.setParticipationNumber(1);
    PARTICIPATION_3.setParticipationNumber(3);

    PARTICIPATION_1.setGame(GAME_FINDING_PLAYERS);
    PARTICIPATION_2.setGame(GAME_FINDING_PLAYERS);

    assertDoesNotThrow(() -> participationValidator.onUpdateParticipation(PARTICIPATION_1));
    assertDoesNotThrow(() -> participationValidator.onUpdateParticipation(PARTICIPATION_2));

    GAME_FINDING_PLAYERS.setGameState(GameState.PLAYING);
    PARTICIPATION_3.setGame(GAME_FINDING_PLAYERS);
    assertThrows(
        HttpClientErrorException.class,
        () -> participationValidator.onUpdateParticipation(PARTICIPATION_3));
  }

  @ParameterizedTest
  @CsvSource({"CLOSED", "PLAYING"})
  void player_join_game_invalid_game_state(GameState invalidGameState) {
    GAME_ILLEGAL_STATE.setGameState(invalidGameState);

    PARTICIPATION_1.setParticipationNumber(0);
    PARTICIPATION_1.setGame(GAME_ILLEGAL_STATE);

    assertThrows(
        HttpClientErrorException.class,
        () -> participationValidator.onUpdateParticipation(PARTICIPATION_1));
  }
}
