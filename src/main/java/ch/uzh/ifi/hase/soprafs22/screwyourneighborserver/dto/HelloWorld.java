package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.dto;

import java.util.Iterator;

public class HelloWorld implements Iterable {
  @SuppressWarnings("FieldCanBeLocal")
  private final String hello = "Hello world";

  @SuppressWarnings("unused")
  public String getHello() {
    return hello;
  }

  @Override
  public Iterator iterator() {
    return null;
  }
}
