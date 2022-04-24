package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.api;

import static ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.CardValue.*;
import static org.junit.jupiter.api.Assertions.*;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.*;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.*;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.sideeffects.CardEventHandler;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.sideeffects.HandEventHandler;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.ClearDBAfterTestListener;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.GameBuilder;
import java.util.Collection;
import java.util.Iterator;
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
class HandEventHandlerTest {

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
  private Hand hand;
  private Hand hand1;
  private Hand hand2;
  private Hand hand3;
  private Card card1;
  private Card card2;
  private Card card3;
  @Autowired private CardEventHandler cardEventHandler;
  @Autowired private HandEventHandler handEventHandler;
  private GameBuilder.MatchBuilder matchBuilder;
  private GameBuilder.HandBuilder handBuilder;

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
  void play_one_card_with_no_announcing_score() {
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
    Collection<Match> savedMatches = matchRepository.findAll();

    assertEquals(1, savedMatches.size());
    assertTrue(savedMatches.stream().anyMatch(r -> r.getMatchState() == MatchState.ANNOUNCING));
    assertFalse(savedMatches.stream().anyMatch(r -> r.getMatchState() == MatchState.PLAYING));
    assertFalse(savedMatches.stream().anyMatch(r -> r.getMatchState() == MatchState.FINISH));
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

    Collection<Hand> hands = handRepository.findAll();

    round = match.getLastRound().get();
    card1 = round.getCards().iterator().next();
    // Hand from all player
    for (Hand hand : hands) {
      hand.setAnnouncedScore(1);
      handRepository.save(hand);
    }

    handEventHandler.onAfterSave(hands.iterator().next());
    handEventHandler.onAfterSave(hands.iterator().next());
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

    Collection<Hand> savedHands = handRepository.findAll();
    Iterator<Hand> iterHands = savedHands.iterator();

    round = match.getLastRound().get();
    card1 = round.getCards().iterator().next();
    // Hand from player 1
    hand = iterHands.next();
    hand.setAnnouncedScore(1);
    handRepository.save(hand);

    hand = iterHands.next();
    hand.setAnnouncedScore(2);
    handRepository.save(hand);

    // Set one element null
    hand = iterHands.next();
    hand.setAnnouncedScore(null);
    handRepository.save(hand);

    savedHands = handRepository.findAll();
    iterHands = savedHands.iterator();

    handEventHandler.onAfterSave(iterHands.next());
    handEventHandler.onAfterSave(iterHands.next());
    handEventHandler.onAfterSave(iterHands.next());

    // Start again find all element in the match repo
    savedMatches = matchRepository.findAll();
    assertEquals(3, savedHands.stream().count());
    assertTrue(savedMatches.stream().anyMatch(r -> r.getMatchState() == MatchState.ANNOUNCING));
    assertFalse(savedMatches.stream().anyMatch(r -> r.getMatchState() == MatchState.PLAYING));
    assertFalse(savedMatches.stream().anyMatch(r -> r.getMatchState() == MatchState.FINISH));
  }
}
