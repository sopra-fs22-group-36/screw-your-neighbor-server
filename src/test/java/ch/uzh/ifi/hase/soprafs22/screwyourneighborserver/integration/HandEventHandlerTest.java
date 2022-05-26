package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.integration;

import static ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.CardValue.*;
import static org.junit.jupiter.api.Assertions.*;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.*;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.MatchRepository;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.ParticipationRepository;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.sideeffects.HandEventHandler;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.ClearDBAfterTestListener;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.GameBuilder;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.web.client.HttpClientErrorException;

@SpringBootTest
@TestExecutionListeners(
    value = {ClearDBAfterTestListener.class},
    mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class HandEventHandlerTest {

  @Autowired private MatchRepository matchRepository;
  @Autowired private GameRepository gameRepository;
  @Autowired private ParticipationRepository participationRepository;

  @Autowired private PlayerRepository playerRepository;

  private static final String PLAYER_NAME_1 = "player1";
  private static final String PLAYER_NAME_2 = "player2";
  private static final String PLAYER_NAME_3 = "player3";

  private Match match;

  @Autowired private HandEventHandler handEventHandler;
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
  void play_one_card_with_announcing_score() {
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

    // Find match id from repository: hand must know which match belongs to
    Collection<Match> savedMatches = matchRepository.findAll();
    assertEquals(1, savedMatches.size());
    long test = savedMatches.iterator().next().getId();
    match.setId(test);
    matchRepository.save(match);

    Collection<Hand> hands = match.getHands();

    // Set announcement
    for (Hand hand : hands) {
      hand.setAnnouncedScore(1);
    }

    handEventHandler.onAfterSave(hands.iterator().next());

    // Start again find all element in the match repo
    savedMatches = matchRepository.findAll();
    assertFalse(savedMatches.stream().anyMatch(r -> r.getMatchState() == MatchState.ANNOUNCING));
    assertTrue(savedMatches.stream().anyMatch(r -> r.getMatchState() == MatchState.PLAYING));
    assertFalse(savedMatches.stream().anyMatch(r -> r.getMatchState() == MatchState.FINISH));
  }

  @Test
  void play_one_card_with_not_all_announcing_score() {
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

    // Find match id from repository: hand must know which match belongs to
    Collection<Match> savedMatches = matchRepository.findAll();
    assertEquals(1, savedMatches.size());
    long test = savedMatches.iterator().next().getId();
    match.setId(test);
    matchRepository.save(match);

    Collection<Hand> hands = match.getHands();

    // Hand from player 1
    int index = 0;
    for (Hand el : hands) {
      if (index < 2) {
        el.setAnnouncedScore(index);
        index++;
      }
    }

    handEventHandler.onAfterSave(hands.iterator().next());

    // Start again find all element in the match repo
    assertEquals(1, game.getMatches().size());
    assertEquals(3, match.getHands().size());
    assertTrue(savedMatches.stream().anyMatch(r -> r.getMatchState() == MatchState.ANNOUNCING));
    assertFalse(savedMatches.stream().anyMatch(r -> r.getMatchState() == MatchState.PLAYING));
    assertFalse(savedMatches.stream().anyMatch(r -> r.getMatchState() == MatchState.FINISH));
  }

  @Test
  void play_one_card_with_announcing_score_and_two_matches() {
    Game game =
        matchBuilder
            .withRound()
            .withPlayedCard(PLAYER_NAME_1, ACE_OF_CLUBS)
            .withPlayedCard(PLAYER_NAME_2, JACK_OF_CLUBS)
            .finishRound()
            .finishMatch()
            .build();
    game =
        matchBuilder
            .withRound()
            .withPlayedCard(PLAYER_NAME_1, ACE_OF_CLUBS)
            .withPlayedCard(PLAYER_NAME_2, JACK_OF_CLUBS)
            .finishRound()
            .finishMatch()
            .build();

    gameRepository.saveAll(List.of(game));
    Match firstMatch = game.getSortedMatches().stream().findFirst().get();
    Match lastMatch = game.getLastMatch().get();

    // Find match id from repository: hand must know which match belongs to
    List<Match> savedMatches = matchRepository.findAll();
    firstMatch.setId(savedMatches.get(0).getId());
    lastMatch.setId(savedMatches.get(1).getId());
    matchRepository.save(firstMatch);
    matchRepository.save(lastMatch);

    Collection<Hand> hands = lastMatch.getHands();

    // Set announcement
    for (Hand hand : hands) {
      hand.setAnnouncedScore(1);
    }

    handEventHandler.onAfterSave(hands.iterator().next());

    // Start again find all element in the match repo
    List<Match> actuelMatches = matchRepository.findAll();
    assertEquals(2, savedMatches.size());
    assertEquals(MatchState.ANNOUNCING, actuelMatches.get(0).getMatchState());
    assertEquals(MatchState.PLAYING, actuelMatches.get(1).getMatchState());
  }

  @Test
  void illegal_score_announcing() {
    Game game = matchBuilder.finishMatch().build();

    Iterable<Game> savedGames = gameRepository.saveAll(List.of(game));
    game = savedGames.iterator().next();
    match = game.getLastMatch().orElseThrow();

    List<Hand> hands = match.getSortedActiveHands();
    hands.forEach(el -> el.setAnnouncedScore(1));
    Hand lastHand = hands.get(hands.size() - 1);
    lastHand.setAnnouncedScore(0);

    assertThrows(
        HttpClientErrorException.class,
        () -> handEventHandler.onBeforeSave(lastHand),
        "Game rules prohibit this score announcement");
  }
}
