package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.serialization;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.*;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.security.expressions.CustomMethodSecurityExpressionRoot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CardSerializerTest {
  private CustomMethodSecurityExpressionRoot customMethodSecurityExpressionRoot;
  private final Card card = new Card();
  private CardSerializer cardSerializer;
  private Match match;

  @BeforeEach
  void setup() {
    customMethodSecurityExpressionRoot = mock(CustomMethodSecurityExpressionRoot.class);
    cardSerializer = new CardSerializer(null, () -> customMethodSecurityExpressionRoot);

    card.setCardRank(CardRank.ACE);
    card.setCardSuit(CardSuit.CLUB);

    Hand hand = new Hand();
    card.setHand(hand);
    hand.getCards().add(card);

    match = new Match();
    match.setMatchNumber(1);
    hand.setMatch(match);
    match.getHands().add(hand);
  }

  @Test
  void does_not_hide_fields_if_card_was_played() {
    card.setRound(new Round());

    cardSerializer.hideFieldsInPlaceIfNecessary(card);

    assertNotNull(card.getCardRank());
    assertNotNull(card.getCardSuit());
  }

  @Test
  void hides_fields_if_its_not_own_card() {
    when(customMethodSecurityExpressionRoot.isOwnCard(card)).thenReturn(false);

    cardSerializer.hideFieldsInPlaceIfNecessary(card);

    assertNull(card.getCardRank());
    assertNull(card.getCardSuit());
  }

  @Test
  void does_not_hide_if_its_own_card() {
    when(customMethodSecurityExpressionRoot.isOwnCard(card)).thenReturn(true);

    cardSerializer.hideFieldsInPlaceIfNecessary(card);

    assertNotNull(card.getCardRank());
    assertNotNull(card.getCardSuit());
  }

  @Test
  void hides_if_its_own_card_in_match_5() {
    when(customMethodSecurityExpressionRoot.isOwnCard(card)).thenReturn(true);
    match.setMatchNumber(5);

    cardSerializer.hideFieldsInPlaceIfNecessary(card);

    assertNull(card.getCardRank());
    assertNull(card.getCardSuit());
  }

  @Test
  void does_not_hide_if_its_not_own_card_in_match_5() {
    when(customMethodSecurityExpressionRoot.isOwnCard(card)).thenReturn(false);
    match.setMatchNumber(5);

    cardSerializer.hideFieldsInPlaceIfNecessary(card);

    assertNotNull(card.getCardRank());
    assertNotNull(card.getCardSuit());
  }
}
