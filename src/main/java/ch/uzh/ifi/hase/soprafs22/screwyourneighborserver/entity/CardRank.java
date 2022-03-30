package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

public enum CardRank {
  ACE("ace", 9),
  KING("king", 8),
  QUEEN("queen", 7),
  JACK("jack", 6),
  TEN("ten", 5),
  NINE("nine", 4),
  EIGHT("eight", 3),
  SEVEN("seven", 2),
  SIX("six", 1);

  private final String key;
  private final Integer value;

  CardRank(String key, Integer value) {
    this.key = key;
    this.value = value;
  }

  public String getKey() {
    return key;
  }

  public Integer getValue() {
    return value;
  }
}
