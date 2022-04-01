package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import java.util.*;

public class StandardCardDeck implements CardDeck {

  private static CardSuit cardSuit;

  private static CardRank cardRank;

  private Stack<Card> cardDeck = new Stack<>();

  public StandardCardDeck() {
    for (CardSuit cardSuit : CardSuit.values()) {
      for (CardRank cardRank : cardRank.values()) {
        Card card = new Card(cardRank, cardSuit);
        cardDeck.push(card);
      }
    }
  }

  @Override
  public Card drawCard() {
    return cardDeck.pop();
  }

  public void shuffle() {
    Collections.shuffle(this.cardDeck);
  }
}
