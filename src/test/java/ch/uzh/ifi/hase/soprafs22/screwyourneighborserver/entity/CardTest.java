package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CardTest {

  private Card card_1;
  private CardRank cardRank;
  private CardSuit cardSuit;

  @BeforeEach
  void setup() {
    cardRank = CardRank.JACK;
    cardSuit = CardSuit.DIAMOND;
    card_1 = new Card(cardRank, cardSuit);
  }

  @Test
  void compare_equal_test() {
    cardRank = CardRank.JACK;
    cardSuit = CardSuit.DIAMOND;
    Card card_2 = new Card(cardRank, cardSuit);
    assertTrue(card_1.isEqualTo(card_2));
    assertTrue(card_2.isEqualTo(card_1));
  }

  @Test
  void compare_not_equal_test() {
    cardRank = CardRank.NINE;
    cardSuit = CardSuit.CLUB;
    Card card_2 = new Card(cardRank, cardSuit);
    assertFalse(card_1.isEqualTo(card_2));
    assertFalse(card_2.isEqualTo(card_1));
  }

  @Test
  void compare_greater_test() {
    cardRank = CardRank.EIGHT;
    cardSuit = CardSuit.HEART;
    Card card_2 = new Card(cardRank, cardSuit);
    assertTrue(card_1.isGreaterThan(card_2));
  }

  @Test
  void compare_smaller_greater_test() {
    cardRank = CardRank.ACE;
    cardSuit = CardSuit.DIAMOND;
    Card card_2 = new Card(cardRank, cardSuit);
    assertFalse(card_1.isGreaterThan(card_2));
    assertTrue(card_2.isGreaterThan(card_1));
  }

  @Test
  void compare_cardRank_null_and_null() {
    Card card = new Card();
    Card card_2 = new Card();
    assertFalse(card.isGreaterThan(card_2));
    assertFalse(card_2.isGreaterThan(card));
    assertThat(card.compareTo(card_2), is(0));
  }

  @Test
  void compare_cardRank_null_and_SIX() {
    Card card = new Card();
    Card card_2 = new Card(CardRank.SIX, CardSuit.CLUB);
    assertFalse(card.isGreaterThan(card_2));
    assertTrue(card_2.isGreaterThan(card));
  }

  @Test
  void compare_cardRank_SIX_and_null() {
    Card card = new Card(CardRank.SIX, CardSuit.CLUB);
    Card card_2 = new Card();
    assertTrue(card.isGreaterThan(card_2));
    assertFalse(card_2.isGreaterThan(card));
  }

  @Test
  void is_not_highest_card_when_round_is_null() {
    Card card = new Card();

    assertThat(card.isHighestCardInRound(), is(false));
  }

  @Test
  void is_not_highest_card_when_this_not_in_highest_cards() {
    Round round = mock(Round.class);
    when(round.getHighestCards()).thenReturn(new LinkedHashSet<>(Set.of(new Card())));
    Card card = new Card();
    card.setRound(round);

    assertThat(card.isHighestCardInRound(), is(false));
  }

  @Test
  void is_highest_card_when_this_is_in_highest_cards() {
    Round round = mock(Round.class);
    Card card = new Card();
    card.setCardRank(CardRank.ACE);
    when(round.getHighestCards()).thenReturn(new LinkedHashSet<>(Set.of(card)));
    card.setRound(round);

    assertThat(card.isHighestCardInRound(), is(true));

    when(round.getHighestCards()).thenReturn(new LinkedHashSet<>(Set.of(card, new Card())));
    assertThat(card.isHighestCardInRound(), is(true));
  }
}
