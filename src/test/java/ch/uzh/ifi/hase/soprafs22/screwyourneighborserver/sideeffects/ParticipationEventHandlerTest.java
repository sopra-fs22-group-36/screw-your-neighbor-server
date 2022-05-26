package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.sideeffects;

import static ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.GameState.*;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.*;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Game;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.GameState;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Participation;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Player;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.CardValue;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.GameBuilder;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.validation.OldStateFetcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ParticipationEventHandlerTest {

  private static final String PLAYER_NAME_1 = "player1";
  private static final String PLAYER_NAME_2 = "player2";
  private ParticipationEventHandler participationEventHandler;
  private HandEventHandler handEventHandler;
  private CardEventHandler cardEventHandler;
  private Participation previousParticipation;
  private Participation participation;
  private GameBuilder gameBuilder;

  @BeforeEach
  void setup() {
    OldStateFetcher oldStateFetcher = mock(OldStateFetcher.class);
    doAnswer(__ -> previousParticipation)
        .when(oldStateFetcher)
        .getPreviousStateOf(notNull(), notNull());
    handEventHandler = mock(HandEventHandler.class);
    cardEventHandler = mock(CardEventHandler.class);
    participationEventHandler =
        new ParticipationEventHandler(oldStateFetcher, handEventHandler, cardEventHandler);

    Player player = new Player();
    player.setName(PLAYER_NAME_1);

    previousParticipation = new Participation();
    previousParticipation.setPlayer(player);

    participation = new Participation();
    participation.setId(1L);
    participation.setPlayer(player);

    gameBuilder =
        GameBuilder.builder("game")
            .withGameState(PLAYING)
            .withParticipation(participation)
            .withParticipation(PLAYER_NAME_2);
  }

  @Test
  void does_nothing_when_active_state_does_not_change() {
    previousParticipation.setActive(true);
    participation.setActive(true);

    participationEventHandler.onBeforeSave(participation);

    previousParticipation.setActive(false);
    participation.setActive(false);

    participationEventHandler.onBeforeSave(participation);

    verifyNoInteractions(handEventHandler, cardEventHandler);
  }

  @Test
  void does_nothing_when_active_is_changed_to_true() {
    previousParticipation.setActive(false);
    participation.setActive(true);

    participationEventHandler.onBeforeSave(participation);

    verifyNoInteractions(handEventHandler, cardEventHandler);
  }

  @ParameterizedTest
  @CsvSource({"FINDING_PLAYERS, CLOSED"})
  void does_nothing_when_game_not_in_PLAYING_state(GameState gameState) {
    Game game = new Game();
    game.setGameState(gameState);
    game.getParticipations().add(participation);
    previousParticipation.setActive(true);
    previousParticipation.setGame(game);
    participation.setActive(false);
    participation.setGame(game);

    participationEventHandler.onBeforeSave(participation);

    verifyNoInteractions(handEventHandler, cardEventHandler);
  }

  @Test
  void calls_needed_handlers_when_player_leaves_game() {
    gameBuilder
        .withMatch()
        .withHandForPlayer(PLAYER_NAME_1)
        .withCards(CardValue.SIX_OF_HEARTS)
        .finishHand()
        .withHandForPlayer(PLAYER_NAME_2)
        .withCards(CardValue.SEVEN_OF_CLUBS)
        .finishHand()
        .withRound()
        .withPlayedCard(PLAYER_NAME_1, CardValue.SIX_OF_HEARTS)
        .finishRound()
        .finishMatch()
        .build();
    previousParticipation.setActive(true);
    participation.setActive(false);

    participationEventHandler.onBeforeSave(participation);

    verify(handEventHandler).onAfterSave(notNull());
    verify(cardEventHandler).handleAfterSave(notNull());
  }

  @Test
  void does_nothing_if_game_has_no_match() {
    gameBuilder.build();
    previousParticipation.setActive(true);
    participation.setActive(false);

    participationEventHandler.onBeforeSave(participation);

    verifyNoInteractions(handEventHandler, cardEventHandler);
  }

  @Test
  void does_nothing_if_game_has_no_hands() {
    gameBuilder.withMatch().finishMatch().build();
    previousParticipation.setActive(true);
    participation.setActive(false);

    participationEventHandler.onBeforeSave(participation);

    verifyNoInteractions(handEventHandler, cardEventHandler);
  }

  @Test
  void calls_handEventHandler_but_not_cardEventHandler_if_no_round() {
    gameBuilder
        .withMatch()
        .withHandForPlayer(PLAYER_NAME_1)
        .withCards(CardValue.SIX_OF_HEARTS)
        .finishHand()
        .withHandForPlayer(PLAYER_NAME_2)
        .withCards(CardValue.SEVEN_OF_CLUBS)
        .finishHand()
        .finishMatch()
        .build();
    previousParticipation.setActive(true);
    participation.setActive(false);

    participationEventHandler.onBeforeSave(participation);

    verify(handEventHandler).onAfterSave(notNull());
    verifyNoMoreInteractions(handEventHandler, cardEventHandler);
  }

  @Test
  void calls_needed_handlers_when_no_cards_were_played() {
    gameBuilder
        .withMatch()
        .withHandForPlayer(PLAYER_NAME_1)
        .withCards(CardValue.SIX_OF_HEARTS)
        .finishHand()
        .withHandForPlayer(PLAYER_NAME_2)
        .withCards(CardValue.SEVEN_OF_CLUBS)
        .finishHand()
        .withRound()
        .finishRound()
        .finishMatch()
        .build();
    previousParticipation.setActive(true);
    participation.setActive(false);

    participationEventHandler.onBeforeSave(participation);

    verify(handEventHandler).onAfterSave(notNull());
    verifyNoMoreInteractions(handEventHandler, cardEventHandler);
  }
}
