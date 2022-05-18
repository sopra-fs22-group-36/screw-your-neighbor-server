package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.integration;

import static ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.CardValue.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.*;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.*;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.ClearDBAfterTestListener;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.GameBuilder;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.WithPersistedPlayer;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.validation.CardValidator;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.validation.OldStateFetcher;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.web.client.HttpClientErrorException;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@SpringBootTest
@TestExecutionListeners(
    value = {ClearDBAfterTestListener.class},
    mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class CardValidatorTest {
  private static final String PLAYER_NAME_1 = "player1";
  private static final String PLAYER_NAME_2 = "player2";

  @Autowired private GameRepository gameRepository;
  @Autowired private ParticipationRepository participationRepository;

  @Autowired private PlayerRepository playerRepository;

  @Autowired private CardRepository cardRepository;

  private Game game;

  private Card CARD_BEFORE = new Card();

  private CardValidator cardValidator;

  @BeforeEach
  void setup() {

    OldStateFetcher oldStateFetcher = mock(OldStateFetcher.class);
    when(oldStateFetcher.getPreviousStateOf(notNull(), notNull())).thenReturn(CARD_BEFORE);
    cardValidator = new CardValidator(playerRepository, oldStateFetcher);
  }

  @Test
  @WithPersistedPlayer(playerName = PLAYER_NAME_1)
  void play_other_players_card_throws_error() {
    Player player = playerRepository.findByName(PLAYER_NAME_1).orElseThrow();
    game =
        GameBuilder.builder("game1", gameRepository, participationRepository, playerRepository)
            .withParticipationWith(player)
            .withParticipation(PLAYER_NAME_2)
            .withGameState(GameState.PLAYING)
            .withMatch()
            .withMatchState(MatchState.PLAYING)
            .withHandForPlayer(player.getName())
            .withCards(ACE_OF_CLUBS, QUEEN_OF_CLUBS)
            .withAnnouncedScore(0)
            .finishHand()
            .withHandForPlayer(PLAYER_NAME_2)
            .withCards(JACK_OF_SPADES, QUEEN_OF_HEARTS)
            .withAnnouncedScore(1)
            .finishHand()
            .withRound()
            .finishRound()
            .finishMatch()
            .build();

    gameRepository.saveAll(List.of(game));
    Card card =
        cardRepository.findAll().stream()
            .filter(c -> c.getCardRank() == CardRank.JACK)
            .findAny()
            .orElseThrow();
    Round round = game.getLastMatch().orElseThrow().getLastRound().orElseThrow();
    card.setRound(round);
    Exception exception =
        assertThrows(HttpClientErrorException.class, () -> cardValidator.onUpdateCard(card));
    assertEquals(
        "422 The card you're trying to play is not available in your hand.",
        exception.getMessage());
  }

  @Test
  @WithPersistedPlayer(playerName = PLAYER_NAME_1)
  void one_player_plays_multiple_cards_in_one_round_throws_error() {
    Player player = playerRepository.findByName(PLAYER_NAME_1).orElseThrow();

    game =
        GameBuilder.builder("game1", gameRepository, participationRepository, playerRepository)
            .withParticipationWith(player)
            .withGameState(GameState.PLAYING)
            .withMatch()
            .withMatchState(MatchState.PLAYING)
            .withHandForPlayer(player.getName())
            .withCards(ACE_OF_CLUBS, QUEEN_OF_CLUBS)
            .withAnnouncedScore(1)
            .finishHand()
            .withRound()
            .withPlayedCard(PLAYER_NAME_1, ACE_OF_CLUBS)
            .finishRound()
            .finishMatch()
            .build();

    gameRepository.saveAll(List.of(game));
    Hand hand =
        game.getLastMatch().orElseThrow().getHands().stream()
            .filter(
                h ->
                    h.getCards().stream().filter(c -> c.getCardRank() == CardRank.QUEEN).count()
                        > 0)
            .findAny()
            .orElseThrow();
    Card card =
        hand.getCards().stream()
            .filter(c -> c.getCardRank() == CardRank.QUEEN)
            .findAny()
            .orElseThrow();
    Round round = game.getLastMatch().orElseThrow().getLastRound().orElseThrow();
    CARD_BEFORE.setRound(round);
    CARD_BEFORE.setHand(hand);
    card.setId(1L);
    card.setRound(round);
    Exception exception =
        assertThrows(HttpClientErrorException.class, () -> cardValidator.onUpdateCard(card));
    assertEquals("422 It's not your turn.", exception.getMessage());
  }

  @Test
  @WithPersistedPlayer(playerName = PLAYER_NAME_1)
  void one_player_plays_same_card_again_throws_error() {
    Player player = playerRepository.findByName(PLAYER_NAME_1).orElseThrow();

    game =
        GameBuilder.builder("game1", gameRepository, participationRepository, playerRepository)
            .withParticipationWith(player)
            .withGameState(GameState.PLAYING)
            .withMatch()
            .withMatchState(MatchState.PLAYING)
            .withHandForPlayer(player.getName())
            .withCards(ACE_OF_CLUBS, QUEEN_OF_CLUBS)
            .withAnnouncedScore(1)
            .finishHand()
            .withRound()
            .withPlayedCard(PLAYER_NAME_1, ACE_OF_CLUBS)
            .finishRound()
            .withRound()
            .finishRound()
            .finishMatch()
            .build();

    gameRepository.saveAll(List.of(game));
    Hand hand =
        game.getLastMatch().orElseThrow().getHands().stream()
            .filter(
                h -> h.getCards().stream().filter(c -> c.getCardRank() == CardRank.ACE).count() > 0)
            .findAny()
            .orElseThrow();
    Card card =
        hand.getCards().stream()
            .filter(c -> c.getCardRank() == CardRank.ACE)
            .findAny()
            .orElseThrow();
    Round round = card.getRound();
    CARD_BEFORE.setRound(round);
    CARD_BEFORE.setHand(hand);
    card.setId(1L);
    card.setRound(round);
    Exception exception =
        assertThrows(HttpClientErrorException.class, () -> cardValidator.onUpdateCard(card));
    assertEquals("422 You already played this card", exception.getMessage());
  }

  @Test
  @WithPersistedPlayer(playerName = PLAYER_NAME_1)
  void playing_card_with_not_active_turn_throws_error() {
    Player player = playerRepository.findByName(PLAYER_NAME_1).orElseThrow();
    game =
        GameBuilder.builder("game1", gameRepository, participationRepository, playerRepository)
            .withParticipationWith(player)
            .withParticipation(PLAYER_NAME_2)
            .withGameState(GameState.PLAYING)
            .withMatch()
            .withMatchState(MatchState.PLAYING)
            .withHandForPlayer(player.getName())
            .withCards(ACE_OF_CLUBS, EIGHT_OF_CLUBS)
            .withAnnouncedScore(1)
            .finishHand()
            .withHandForPlayer(PLAYER_NAME_2)
            .withCards(JACK_OF_SPADES, QUEEN_OF_HEARTS)
            .withAnnouncedScore(0)
            .finishHand()
            .withRound()
            .finishRound()
            .finishMatch()
            .build();

    gameRepository.saveAll(List.of(game));
    Hand hand =
        game.getLastMatch().orElseThrow().getHands().stream()
            .filter(
                h ->
                    h.getCards().stream().filter(c -> c.getCardRank() == CardRank.EIGHT).count()
                        > 0)
            .findAny()
            .orElseThrow();
    Card card =
        hand.getCards().stream()
            .filter(c -> c.getCardRank() == CardRank.EIGHT)
            .findAny()
            .orElseThrow();
    Round round = game.getLastMatch().orElseThrow().getLastRound().orElseThrow();
    CARD_BEFORE.setRound(round);
    CARD_BEFORE.setHand(hand);
    card.setId(1L);
    card.setRound(round);
    Exception exception =
        assertThrows(HttpClientErrorException.class, () -> cardValidator.onUpdateCard(card));
    assertEquals("422 It's not your turn.", exception.getMessage());
  }

  @Test
  @WithPersistedPlayer(playerName = PLAYER_NAME_1)
  void playing_card_in_announcing_round_throws_error() {
    Player player = playerRepository.findByName(PLAYER_NAME_1).orElseThrow();
    game =
        GameBuilder.builder("game1", gameRepository, participationRepository, playerRepository)
            .withParticipationWith(player)
            .withParticipation(PLAYER_NAME_2)
            .withGameState(GameState.PLAYING)
            .withMatch()
            .withMatchState(MatchState.ANNOUNCING)
            .withHandForPlayer(player.getName())
            .withCards(ACE_OF_CLUBS, EIGHT_OF_CLUBS)
            .finishHand()
            .withHandForPlayer(PLAYER_NAME_2)
            .withCards(JACK_OF_SPADES, QUEEN_OF_HEARTS)
            .finishHand()
            .withRound()
            .finishRound()
            .finishMatch()
            .build();

    gameRepository.saveAll(List.of(game));
    Card card =
        game.getLastMatch().orElseThrow().getSortedHands().stream()
            .findAny()
            .orElseThrow()
            .getCards()
            .stream()
            .filter(c -> c.getCardRank() == CardRank.EIGHT)
            .findAny()
            .orElseThrow();
    Round round = game.getLastMatch().orElseThrow().getLastRound().orElseThrow();
    card.setRound(round);
    Exception exception =
        assertThrows(HttpClientErrorException.class, () -> cardValidator.onUpdateCard(card));
    assertEquals("422 You can not play a card in the announcing round.", exception.getMessage());
  }
}
