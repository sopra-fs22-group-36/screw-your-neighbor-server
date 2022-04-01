package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;

@ControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(
      value = {
        HttpClientErrorException.class,
      })
  private ResponseEntity<String> handleConflict(HttpClientErrorException e) {
    return logResponse(e, ResponseEntity.status(e.getStatusCode()).body(e.getMessage()));
  }

  private static <T> ResponseEntity<T> logResponse(Throwable e, ResponseEntity<T> response) {
    LOGGER.info(
        "Exception occured of type "
            + e.getClass()
            + " with message: "
            + e.getMessage()
            + ", returning response with code "
            + response.getStatusCode()
            + " and body: "
            + response.getBody());
    return response;
  }
}
