package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.serialization;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.*;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import org.springframework.data.projection.TargetAware;

public class CardEmbedProjectionSerializer extends JsonSerializer<TargetAware> {

  private final CardSerializer cardSerializer;
  private final JsonSerializer<Object> defaultSerializer;

  public CardEmbedProjectionSerializer(
      CardSerializer cardSerializer, JsonSerializer<Object> defaultSerializer) {
    this.cardSerializer = cardSerializer;
    this.defaultSerializer = defaultSerializer;
  }

  @Override
  public void serialize(TargetAware value, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {
    cardSerializer.hideFieldsInPlaceIfNecessary((Card) value.getTarget());
    defaultSerializer.serialize(value, gen, serializers);
  }
}
