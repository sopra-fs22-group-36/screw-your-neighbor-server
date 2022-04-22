package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import java.util.*;

public class StandardCardDeck implements CardDeck {

  private Stack<Card> cardDeck = new Stack<>();

  public StandardCardDeck() {
    for (CardSuit cardSuit : CardSuit.values()) {
      for (CardRank cardRank : CardRank.values()) {
        Card card = new Card(cardRank, cardSuit);
        cardDeck.push(card);
      }
    }
  }

  @Override
  public Card drawCard() {
    return cardDeck.pop();
  }

  @Override
  public void shuffle() {
    Collections.shuffle(this.cardDeck);
  }
}
