package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.web;

import javax.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
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

  @ExceptionHandler(value = {ConstraintViolationException.class})
  private ResponseEntity<String> handleConstraintViolation(ConstraintViolationException e) {
    return ResponseEntity.badRequest().body(e.getMessage());
  }

  @ExceptionHandler(value = {DataIntegrityViolationException.class})
  private ResponseEntity<String> handleDataIntegrityViolationException(
      DataIntegrityViolationException e) {
    return ResponseEntity.unprocessableEntity().body(e.getMessage());
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
