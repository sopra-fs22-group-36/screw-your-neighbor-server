package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithPersistedPlayerSecurityContextFactory.class)
public @interface WithPersistedPlayer {
  String playerName() default "test";
}
