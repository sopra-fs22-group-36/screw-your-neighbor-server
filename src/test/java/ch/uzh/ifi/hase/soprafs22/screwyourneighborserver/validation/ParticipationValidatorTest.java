package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Game;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.GameState;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Participation;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.web.client.HttpClientErrorException;

class ParticipationValidatorTest {
  private Game GAME_FINDING_PLAYERS;
  private Game GAME_ILLEGAL_STATE;

  private static final Participation PARTICIPATION = new Participation();
  private static final Participation PARTICIPATION_2 = new Participation();
  private static final Participation PARTICIPATION_3 = new Participation();

  private ParticipationValidator participationValidator;

  private Participation previousState;

  @BeforeEach
  void setup() {
    GAME_FINDING_PLAYERS = new Game();
    GAME_ILLEGAL_STATE = new Game();

    GAME_FINDING_PLAYERS.setGameState(GameState.FINDING_PLAYERS);
    GAME_ILLEGAL_STATE.setGameState(GameState.PLAYING);

    GAME_FINDING_PLAYERS.setId(1L);
    GAME_ILLEGAL_STATE.setId(2L);

    PARTICIPATION.setId(1L);
    PARTICIPATION_2.setId(2L);
    PARTICIPATION_3.setId(3L);

    OldStateFetcher oldStateFetcher = mock(OldStateFetcher.class);
    doAnswer(__ -> previousState).when(oldStateFetcher).getPreviousStateOf(notNull(), notNull());
    participationValidator = new ParticipationValidator(oldStateFetcher);
  }

  @Test
  void player_join_game_valid_game_state() {
    PARTICIPATION.setParticipationNumber(1);
    PARTICIPATION_2.setParticipationNumber(2);
    PARTICIPATION_3.setParticipationNumber(3);

    PARTICIPATION.setGame(GAME_FINDING_PLAYERS);
    PARTICIPATION_2.setGame(GAME_FINDING_PLAYERS);
    PARTICIPATION_3.setGame(GAME_FINDING_PLAYERS);

    assertDoesNotThrow(() -> participationValidator.onBeforeCreateParticipation(PARTICIPATION));
    assertDoesNotThrow(() -> participationValidator.onBeforeCreateParticipation(PARTICIPATION_2));
    assertDoesNotThrow(() -> participationValidator.onBeforeCreateParticipation(PARTICIPATION_3));

    previousState = new Participation();
    previousState.setActive(false);
    PARTICIPATION.setActive(true);

    assertDoesNotThrow(() -> participationValidator.onBeforeSaveParticipation(PARTICIPATION));
  }

  @Test
  void player_join_game_valid_game_state_then_start_game_after_one_illegal_join() {
    PARTICIPATION.setParticipationNumber(1);
    PARTICIPATION_2.setParticipationNumber(2);
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

    PARTICIPATION.setParticipationNumber(1);
    PARTICIPATION.setGame(GAME_ILLEGAL_STATE);

    assertThrows(
        HttpClientErrorException.class,
        () -> participationValidator.onBeforeCreateParticipation(PARTICIPATION));

    previousState = new Participation();
    previousState.setActive(false);
    PARTICIPATION.setActive(true);

    assertThrows(
        HttpClientErrorException.class,
        () -> participationValidator.onBeforeSaveParticipation(PARTICIPATION));
  }

  @Test
  void game_has_space_for_one_additional_player() {
    PARTICIPATION.setGame(GAME_FINDING_PLAYERS);

    GAME_FINDING_PLAYERS.setParticipations(
        Stream.generate(Participation::new)
            .limit(ParticipationValidator.MAX_NUMBER_OF_PLAYERS - 1)
            .collect(Collectors.toList()));
    PARTICIPATION.setGame(GAME_FINDING_PLAYERS);
    GAME_FINDING_PLAYERS.getParticipations().add(PARTICIPATION);

    assertDoesNotThrow(() -> participationValidator.onBeforeCreateParticipation(PARTICIPATION));

    previousState = new Participation();
    previousState.setActive(false);
    PARTICIPATION.setActive(true);

    assertDoesNotThrow(() -> participationValidator.onBeforeSaveParticipation(PARTICIPATION));
  }

  @Test
  void game_has_no_more_space_for_additional_players() {
    PARTICIPATION.setGame(GAME_FINDING_PLAYERS);

    GAME_FINDING_PLAYERS.setParticipations(
        Stream.generate(Participation::new)
            .limit(ParticipationValidator.MAX_NUMBER_OF_PLAYERS)
            .collect(Collectors.toList()));

    PARTICIPATION.setGame(GAME_FINDING_PLAYERS);
    GAME_FINDING_PLAYERS.getParticipations().add(PARTICIPATION);

    assertThrows(
        HttpClientErrorException.class,
        () -> participationValidator.onBeforeCreateParticipation(PARTICIPATION));

    previousState = new Participation();
    previousState.setActive(false);
    PARTICIPATION.setActive(true);

    assertThrows(
        HttpClientErrorException.class,
        () -> participationValidator.onBeforeSaveParticipation(PARTICIPATION));
  }

  @Test
  void onBeforeSaveParticipation_does_not_throw_when_active_flag_does_not_change() {
    previousState = PARTICIPATION;

    assertDoesNotThrow(() -> participationValidator.onBeforeSaveParticipation(PARTICIPATION));
  }

  @ParameterizedTest
  @EnumSource(GameState.class)
  void onBeforeSaveParticipation_always_allows_leaving_game(GameState gameState) {
    GAME_FINDING_PLAYERS.setGameState(gameState);
    PARTICIPATION.setGame(GAME_FINDING_PLAYERS);

    previousState = new Participation();
    previousState.setActive(true);
    PARTICIPATION.setActive(false);

    assertDoesNotThrow(() -> participationValidator.onBeforeSaveParticipation(PARTICIPATION));
  }
}
