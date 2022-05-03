package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.CardRank;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.CardSuit;
import java.util.Objects;

public class CardValue {
  public static final CardValue ACE_OF_CLUBS = of(CardRank.ACE, CardSuit.CLUB);
  public static final CardValue KING_OF_CLUBS = of(CardRank.KING, CardSuit.CLUB);
  public static final CardValue QUEEN_OF_CLUBS = of(CardRank.QUEEN, CardSuit.CLUB);
  public static final CardValue JACK_OF_CLUBS = of(CardRank.JACK, CardSuit.CLUB);
  public static final CardValue TEN_OF_CLUBS = of(CardRank.TEN, CardSuit.CLUB);
  public static final CardValue NINE_OF_CLUBS = of(CardRank.NINE, CardSuit.CLUB);
  public static final CardValue EIGHT_OF_CLUBS = of(CardRank.EIGHT, CardSuit.CLUB);
  public static final CardValue SEVEN_OF_CLUBS = of(CardRank.SEVEN, CardSuit.CLUB);

  public static final CardValue ACE_OF_SPADES = of(CardRank.ACE, CardSuit.SPADE);
  public static final CardValue KING_OF_SPADES = of(CardRank.KING, CardSuit.SPADE);
  public static final CardValue QUEEN_OF_SPADES = of(CardRank.QUEEN, CardSuit.SPADE);
  public static final CardValue JACK_OF_SPADES = of(CardRank.JACK, CardSuit.SPADE);
  public static final CardValue EIGHT_OF_SPADES = of(CardRank.EIGHT, CardSuit.SPADE);

  public static final CardValue QUEEN_OF_HEARTS = of(CardRank.QUEEN, CardSuit.HEART);
  public static final CardValue KING_OF_HEARTS = of(CardRank.KING, CardSuit.HEART);
  public static final CardValue SIX_OF_HEARTS = of(CardRank.SIX, CardSuit.HEART);

  public static final CardValue JACK_OF_DIAMONDS = of(CardRank.JACK, CardSuit.DIAMOND);

  public static CardValue of(CardRank rank, CardSuit suit) {
    return new CardValue(rank, suit);
  }

  private final CardRank rank;
  private final CardSuit cardSuit;

  private CardValue(CardRank rank, CardSuit cardSuit) {
    this.rank = Objects.requireNonNull(rank);
    this.cardSuit = Objects.requireNonNull(cardSuit);
  }

  public CardRank getRank() {
    return rank;
  }

  public CardSuit getCardSuit() {
    return cardSuit;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CardValue cardValue = (CardValue) o;
    return rank == cardValue.rank && cardSuit == cardValue.cardSuit;
  }

  @Override
  public int hashCode() {
    return Objects.hash(rank, cardSuit);
  }

  @Override
  public String toString() {
    return "CardValue{" + "rank=" + rank + ", cardSuit=" + cardSuit + '}';
  }
}
