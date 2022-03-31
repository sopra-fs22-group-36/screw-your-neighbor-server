package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StandardCardDeckTest {

  private StandardCardDeck standardCardDeck;

  @BeforeEach
  public void setup() {
    standardCardDeck = new StandardCardDeck();
  }

  @Test
  public void draw_card_not_shuffled_test() {
    Card card = standardCardDeck.drawCard();
    // StandardCardSet is filled up by iterating over the Enum CardSuit and CardRank, by looking how
    // Suits and Ranks
    // are ordered, we see that SIX/SPADE will be the top card (because it was added last)
    CardRank cardRank = CardRank.SIX;
    CardSuit cardSuit = CardSuit.SPADE;
    Card last_card = new Card(cardRank, cardSuit);

    assertEquals(card.getCardRankName(), last_card.getCardRankName());
    assertEquals(card.getCardSuitName(), last_card.getCardSuitName());
  }

  @Test
  public void shuffle_test() {
    StandardCardDeck previous = this.standardCardDeck;
    standardCardDeck.shuffle();
    Card card = standardCardDeck.drawCard();
    CardRank cardRank = CardRank.SIX;
    CardSuit cardSuit = CardSuit.SPADE;
    Card last_card = new Card(cardRank, cardSuit);
    // In very rare cases it may happen, that accidentally after shuffling, the top card is still
    // the same as before the shuffling, then we shuffle again, before doing the assertion.
    if (card.getCardRankName().equals(last_card.getCardRankName())
        && card.getCardSuitName().equals(last_card.getCardSuitName())) {
      standardCardDeck.shuffle();
      card = standardCardDeck.drawCard();
    }
    // either Rank or Suit should not be the same as before anymore.
    assertTrue(
        card.getCardRankName() != last_card.getCardRankName()
            || card.getCardSuitName() != last_card.getCardSuitName());
  }
}
