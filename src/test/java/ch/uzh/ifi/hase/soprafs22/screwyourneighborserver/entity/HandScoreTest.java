package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import static ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.CardValue.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.ParticipationRepository;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.ClearDBAfterTestListener;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.GameBuilder;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestExecutionListeners;

/** Needs a DataJpaTest, because the cards need ids */
@DataJpaTest
@TestExecutionListeners(
    value = {ClearDBAfterTestListener.class},
    mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class HandScoreTest {

  private static final String PLAYER_1 = "player1";
  private static final int ANNOUNCED_SCORE_PLAYER_1 = 1;
  private static final String PLAYER_2 = "player2";
  private static final int ANNOUNCED_SCORE_PLAYER_2 = 0;
  private static final String PLAYER_3 = "player3";
  private static final int ANNOUNCED_SCORE_PLAYER_3 = 3;

  @Autowired private GameRepository gameRepository;
  @Autowired private ParticipationRepository participationRepository;
  @Autowired private PlayerRepository playerRepository;
  private GameBuilder.MatchBuilder matchBuilder;
  private GameBuilder.MatchBuilder matchBuilderWithHands;

  @BeforeEach
  void setup() {
    matchBuilder =
        GameBuilder.builder("test", gameRepository, participationRepository, playerRepository)
            .withParticipation(PLAYER_1)
            .withParticipation(PLAYER_2)
            .withParticipation(PLAYER_3)
            .withMatch();

    matchBuilderWithHands =
        matchBuilder
            .withHandForPlayer(PLAYER_1)
            .withCards(ACE_OF_CLUBS, SEVEN_OF_CLUBS, JACK_OF_CLUBS)
            .withAnnouncedScore(ANNOUNCED_SCORE_PLAYER_1)
            .finishHand()
            .withHandForPlayer(PLAYER_2)
            .withCards(KING_OF_CLUBS, JACK_OF_SPADES, EIGHT_OF_CLUBS)
            .withAnnouncedScore(ANNOUNCED_SCORE_PLAYER_2)
            .finishHand()
            .withHandForPlayer(PLAYER_3)
            .withCards(QUEEN_OF_CLUBS, ACE_OF_SPADES, QUEEN_OF_SPADES)
            .withAnnouncedScore(ANNOUNCED_SCORE_PLAYER_3)
            .finishHand();
  }

  @Test
  void is_0_and_null_when_no_rounds_and_empty_hands() {
    Game game =
        matchBuilder
            .withHandForPlayer(PLAYER_1)
            .finishHand()
            .withHandForPlayer(PLAYER_2)
            .finishHand()
            .withHandForPlayer(PLAYER_3)
            .finishHand()
            .finishMatch()
            .build();

    gameRepository.saveAll(List.of(game));

    Match match = game.getSortedMatches().get(0);

    assertTrue(
        match.getHands().stream()
            .map(Hand::getNumberOfWonTricks)
            .allMatch(wonTricks -> wonTricks == 0));

    assertTrue(match.getHands().stream().map(Hand::getPoints).allMatch(Objects::isNull));
  }

  @Test
  void is_0_and_null_when_no_rounds_but_cards() {
    Game game = matchBuilderWithHands.finishMatch().build();

    gameRepository.saveAll(List.of(game));

    Match match = game.getSortedMatches().get(0);

    assertTrue(
        match.getHands().stream()
            .map(Hand::getNumberOfWonTricks)
            .allMatch(wonTricks -> wonTricks == 0));

    assertTrue(match.getHands().stream().map(Hand::getPoints).allMatch(Objects::isNull));
  }

  @Test
  void is_0_and_null_when_rounds_but_no_cards() {
    Game game =
        matchBuilder
            .withHandForPlayer(PLAYER_1)
            .finishHand()
            .withHandForPlayer(PLAYER_2)
            .finishHand()
            .withHandForPlayer(PLAYER_3)
            .finishHand()
            .withRound()
            .finishRound()
            .finishMatch()
            .build();

    gameRepository.saveAll(List.of(game));

    Match match = game.getSortedMatches().get(0);

    assertTrue(
        match.getHands().stream()
            .map(Hand::getNumberOfWonTricks)
            .allMatch(wonTricks -> wonTricks == 0));

    assertTrue(match.getHands().stream().map(Hand::getPoints).allMatch(Objects::isNull));
  }

  @Test
  void evaluates_score() {
    Game game =
        matchBuilderWithHands
            .withRound()
            .withPlayedCard(PLAYER_1, ACE_OF_CLUBS)
            .withPlayedCard(PLAYER_2, KING_OF_CLUBS)
            .withPlayedCard(PLAYER_3, QUEEN_OF_CLUBS)
            .finishRound()
            .withRound()
            .withPlayedCard(PLAYER_1, SEVEN_OF_CLUBS)
            .withPlayedCard(PLAYER_2, JACK_OF_SPADES)
            .withPlayedCard(PLAYER_3, ACE_OF_SPADES)
            .finishRound()
            .withRound()
            .withPlayedCard(PLAYER_1, JACK_OF_CLUBS)
            .withPlayedCard(PLAYER_2, EIGHT_OF_CLUBS)
            .withPlayedCard(PLAYER_3, QUEEN_OF_SPADES)
            .finishRound()
            .finishMatch()
            .build();

    game.getLastMatch()
        .flatMap(Match::getLastRound)
        .ifPresent(lastRound -> lastRound.setRoundNumber(9));
    gameRepository.saveAll(List.of(game));

    Match match = game.getSortedMatches().get(0);
    List<Hand> sortedHands =
        match.getHands().stream()
            .sorted(Comparator.comparing(hand -> hand.getParticipation().getParticipationNumber()))
            .collect(Collectors.toList());
    Hand handPlayer1 = sortedHands.get(0);
    Hand handPlayer2 = sortedHands.get(1);
    Hand handPlayer3 = sortedHands.get(2);

    assertThat(handPlayer1.getNumberOfWonTricks(), is(ANNOUNCED_SCORE_PLAYER_1));
    assertThat(handPlayer1.getPoints(), is(ANNOUNCED_SCORE_PLAYER_1 * ANNOUNCED_SCORE_PLAYER_1));

    assertThat(handPlayer2.getNumberOfWonTricks(), is(ANNOUNCED_SCORE_PLAYER_2));
    assertThat(handPlayer2.getPoints(), is(ANNOUNCED_SCORE_PLAYER_2));

    assertThat(handPlayer3.getNumberOfWonTricks(), is(2));
    assertThat(handPlayer3.getPoints(), is(-Math.abs(ANNOUNCED_SCORE_PLAYER_3 - 2)));
  }

  @Test
  void evaluates_score_with_multiple_highest_cards() {
    Game game =
        matchBuilderWithHands
            .withRound()
            .withPlayedCard(PLAYER_1, ACE_OF_CLUBS)
            .withPlayedCard(PLAYER_2, KING_OF_CLUBS)
            .withPlayedCard(PLAYER_3, ACE_OF_SPADES)
            .finishRound()
            .withRound()
            .withPlayedCard(PLAYER_1, SEVEN_OF_CLUBS)
            .withPlayedCard(PLAYER_2, JACK_OF_SPADES)
            .withPlayedCard(PLAYER_3, QUEEN_OF_CLUBS)
            .finishRound()
            .withRound()
            .withPlayedCard(PLAYER_1, JACK_OF_CLUBS)
            .withPlayedCard(PLAYER_2, EIGHT_OF_CLUBS)
            .withPlayedCard(PLAYER_3, QUEEN_OF_SPADES)
            .finishRound()
            .finishMatch()
            .build();

    gameRepository.saveAll(List.of(game));

    Match match = game.getSortedMatches().get(0);
    List<Hand> sortedHands =
        match.getHands().stream()
            .sorted(Comparator.comparing(hand -> hand.getParticipation().getParticipationNumber()))
            .collect(Collectors.toList());
    Hand handPlayer1 = sortedHands.get(0);
    Hand handPlayer2 = sortedHands.get(1);
    Hand handPlayer3 = sortedHands.get(2);

    assertThat(handPlayer1.getNumberOfWonTricks(), is(0));
    assertThat(handPlayer1.getPoints(), is(-ANNOUNCED_SCORE_PLAYER_1));

    assertThat(handPlayer2.getNumberOfWonTricks(), is(ANNOUNCED_SCORE_PLAYER_2));
    assertThat(handPlayer2.getPoints(), is(ANNOUNCED_SCORE_PLAYER_2));

    assertThat(handPlayer3.getNumberOfWonTricks(), is(ANNOUNCED_SCORE_PLAYER_3));
    assertThat(handPlayer3.getPoints(), is(ANNOUNCED_SCORE_PLAYER_3 * ANNOUNCED_SCORE_PLAYER_3));
  }

  @Test
  void evaluates_score_with_two_stacked_round() {
    matchBuilder =
        GameBuilder.builder("test", gameRepository, participationRepository, playerRepository)
            .withParticipation(PLAYER_1)
            .withParticipation(PLAYER_2)
            .withParticipation(PLAYER_3)
            .withMatch();

    matchBuilderWithHands =
        matchBuilder
            .withHandForPlayer(PLAYER_1)
            .withCards(ACE_OF_CLUBS, QUEEN_OF_CLUBS, JACK_OF_CLUBS)
            .withAnnouncedScore(ANNOUNCED_SCORE_PLAYER_1)
            .finishHand()
            .withHandForPlayer(PLAYER_2)
            .withCards(KING_OF_CLUBS, JACK_OF_SPADES, EIGHT_OF_CLUBS)
            .withAnnouncedScore(ANNOUNCED_SCORE_PLAYER_2)
            .finishHand()
            .withHandForPlayer(PLAYER_3)
            .withCards(SEVEN_OF_CLUBS, ACE_OF_SPADES, QUEEN_OF_SPADES)
            .withAnnouncedScore(ANNOUNCED_SCORE_PLAYER_3)
            .finishHand();
    Game game =
        matchBuilderWithHands
            .withRound()
            .withPlayedCard(PLAYER_1, ACE_OF_CLUBS)
            .withPlayedCard(PLAYER_2, KING_OF_CLUBS)
            .withPlayedCard(PLAYER_3, ACE_OF_SPADES)
            .finishRound()
            .withRound()
            .withPlayedCard(PLAYER_1, QUEEN_OF_CLUBS)
            .withPlayedCard(PLAYER_2, JACK_OF_SPADES)
            .withPlayedCard(PLAYER_3, SEVEN_OF_CLUBS)
            .finishRound()
            .withRound()
            .withPlayedCard(PLAYER_1, JACK_OF_CLUBS)
            .withPlayedCard(PLAYER_2, EIGHT_OF_CLUBS)
            .withPlayedCard(PLAYER_3, QUEEN_OF_SPADES)
            .finishRound()
            .finishMatch()
            .build();

    gameRepository.saveAll(List.of(game));

    Match match = game.getSortedMatches().get(0);
    List<Hand> sortedHands =
        match.getHands().stream()
            .sorted(Comparator.comparing(hand -> hand.getParticipation().getParticipationNumber()))
            .collect(Collectors.toList());
    Hand handPlayer1 = sortedHands.get(0);
    Hand handPlayer2 = sortedHands.get(1);
    Hand handPlayer3 = sortedHands.get(2);

    assertThat(handPlayer1.getNumberOfWonTricks(), is(2));
    assertThat(handPlayer2.getNumberOfWonTricks(), is(0));
    assertThat(handPlayer3.getNumberOfWonTricks(), is(1));
  }

  @Test
  void evaluates_score_with_one_stacked_round() {
    matchBuilder =
        GameBuilder.builder("test", gameRepository, participationRepository, playerRepository)
            .withParticipation(PLAYER_1)
            .withParticipation(PLAYER_2)
            .withParticipation(PLAYER_3)
            .withMatch();

    matchBuilderWithHands =
        matchBuilder
            .withHandForPlayer(PLAYER_1)
            .withCards(ACE_OF_CLUBS, QUEEN_OF_CLUBS, JACK_OF_CLUBS)
            .withAnnouncedScore(ANNOUNCED_SCORE_PLAYER_1)
            .finishHand()
            .withHandForPlayer(PLAYER_2)
            .withCards(KING_OF_CLUBS, JACK_OF_SPADES, EIGHT_OF_CLUBS)
            .withAnnouncedScore(ANNOUNCED_SCORE_PLAYER_2)
            .finishHand()
            .withHandForPlayer(PLAYER_3)
            .withCards(SEVEN_OF_CLUBS, ACE_OF_SPADES, QUEEN_OF_SPADES)
            .withAnnouncedScore(ANNOUNCED_SCORE_PLAYER_3)
            .finishHand();
    Game game =
        matchBuilderWithHands
            .withRound()
            .withPlayedCard(PLAYER_1, ACE_OF_CLUBS)
            .withPlayedCard(PLAYER_2, KING_OF_CLUBS)
            .withPlayedCard(PLAYER_3, ACE_OF_SPADES)
            .finishRound()
            .withRound()
            .withPlayedCard(PLAYER_1, QUEEN_OF_CLUBS)
            .withPlayedCard(PLAYER_2, JACK_OF_SPADES)
            .withPlayedCard(PLAYER_3, SEVEN_OF_CLUBS)
            .finishRound()
            .withRound()
            .withPlayedCard(PLAYER_1, JACK_OF_CLUBS)
            .withPlayedCard(PLAYER_2, EIGHT_OF_CLUBS)
            .withPlayedCard(PLAYER_3, QUEEN_OF_SPADES)
            .finishRound()
            .finishMatch()
            .build();

    gameRepository.saveAll(List.of(game));

    Match match = game.getSortedMatches().get(0);
    List<Hand> sortedHands =
        match.getHands().stream()
            .sorted(Comparator.comparing(hand -> hand.getParticipation().getParticipationNumber()))
            .collect(Collectors.toList());
    Hand handPlayer1 = sortedHands.get(0);
    Hand handPlayer2 = sortedHands.get(1);
    Hand handPlayer3 = sortedHands.get(2);

    assertThat(handPlayer1.getNumberOfWonTricks(), is(2));
    assertThat(handPlayer2.getNumberOfWonTricks(), is(0));
    assertThat(handPlayer3.getNumberOfWonTricks(), is(1));
  }

  @Test
  void evaluates_score_with_two_stacked_rounds() {
    matchBuilder =
        GameBuilder.builder("test", gameRepository, participationRepository, playerRepository)
            .withParticipation(PLAYER_1)
            .withParticipation(PLAYER_2)
            .withParticipation(PLAYER_3)
            .withMatch();

    matchBuilderWithHands =
        matchBuilder
            .withHandForPlayer(PLAYER_1)
            .withCards(ACE_OF_CLUBS, QUEEN_OF_CLUBS, JACK_OF_CLUBS, KING_OF_HEARTS)
            .withAnnouncedScore(ANNOUNCED_SCORE_PLAYER_1)
            .finishHand()
            .withHandForPlayer(PLAYER_2)
            .withCards(KING_OF_CLUBS, JACK_OF_SPADES, EIGHT_OF_CLUBS, QUEEN_OF_HEARTS)
            .withAnnouncedScore(ANNOUNCED_SCORE_PLAYER_2)
            .finishHand()
            .withHandForPlayer(PLAYER_3)
            .withCards(SEVEN_OF_CLUBS, ACE_OF_SPADES, QUEEN_OF_SPADES, EIGHT_OF_SPADES)
            .withAnnouncedScore(ANNOUNCED_SCORE_PLAYER_3)
            .finishHand();
    Game game =
        matchBuilderWithHands
            .withRound()
            .withPlayedCard(PLAYER_1, QUEEN_OF_CLUBS)
            .withPlayedCard(PLAYER_2, JACK_OF_SPADES)
            .withPlayedCard(PLAYER_3, SEVEN_OF_CLUBS)
            .finishRound()
            .withRound()
            .withPlayedCard(PLAYER_1, ACE_OF_CLUBS)
            .withPlayedCard(PLAYER_2, QUEEN_OF_HEARTS)
            .withPlayedCard(PLAYER_3, ACE_OF_SPADES)
            .finishRound()
            .withRound()
            .withPlayedCard(PLAYER_1, KING_OF_HEARTS)
            .withPlayedCard(PLAYER_2, KING_OF_CLUBS)
            .withPlayedCard(PLAYER_3, EIGHT_OF_SPADES)
            .finishRound()
            .withRound()
            .withPlayedCard(PLAYER_1, JACK_OF_CLUBS)
            .withPlayedCard(PLAYER_2, EIGHT_OF_CLUBS)
            .withPlayedCard(PLAYER_3, QUEEN_OF_SPADES)
            .finishRound()
            .finishMatch()
            .build();

    gameRepository.saveAll(List.of(game));

    Match match = game.getSortedMatches().get(0);
    List<Hand> sortedHands =
        match.getHands().stream()
            .sorted(Comparator.comparing(hand -> hand.getParticipation().getParticipationNumber()))
            .collect(Collectors.toList());
    Hand handPlayer1 = sortedHands.get(0);
    Hand handPlayer2 = sortedHands.get(1);
    Hand handPlayer3 = sortedHands.get(2);

    assertThat(handPlayer1.getNumberOfWonTricks(), is(1));
    assertThat(handPlayer2.getNumberOfWonTricks(), is(0));
    assertThat(handPlayer3.getNumberOfWonTricks(), is(3));
  }

  @Test
  void evaluates_score_with_last_round_stacked() {
    matchBuilder =
        GameBuilder.builder("test", gameRepository, participationRepository, playerRepository)
            .withParticipation(PLAYER_1)
            .withParticipation(PLAYER_2)
            .withParticipation(PLAYER_3)
            .withMatch();

    matchBuilderWithHands =
        matchBuilder
            .withHandForPlayer(PLAYER_1)
            .withCards(ACE_OF_CLUBS, QUEEN_OF_CLUBS, JACK_OF_CLUBS)
            .withAnnouncedScore(ANNOUNCED_SCORE_PLAYER_1)
            .finishHand()
            .withHandForPlayer(PLAYER_2)
            .withCards(KING_OF_CLUBS, JACK_OF_SPADES, EIGHT_OF_CLUBS)
            .withAnnouncedScore(ANNOUNCED_SCORE_PLAYER_2)
            .finishHand()
            .withHandForPlayer(PLAYER_3)
            .withCards(SEVEN_OF_CLUBS, ACE_OF_SPADES, QUEEN_OF_SPADES)
            .withAnnouncedScore(ANNOUNCED_SCORE_PLAYER_3)
            .finishHand();
    Game game =
        matchBuilderWithHands
            .withRound()
            .withPlayedCard(PLAYER_1, JACK_OF_CLUBS)
            .withPlayedCard(PLAYER_2, KING_OF_CLUBS)
            .withPlayedCard(PLAYER_3, ACE_OF_SPADES)
            .finishRound()
            .withRound()
            .withPlayedCard(PLAYER_1, ACE_OF_CLUBS)
            .withPlayedCard(PLAYER_2, JACK_OF_SPADES)
            .withPlayedCard(PLAYER_3, SEVEN_OF_CLUBS)
            .finishRound()
            .withRound()
            .withPlayedCard(PLAYER_1, QUEEN_OF_CLUBS)
            .withPlayedCard(PLAYER_2, EIGHT_OF_CLUBS)
            .withPlayedCard(PLAYER_3, QUEEN_OF_SPADES)
            .finishRound()
            .finishMatch()
            .build();

    gameRepository.saveAll(List.of(game));

    Match match = game.getSortedMatches().get(0);
    List<Hand> sortedHands =
        match.getHands().stream()
            .sorted(Comparator.comparing(hand -> hand.getParticipation().getParticipationNumber()))
            .collect(Collectors.toList());
    Hand handPlayer1 = sortedHands.get(0);
    Hand handPlayer2 = sortedHands.get(1);
    Hand handPlayer3 = sortedHands.get(2);

    System.out.println("PLAYER 1");
    System.out.println(handPlayer1.getNumberOfWonTricks());
    System.out.println("PLAYER 3");
    System.out.println(handPlayer3.getNumberOfWonTricks());

    assertThat(handPlayer2.getNumberOfWonTricks(), is(0));
    assertThat(handPlayer2.getPoints(), is(0));
    assertTrue(
        handPlayer1.getNumberOfWonTricks() == 3 && handPlayer3.getNumberOfWonTricks() == 1
            || handPlayer1.getNumberOfWonTricks() == 1 && handPlayer3.getNumberOfWonTricks() == 3);
    // assertThat(handPlayer1.getNumberOfWonTricks(), is(1));
    // assertThat(handPlayer3.getNumberOfWonTricks(), is(3));
    // assertThat(handPlayer3.getPoints(), is(-1));
  }

  @Test
  void evaluates_score_with_last_two_rounds_stacked() {
    matchBuilder =
        GameBuilder.builder("test", gameRepository, participationRepository, playerRepository)
            .withParticipation(PLAYER_1)
            .withParticipation(PLAYER_2)
            .withParticipation(PLAYER_3)
            .withMatch();

    matchBuilderWithHands =
        matchBuilder
            .withHandForPlayer(PLAYER_1)
            .withCards(ACE_OF_CLUBS, QUEEN_OF_CLUBS, JACK_OF_CLUBS)
            .withAnnouncedScore(ANNOUNCED_SCORE_PLAYER_1)
            .finishHand()
            .withHandForPlayer(PLAYER_2)
            .withCards(KING_OF_CLUBS, JACK_OF_SPADES, EIGHT_OF_CLUBS)
            .withAnnouncedScore(ANNOUNCED_SCORE_PLAYER_2)
            .finishHand()
            .withHandForPlayer(PLAYER_3)
            .withCards(SEVEN_OF_CLUBS, ACE_OF_SPADES, QUEEN_OF_SPADES)
            .withAnnouncedScore(ANNOUNCED_SCORE_PLAYER_3)
            .finishHand();
    Game game =
        matchBuilderWithHands
            .withRound()
            .withPlayedCard(PLAYER_1, JACK_OF_CLUBS)
            .withPlayedCard(PLAYER_2, KING_OF_CLUBS)
            .withPlayedCard(PLAYER_3, SEVEN_OF_CLUBS)
            .finishRound()
            .withRound()
            .withPlayedCard(PLAYER_1, ACE_OF_CLUBS)
            .withPlayedCard(PLAYER_2, JACK_OF_SPADES)
            .withPlayedCard(PLAYER_3, ACE_OF_SPADES)
            .finishRound()
            .withRound()
            .withPlayedCard(PLAYER_1, QUEEN_OF_CLUBS)
            .withPlayedCard(PLAYER_2, EIGHT_OF_CLUBS)
            .withPlayedCard(PLAYER_3, QUEEN_OF_SPADES)
            .finishRound()
            .finishMatch()
            .build();

    gameRepository.saveAll(List.of(game));

    Match match = game.getSortedMatches().get(0);
    List<Hand> sortedHands =
        match.getHands().stream()
            .sorted(Comparator.comparing(hand -> hand.getParticipation().getParticipationNumber()))
            .collect(Collectors.toList());
    Hand handPlayer1 = sortedHands.get(0);
    Hand handPlayer2 = sortedHands.get(1);
    Hand handPlayer3 = sortedHands.get(2);

    assertThat(handPlayer2.getNumberOfWonTricks(), is(1));
    // assertThat(handPlayer2.getPoints(), is(0));

    assertTrue(
        handPlayer1.getNumberOfWonTricks() == 3 && handPlayer3.getNumberOfWonTricks() == 0
            || handPlayer1.getNumberOfWonTricks() == 0 && handPlayer3.getNumberOfWonTricks() == 3);
    // assertThat(handPlayer3.getPoints(), is(-1));
  }
}
