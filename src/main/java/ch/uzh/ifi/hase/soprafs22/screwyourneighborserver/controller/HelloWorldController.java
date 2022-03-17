package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.controller;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.dto.HelloWorld;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldController {
  @GetMapping("/")
  @ResponseStatus(HttpStatus.OK)
  public HelloWorld helloWorld() {
    return new HelloWorld();
  }
}
