package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.api;

import static ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.CardValue.*;
import static ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.CardValue.JACK_OF_CLUBS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.*;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.*;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.sideeffects.CardEventHandler;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.ClearDBAfterTestListener;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.GameBuilder;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;

@SpringBootTest
@TestExecutionListeners(
    value = {ClearDBAfterTestListener.class},
    mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class CardEventHandlerTest {

  @Autowired private RoundRepository roundRepository;
  @Autowired private MatchRepository matchRepository;
  @Autowired private CardRepository cardRepository;
  @Autowired private GameRepository gameRepository;
  @Autowired private HandRepository handRepository;
  @Autowired private ParticipationRepository participationRepository;

  @Autowired private PlayerRepository playerRepository;

  private static final String PLAYER_NAME_1 = "player1";
  private static final String PLAYER_NAME_2 = "player2";
  private static final String PLAYER_NAME_3 = "player3";

  private Participation participation1;
  private Participation participation2;
  private Participation participation3;
  private Game game;
  private Match match;
  private Round round;
  private Card card1;
  private Card card2;
  private Card card3;
  @Autowired private CardEventHandler cardEventHandler;
  private GameBuilder.MatchBuilder matchBuilder;

  @BeforeEach
  void setup() {
    matchBuilder =
        GameBuilder.builder("game1", gameRepository, participationRepository, playerRepository)
            .withParticipation(PLAYER_NAME_1)
            .withParticipation(PLAYER_NAME_2)
            .withParticipation(PLAYER_NAME_3)
            .withGameState(GameState.PLAYING)
            .withMatch()
            .withMatchState(MatchState.ANNOUNCING)
            .withHandForPlayer(PLAYER_NAME_1)
            .withCards(ACE_OF_CLUBS, QUEEN_OF_CLUBS)
            .finishHand()
            .withHandForPlayer(PLAYER_NAME_2)
            .withCards(KING_OF_CLUBS, JACK_OF_CLUBS)
            .finishHand()
            .withHandForPlayer(PLAYER_NAME_3)
            .withCards(QUEEN_OF_HEARTS, KING_OF_HEARTS)
            .finishHand();
  }

  @Test
  void play_first_card_no_new_round() {
    Game game =
        matchBuilder
            .withRound()
            .withPlayedCard(PLAYER_NAME_1, ACE_OF_CLUBS)
            .finishRound()
            .finishMatch()
            .build();

    gameRepository.saveAll(List.of(game));
    match = game.getLastMatch().get();
    round = match.getLastRound().get();
    card1 = round.getCards().iterator().next();
    cardEventHandler.handleAfterSave(card1);
    Collection<Round> savedRounds = roundRepository.findAll();

    assertEquals(1, savedRounds.size());
    assertTrue(savedRounds.stream().anyMatch(r -> r.getRoundNumber() == 1));
    assertFalse(savedRounds.stream().anyMatch(r -> r.getRoundNumber() == 2));
  }

  @Test
  void play_not_last_card_no_new_round() {
    Game game =
        matchBuilder
            .withRound()
            .withPlayedCard(PLAYER_NAME_1, ACE_OF_CLUBS)
            .withPlayedCard(PLAYER_NAME_2, JACK_OF_CLUBS)
            .finishRound()
            .finishMatch()
            .build();

    gameRepository.saveAll(List.of(game));
    match = game.getLastMatch().get();
    round = match.getLastRound().get();
    card1 = round.getCards().iterator().next();
    cardEventHandler.handleAfterSave(card1);
    Collection<Round> savedRounds = roundRepository.findAll();

    assertEquals(1, savedRounds.size());
    assertTrue(savedRounds.stream().anyMatch(r -> r.getRoundNumber() == 1));
    assertFalse(savedRounds.stream().anyMatch(r -> r.getRoundNumber() == 2));
  }

  @Test
  void play_last_card_new_round_no_new_match() {
    Game game =
        matchBuilder
            .withRound()
            .withPlayedCard(PLAYER_NAME_1, ACE_OF_CLUBS)
            .withPlayedCard(PLAYER_NAME_2, JACK_OF_CLUBS)
            .withPlayedCard(PLAYER_NAME_3, QUEEN_OF_HEARTS)
            .finishRound()
            .finishMatch()
            .build();

    Iterable<Game> savedGames = gameRepository.saveAll(List.of(game));
    match = savedGames.iterator().next().getLastMatch().get();
    round = match.getLastRound().get();
    card1 = round.getCards().iterator().next();
    Collection<Round> savedRoundsBefore = roundRepository.findAll();
    assertEquals(1, savedRoundsBefore.size());
    cardEventHandler.handleAfterSave(card1);
    Collection<Round> savedRoundsAfter = roundRepository.findAll();
    Collection<Match> savedMatches = matchRepository.findAll();

    assertEquals(2, savedRoundsAfter.size());
    assertTrue(savedRoundsAfter.stream().anyMatch(r -> r.getRoundNumber() == 1));
    assertTrue(savedRoundsAfter.stream().anyMatch(r -> r.getRoundNumber() == 2));
    assertFalse(savedMatches.stream().anyMatch(m -> m.getMatchNumber() == 2));
  }

  @Test
  void play_last_card_new_round_new_match() {
    Game game =
        matchBuilder
            .withRound()
            .withPlayedCard(PLAYER_NAME_1, ACE_OF_CLUBS)
            .withPlayedCard(PLAYER_NAME_2, JACK_OF_CLUBS)
            .withPlayedCard(PLAYER_NAME_3, QUEEN_OF_HEARTS)
            .finishRound()
            .withRound()
            .withPlayedCard(PLAYER_NAME_1, QUEEN_OF_CLUBS)
            .withPlayedCard(PLAYER_NAME_2, KING_OF_CLUBS)
            .withPlayedCard(PLAYER_NAME_3, KING_OF_HEARTS)
            .finishRound()
            .finishMatch()
            .build();

    Iterable<Game> savedGames = gameRepository.saveAll(List.of(game));
    match = savedGames.iterator().next().getLastMatch().get();
    round = match.getLastRound().get();
    card1 = round.getCards().iterator().next();
    Collection<Round> savedRounds1 = roundRepository.findAll();
    assertEquals(2, savedRounds1.size());
    cardEventHandler.handleAfterSave(card1);
    Collection<Round> savedRounds = roundRepository.findAll();
    Collection<Match> savedMatches = matchRepository.findAll();

    assertEquals(3, savedRounds.size());
    assertEquals(2, savedMatches.size());
    assertTrue(savedRounds.stream().anyMatch(r -> r.getRoundNumber() == 1));
    assertTrue(savedRounds.stream().anyMatch(r -> r.getRoundNumber() == 2));
    assertTrue(savedMatches.stream().anyMatch(m -> m.getMatchNumber() == 1));
    assertTrue(savedMatches.stream().anyMatch(m -> m.getMatchNumber() == 2));
  }
}
