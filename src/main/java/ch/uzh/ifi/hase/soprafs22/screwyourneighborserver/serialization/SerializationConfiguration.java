package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.serialization;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Card;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.CardEmbedProjection;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SerializationConfiguration {
  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(
        new SimpleModule() {
          @Override
          public void setupModule(final SetupContext context) {
            super.setupModule(context);
            context.addBeanSerializerModifier(createBeanSerializerModifier());
          }
        });
    return objectMapper;
  }

  private BeanSerializerModifier createBeanSerializerModifier() {
    return new BeanSerializerModifier() {
      @Override
      public JsonSerializer<?> modifySerializer(
          final SerializationConfig config,
          final BeanDescription beanDesc,
          final JsonSerializer<?> serializer) {
        //noinspection unchecked
        CardSerializer cardSerializer = new CardSerializer((JsonSerializer<Object>) serializer);
        if (Card.class.isAssignableFrom(beanDesc.getBeanClass())) {
          return cardSerializer;
        }
        if (CardEmbedProjection.class.isAssignableFrom(beanDesc.getBeanClass())) {
          // 2 serializers can serialize CardEmbedProjection.
          // We only want ProjectionSerializer.PersistentEntityJackson2Module
          if (serializer instanceof BeanSerializer) {
            return serializer;
          }
          //noinspection unchecked
          return new CardEmbedProjectionSerializer(
              cardSerializer, (JsonSerializer<Object>) serializer);
        }
        return serializer;
      }
    };
  }
}
