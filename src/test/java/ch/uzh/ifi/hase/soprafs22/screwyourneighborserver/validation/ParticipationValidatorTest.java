package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Game;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.GameState;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Participation;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.web.client.HttpClientErrorException;

class ParticipationValidatorTest {
  private static final int MAX_NUMBER_OF_PLAYERS = 5;

  private static final Game GAME_FINDING_PLAYERS = new Game();
  private static final Game GAME_ILLEGAL_STATE = new Game();

  private static final Participation PARTICIPATION = new Participation();
  private static final Participation PARTICIPATION_2 = new Participation();
  private static final Participation PARTICIPATION_3 = new Participation();

  private ParticipationValidator participationValidator;

  @BeforeEach
  void setup() {
    GAME_FINDING_PLAYERS.setGameState(GameState.FINDING_PLAYERS);
    GAME_ILLEGAL_STATE.setGameState(GameState.PLAYING);

    GAME_FINDING_PLAYERS.setId(1L);
    GAME_ILLEGAL_STATE.setId(2L);

    PARTICIPATION.setId(1L);
    PARTICIPATION_2.setId(2L);
    PARTICIPATION_3.setId(3L);

    participationValidator = new ParticipationValidator();
  }

  @Test
  void player_join_game_valid_game_state() {
    PARTICIPATION.setParticipationNumber(0);
    PARTICIPATION_2.setParticipationNumber(1);
    PARTICIPATION_3.setParticipationNumber(3);

    PARTICIPATION.setGame(GAME_FINDING_PLAYERS);
    PARTICIPATION_2.setGame(GAME_FINDING_PLAYERS);
    PARTICIPATION_3.setGame(GAME_FINDING_PLAYERS);

    assertDoesNotThrow(() -> participationValidator.onBeforeCreateParticipation(PARTICIPATION));
    assertDoesNotThrow(() -> participationValidator.onBeforeCreateParticipation(PARTICIPATION_2));
    assertDoesNotThrow(() -> participationValidator.onBeforeCreateParticipation(PARTICIPATION_3));
  }

  @Test
  void player_join_game_valid_game_state_then_start_game_after_one_illegal_join() {
    PARTICIPATION.setParticipationNumber(0);
    PARTICIPATION_2.setParticipationNumber(1);
    PARTICIPATION_3.setParticipationNumber(3);

    PARTICIPATION.setGame(GAME_FINDING_PLAYERS);
    PARTICIPATION_2.setGame(GAME_FINDING_PLAYERS);

    assertDoesNotThrow(() -> participationValidator.onBeforeCreateParticipation(PARTICIPATION));
    assertDoesNotThrow(() -> participationValidator.onBeforeCreateParticipation(PARTICIPATION_2));

    GAME_FINDING_PLAYERS.setGameState(GameState.PLAYING);
    PARTICIPATION_3.setGame(GAME_FINDING_PLAYERS);
    assertThrows(
        HttpClientErrorException.class,
        () -> participationValidator.onBeforeCreateParticipation(PARTICIPATION_3));
  }

  @ParameterizedTest
  @CsvSource({"CLOSED", "PLAYING"})
  void player_join_game_invalid_game_state(GameState invalidGameState) {
    GAME_ILLEGAL_STATE.setGameState(invalidGameState);

    PARTICIPATION.setParticipationNumber(0);
    PARTICIPATION.setGame(GAME_ILLEGAL_STATE);

    assertThrows(
        HttpClientErrorException.class,
        () -> participationValidator.onBeforeCreateParticipation(PARTICIPATION));
  }

  @Test
  void game_has_space_for_one_additional_player() {
    PARTICIPATION.setGame(GAME_FINDING_PLAYERS);

    GAME_FINDING_PLAYERS.setParticipation(
        Stream.generate(Participation::new)
            .limit(MAX_NUMBER_OF_PLAYERS - 1)
            .collect(Collectors.toList()));
    assertDoesNotThrow(() -> participationValidator.onBeforeCreateParticipation(PARTICIPATION));
  }

  @Test
  void game_has_no_more_space_for_additional_players() {
    PARTICIPATION.setGame(GAME_FINDING_PLAYERS);

    GAME_FINDING_PLAYERS.setParticipation(
        Stream.generate(Participation::new)
            .limit(MAX_NUMBER_OF_PLAYERS)
            .collect(Collectors.toList()));

    assertThrows(
        HttpClientErrorException.class,
        () -> participationValidator.onBeforeCreateParticipation(PARTICIPATION));
  }
}
