package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import static org.junit.jupiter.api.Assertions.*;

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
}
