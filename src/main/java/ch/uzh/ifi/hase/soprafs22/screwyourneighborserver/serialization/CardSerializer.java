package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.serialization;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Card;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.security.expressions.CustomMethodSecurityExpressionRoot;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.function.Supplier;

public class CardSerializer extends JsonSerializer<Card> {
  private final JsonSerializer<Object> defaultSerializer;
  private final Supplier<CustomMethodSecurityExpressionRoot> securityExpressionRootSupplier;

  public CardSerializer(
      JsonSerializer<Object> defaultSerializer,
      Supplier<CustomMethodSecurityExpressionRoot> securityExpressionRootSupplier) {
    this.defaultSerializer = defaultSerializer;
    this.securityExpressionRootSupplier = securityExpressionRootSupplier;
  }

  @Override
  public void serialize(Card value, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {
    hideFieldsInPlaceIfNecessary(value);
    defaultSerializer.serialize(value, gen, serializers);
  }

  public void hideFieldsInPlaceIfNecessary(Card card) {
    if (card.getRound() != null) {
      return;
    }

    boolean shouldHide = !securityExpressionRootSupplier.get().isOwnCard(card);
    boolean isMatch5 = card.getHand().getMatch().getMatchNumber() == 5;
    if (isMatch5) {
      shouldHide = !shouldHide;
    }

    if (shouldHide) {
      card.setCardRank(null);
      card.setCardSuit(null);
    }
  }
}
