package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util;

import java.util.Collection;
import java.util.Optional;
import org.springframework.http.HttpHeaders;

public class SessionUtil {
  public static String getSessionIdOf(HttpHeaders responseHeaders) {
    return Optional.ofNullable(responseHeaders.get(HttpHeaders.SET_COOKIE)).stream()
        .flatMap(Collection::stream)
        .filter(s -> s.contains("JSESSIONID="))
        .map(s -> s.replace("JSESSIONID=", ""))
        .map(s -> s.replace("; Path=/; HttpOnly", ""))
        .findFirst()
        .orElseThrow();
  }
}
