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
  private static final String PLAYER_3 = "player3";
  private static final String PLAYER_4 = "player4";
  private static final String PLAYER_5 = "player5";

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
  void if_the_first_player_left_the_second_player_has_to_start() {
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

    Participation participation1 =
        game.getParticipations().stream()
            .filter(participation -> PLAYER_1.equals(participation.getPlayer().getName()))
            .findFirst()
            .orElseThrow();
    participation1.setActive(false);

    Match activeMatch = game.getLastMatch().orElseThrow();
    List<Hand> hands =
        activeMatch.getHands().stream()
            .sorted(Comparator.comparing(hand -> hand.getParticipation().getParticipationNumber()))
            .collect(Collectors.toList());
    Hand firstHand = hands.get(0);
    Hand secondHand = hands.get(1);

    assertThat(firstHand.isTurnActive(), is(false));
    assertThat(secondHand.isTurnActive(), is(true));

    Round activeRound = activeMatch.getLastRound().orElseThrow();
    Card cardToPlay = firstHand.getCards().iterator().next();
    cardToPlay.setRound(activeRound);
    activeRound.getCards().add(cardToPlay);

    assertThat(firstHand.isTurnActive(), is(false));
    assertThat(secondHand.isTurnActive(), is(true));
  }

  @Test
  void the_winner_of_the_last_round_has_to_start_the_next_round() {
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
            .withPlayedCard(PLAYER_1, QUEEN_OF_CLUBS)
            .withPlayedCard(PLAYER_2, KING_OF_CLUBS)
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

    Round lastRound = activeMatch.getLastRound().orElseThrow();
    Card availableCardOfHand2 =
        secondHand.getCards().stream()
            .filter(card -> card.getRound() == null)
            .findFirst()
            .orElseThrow();
    availableCardOfHand2.setRound(lastRound);
    lastRound.getCards().add(availableCardOfHand2);

    assertThat(firstHand.isTurnActive(), is(true));
    assertThat(secondHand.isTurnActive(), is(false));
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

  @Test
  void in_the_second_match_the_start_player_shifts_by_1_following_the_participation_number() {
    Game game =
        GameBuilder.builder("game1")
            .withParticipation(PLAYER_1)
            .withParticipation(PLAYER_2)
            .withParticipation(PLAYER_3)
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
            .withHandForPlayer(PLAYER_3)
            .withCards(QUEEN_OF_HEARTS, KING_OF_HEARTS)
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
    Hand thirdHand = hands.get(2);

    assertThat(firstHand.isTurnActive(), is(false));
    assertThat(secondHand.isTurnActive(), is(true));
    assertThat(thirdHand.isTurnActive(), is(false));

    Match previousMatch = game.getSortedMatches().get(0);
    Optional<Round> lastRoundOfPreviousMatch = previousMatch.getLastRound();
    assertThat(lastRoundOfPreviousMatch, not(Optional.empty()));

    Assertions.assertThat(previousMatch.getHands()).allMatch(hand -> !hand.isTurnActive());
  }

  @Test
  void player_who_won_last_trick_starts_playing_next_round_with_three_players() {
    Game game =
        GameBuilder.builder("game1")
            .withParticipation(PLAYER_1)
            .withParticipation(PLAYER_2)
            .withParticipation(PLAYER_3)
            .withMatch()
            .withMatchState(MatchState.FINISH)
            .withRound()
            .finishRound()
            .finishMatch()
            .withMatch()
            .withMatchState(MatchState.PLAYING)
            .withHandForPlayer(PLAYER_1)
            .withCards(ACE_OF_CLUBS, SIX_OF_HEARTS)
            .withAnnouncedScore(1)
            .finishHand()
            .withHandForPlayer(PLAYER_2)
            .withCards(JACK_OF_DIAMONDS, KING_OF_CLUBS)
            .withAnnouncedScore(0)
            .finishHand()
            .withHandForPlayer(PLAYER_3)
            .withCards(QUEEN_OF_CLUBS, EIGHT_OF_SPADES)
            .withAnnouncedScore(0)
            .finishHand()
            .withRound()
            .withPlayedCard(PLAYER_1, SIX_OF_HEARTS)
            .withPlayedCard(PLAYER_2, JACK_OF_DIAMONDS)
            .withPlayedCard(PLAYER_3, EIGHT_OF_SPADES)
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
    Hand thirdHand = hands.get(2);

    assertThat(firstHand.isTurnActive(), is(false));
    assertThat(secondHand.isTurnActive(), is(true));
    assertThat(thirdHand.isTurnActive(), is(false));
  }

  @Test
  void player_who_won_last_trick_starts_playing_next_round_with_four_players() {
    Game game =
        GameBuilder.builder("game1")
            .withParticipation(PLAYER_1)
            .withParticipation(PLAYER_2)
            .withParticipation(PLAYER_3)
            .withParticipation(PLAYER_4)
            .withMatch()
            .withMatchState(MatchState.FINISH)
            .withRound()
            .finishRound()
            .finishMatch()
            .withMatch()
            .withMatchState(MatchState.PLAYING)
            .withHandForPlayer(PLAYER_1)
            .withCards(ACE_OF_CLUBS, SIX_OF_HEARTS)
            .withAnnouncedScore(1)
            .finishHand()
            .withHandForPlayer(PLAYER_2)
            .withCards(JACK_OF_DIAMONDS, KING_OF_CLUBS)
            .withAnnouncedScore(0)
            .finishHand()
            .withHandForPlayer(PLAYER_3)
            .withCards(QUEEN_OF_CLUBS, EIGHT_OF_SPADES)
            .withAnnouncedScore(1)
            .finishHand()
            .withHandForPlayer(PLAYER_4)
            .withCards(NINE_OF_CLUBS, KING_OF_HEARTS)
            .withAnnouncedScore(2)
            .finishHand()
            .withRound()
            .withPlayedCard(PLAYER_1, SIX_OF_HEARTS)
            .withPlayedCard(PLAYER_2, JACK_OF_DIAMONDS)
            .withPlayedCard(PLAYER_3, QUEEN_OF_CLUBS)
            .withPlayedCard(PLAYER_4, NINE_OF_CLUBS)
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
    Hand thirdHand = hands.get(2);
    Hand fourthHand = hands.get(3);

    assertThat(firstHand.isTurnActive(), is(false));
    assertThat(secondHand.isTurnActive(), is(false));
    assertThat(thirdHand.isTurnActive(), is(true));
    assertThat(fourthHand.isTurnActive(), is(false));
  }

  @Test
  void player_who_won_last_trick_starts_playing_next_round_with_four_players_2() {
    Game game =
        GameBuilder.builder("game1")
            .withParticipation(PLAYER_1)
            .withParticipation(PLAYER_2)
            .withParticipation(PLAYER_3)
            .withParticipation(PLAYER_4)
            .withMatch()
            .withMatchState(MatchState.FINISH)
            .withRound()
            .finishRound()
            .finishMatch()
            .withMatch()
            .withMatchState(MatchState.PLAYING)
            .withHandForPlayer(PLAYER_1)
            .withCards(ACE_OF_CLUBS, SIX_OF_HEARTS)
            .withAnnouncedScore(1)
            .finishHand()
            .withHandForPlayer(PLAYER_2)
            .withCards(JACK_OF_DIAMONDS, KING_OF_CLUBS)
            .withAnnouncedScore(0)
            .finishHand()
            .withHandForPlayer(PLAYER_3)
            .withCards(QUEEN_OF_CLUBS, EIGHT_OF_SPADES)
            .withAnnouncedScore(1)
            .finishHand()
            .withHandForPlayer(PLAYER_4)
            .withCards(NINE_OF_CLUBS, KING_OF_HEARTS)
            .withAnnouncedScore(2)
            .finishHand()
            .withRound()
            .withPlayedCard(PLAYER_1, SIX_OF_HEARTS)
            .withPlayedCard(PLAYER_2, JACK_OF_DIAMONDS)
            .withPlayedCard(PLAYER_3, EIGHT_OF_SPADES)
            .withPlayedCard(PLAYER_4, NINE_OF_CLUBS)
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
    Hand thirdHand = hands.get(2);
    Hand fourthHand = hands.get(3);

    assertThat(firstHand.isTurnActive(), is(false));
    assertThat(secondHand.isTurnActive(), is(true));
    assertThat(thirdHand.isTurnActive(), is(false));
    assertThat(fourthHand.isTurnActive(), is(false));
  }

  @Test
  void player_who_won_last_trick_starts_playing_next_round_with_five_players() {
    Game game =
        GameBuilder.builder("game1")
            .withParticipation(PLAYER_1)
            .withParticipation(PLAYER_2)
            .withParticipation(PLAYER_3)
            .withParticipation(PLAYER_4)
            .withParticipation(PLAYER_5)
            .withMatch()
            .withMatchState(MatchState.FINISH)
            .withRound()
            .finishRound()
            .finishMatch()
            .withMatch()
            .withMatchState(MatchState.PLAYING)
            .withHandForPlayer(PLAYER_1)
            .withCards(ACE_OF_CLUBS, SIX_OF_HEARTS)
            .withAnnouncedScore(1)
            .finishHand()
            .withHandForPlayer(PLAYER_2)
            .withCards(JACK_OF_DIAMONDS, KING_OF_CLUBS)
            .withAnnouncedScore(0)
            .finishHand()
            .withHandForPlayer(PLAYER_3)
            .withCards(QUEEN_OF_CLUBS, EIGHT_OF_SPADES)
            .withAnnouncedScore(1)
            .finishHand()
            .withHandForPlayer(PLAYER_4)
            .withCards(NINE_OF_CLUBS, KING_OF_HEARTS)
            .withAnnouncedScore(2)
            .finishHand()
            .withHandForPlayer(PLAYER_5)
            .withCards(QUEEN_OF_HEARTS, EIGHT_OF_CLUBS)
            .withAnnouncedScore(0)
            .finishHand()
            .withRound()
            .withPlayedCard(PLAYER_1, SIX_OF_HEARTS)
            .withPlayedCard(PLAYER_2, JACK_OF_DIAMONDS)
            .withPlayedCard(PLAYER_3, EIGHT_OF_SPADES)
            .withPlayedCard(PLAYER_4, NINE_OF_CLUBS)
            .withPlayedCard(PLAYER_5, EIGHT_OF_CLUBS)
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
    Hand thirdHand = hands.get(2);
    Hand fourthHand = hands.get(3);
    Hand fifthHand = hands.get(4);

    assertThat(firstHand.isTurnActive(), is(false));
    assertThat(secondHand.isTurnActive(), is(true));
    assertThat(thirdHand.isTurnActive(), is(false));
    assertThat(fourthHand.isTurnActive(), is(false));
    assertThat(fifthHand.isTurnActive(), is(false));
  }

  @Test
  void player_who_started_last_round_starts_playing_next_round_when_stacked_round_same_winner() {
    Game game =
        GameBuilder.builder("game1")
            .withParticipation(PLAYER_1)
            .withParticipation(PLAYER_2)
            .withParticipation(PLAYER_3)
            .withMatch()
            .withMatchState(MatchState.FINISH)
            .withRound()
            .finishRound()
            .finishMatch()
            .withMatch()
            .withMatchState(MatchState.PLAYING)
            .withHandForPlayer(PLAYER_1)
            .withCards(ACE_OF_CLUBS, KING_OF_HEARTS, EIGHT_OF_CLUBS)
            .withAnnouncedScore(1)
            .finishHand()
            .withHandForPlayer(PLAYER_2)
            .withCards(JACK_OF_DIAMONDS, KING_OF_CLUBS, QUEEN_OF_HEARTS)
            .withAnnouncedScore(0)
            .finishHand()
            .withHandForPlayer(PLAYER_3)
            .withCards(QUEEN_OF_CLUBS, SEVEN_OF_CLUBS, KING_OF_SPADES)
            .withAnnouncedScore(0)
            .finishHand()
            .withRound()
            .withPlayedCard(PLAYER_1, EIGHT_OF_CLUBS)
            .withPlayedCard(PLAYER_2, JACK_OF_DIAMONDS)
            .withPlayedCard(PLAYER_3, SEVEN_OF_CLUBS)
            .finishRound()
            .withRound()
            .withPlayedCard(PLAYER_1, KING_OF_HEARTS)
            .withPlayedCard(PLAYER_2, KING_OF_CLUBS)
            .withPlayedCard(PLAYER_3, QUEEN_OF_CLUBS)
            .finishRound()
            .withRound()
            .finishRound()
            .finishMatch()
            .build();

    Match match = game.getLastMatch().orElseThrow();
    List<Hand> hands =
        match.getHands().stream()
            .sorted(Comparator.comparing(hand -> hand.getParticipation().getParticipationNumber()))
            .collect(Collectors.toList());

    Hand firstHand = hands.get(0);
    Hand secondHand = hands.get(1);
    Hand thirdHand = hands.get(2);

    assertThat(firstHand.isTurnActive(), is(false));
    assertThat(secondHand.isTurnActive(), is(true));
    assertThat(thirdHand.isTurnActive(), is(false));
  }

  @Test
  void player_who_started_last_round_starts_playing_next_round_when_stacked_round_other_winner() {
    Game game =
        GameBuilder.builder("game1")
            .withParticipation(PLAYER_1)
            .withParticipation(PLAYER_2)
            .withParticipation(PLAYER_3)
            .withMatch()
            .withMatchState(MatchState.FINISH)
            .withRound()
            .finishRound()
            .finishMatch()
            .withMatch()
            .withMatchState(MatchState.PLAYING)
            .withHandForPlayer(PLAYER_1)
            .withCards(ACE_OF_CLUBS, KING_OF_HEARTS, EIGHT_OF_CLUBS)
            .withAnnouncedScore(1)
            .finishHand()
            .withHandForPlayer(PLAYER_2)
            .withCards(JACK_OF_DIAMONDS, KING_OF_CLUBS, QUEEN_OF_HEARTS)
            .withAnnouncedScore(0)
            .finishHand()
            .withHandForPlayer(PLAYER_3)
            .withCards(QUEEN_OF_CLUBS, EIGHT_OF_SPADES, KING_OF_SPADES)
            .withAnnouncedScore(0)
            .finishHand()
            .withRound()
            .withPlayedCard(PLAYER_1, EIGHT_OF_CLUBS)
            .withPlayedCard(PLAYER_2, JACK_OF_DIAMONDS)
            .withPlayedCard(PLAYER_3, QUEEN_OF_CLUBS)
            .finishRound()
            .withRound()
            .withPlayedCard(PLAYER_1, KING_OF_HEARTS)
            .withPlayedCard(PLAYER_2, KING_OF_CLUBS)
            .withPlayedCard(PLAYER_3, EIGHT_OF_SPADES)
            .finishRound()
            .withRound()
            .finishRound()
            .finishMatch()
            .build();

    Match match = game.getLastMatch().orElseThrow();
    List<Hand> hands =
        match.getHands().stream()
            .sorted(Comparator.comparing(hand -> hand.getParticipation().getParticipationNumber()))
            .collect(Collectors.toList());

    Hand firstHand = hands.get(0);
    Hand secondHand = hands.get(1);
    Hand thirdHand = hands.get(2);

    assertThat(firstHand.isTurnActive(), is(false));
    assertThat(secondHand.isTurnActive(), is(false));
    assertThat(thirdHand.isTurnActive(), is(true));
  }
}
