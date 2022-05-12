package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.serialization;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Card;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

public class CardSerializer extends JsonSerializer<Card> {
  private final JsonSerializer<Object> defaultSerializer;

  public CardSerializer(JsonSerializer<Object> serializer) {
    defaultSerializer = serializer;
  }

  @Override
  public void serialize(Card value, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {
    hideFieldsInPlaceIfNecessary(value);
    defaultSerializer.serialize(value, gen, serializers);
  }

  public void hideFieldsInPlaceIfNecessary(Card card) {
    // not implemented yet
  }
}
