package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import static ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.CardValue.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.GameBuilder;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class HandTurnActiveTest {

  private static final String PLAYER_1 = "player1";
  private static final String PLAYER_2 = "player2";

  @Test
  void the_first_player_must_announce_his_score_at_match_start() {
    Game game =
        GameBuilder.builder("game1")
            .withParticipation(PLAYER_1)
            .withParticipation(PLAYER_2)
            .withGameState(GameState.PLAYING)
            .withMatch()
            .withHandForPlayer(PLAYER_1)
            .withCards(ACE_OF_CLUBS, QUEEN_OF_CLUBS)
            .finishHand()
            .withHandForPlayer(PLAYER_2)
            .withCards(KING_OF_CLUBS, JACK_OF_CLUBS)
            .finishHand()
            .withRound()
            .finishRound()
            .finishMatch()
            .build();

    Match activeMatch = game.getLastMatch().orElseThrow();
    List<Hand> hands =
        activeMatch.getHands().stream()
            .sorted(Comparator.comparing(hand -> hand.getParticipation().getParticipationNumber()))
            .collect(Collectors.toList());
    Hand firstHand = hands.get(0);
    Hand secondHand = hands.get(1);

    assertThat(firstHand.isTurnActive(), is(true));
    assertThat(secondHand.isTurnActive(), is(false));

    firstHand.setAnnouncedScore(1);

    assertThat(firstHand.isTurnActive(), is(false));
    assertThat(secondHand.isTurnActive(), is(true));
  }

  @Test
  void the_first_player_must_play_a_card_when_round_starts() {
    Game game =
        GameBuilder.builder("game1")
            .withParticipation(PLAYER_1)
            .withParticipation(PLAYER_2)
            .withMatch()
            .withMatchState(MatchState.PLAYING)
            .withHandForPlayer(PLAYER_1)
            .withCards(ACE_OF_CLUBS, QUEEN_OF_CLUBS)
            .withAnnouncedScore(1)
            .finishHand()
            .withHandForPlayer(PLAYER_2)
            .withCards(KING_OF_CLUBS, JACK_OF_CLUBS)
            .withAnnouncedScore(0)
            .finishHand()
            .withRound()
            .finishRound()
            .finishMatch()
            .build();

    Match activeMatch = game.getLastMatch().orElseThrow();
    List<Hand> hands =
        activeMatch.getHands().stream()
            .sorted(Comparator.comparing(hand -> hand.getParticipation().getParticipationNumber()))
            .collect(Collectors.toList());
    Hand firstHand = hands.get(0);
    Hand secondHand = hands.get(1);

    assertThat(firstHand.isTurnActive(), is(true));
    assertThat(secondHand.isTurnActive(), is(false));

    Round activeRound = activeMatch.getLastRound().orElseThrow();
    Card cardToPlay = firstHand.getCards().iterator().next();
    cardToPlay.setRound(activeRound);
    activeRound.getCards().add(cardToPlay);

    assertThat(firstHand.isTurnActive(), is(false));
    assertThat(secondHand.isTurnActive(), is(true));
  }

  @Test
  void in_the_second_match_the_start_player_shifts_by_1() {
    Game game =
        GameBuilder.builder("game1")
            .withParticipation(PLAYER_1)
            .withParticipation(PLAYER_2)
            .withMatch()
            .withMatchState(MatchState.FINISH)
            .withRound()
            .finishRound()
            .finishMatch()
            .withMatch()
            .withMatchState(MatchState.PLAYING)
            .withHandForPlayer(PLAYER_1)
            .withCards(ACE_OF_CLUBS, QUEEN_OF_CLUBS)
            .withAnnouncedScore(1)
            .finishHand()
            .withHandForPlayer(PLAYER_2)
            .withCards(KING_OF_CLUBS, JACK_OF_CLUBS)
            .withAnnouncedScore(0)
            .finishHand()
            .withRound()
            .finishRound()
            .withRound()
            .finishRound()
            .finishMatch()
            .build();

    Match activeMatch = game.getLastMatch().orElseThrow();
    List<Hand> hands =
        activeMatch.getHands().stream()
            .sorted(Comparator.comparing(hand -> hand.getParticipation().getParticipationNumber()))
            .collect(Collectors.toList());
    Hand firstHand = hands.get(0);
    Hand secondHand = hands.get(1);

    assertThat(firstHand.isTurnActive(), is(false));
    assertThat(secondHand.isTurnActive(), is(true));

    Match previousMatch = game.getSortedMatches().get(0);
    Optional<Round> lastRoundOfPreviousMatch = previousMatch.getLastRound();
    assertThat(lastRoundOfPreviousMatch, not(Optional.empty()));

    Assertions.assertThat(previousMatch.getHands()).allMatch(hand -> !hand.isTurnActive());
  }
}
