package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RoundTest {

  private Game game = new Game();
  private Player player_1 = new Player();
  private Player player_2 = new Player();
  private Player player_3 = new Player();
  private Participation participation_1 = new Participation();
  private Participation participation_2 = new Participation();
  private Participation participation_3 = new Participation();
  private Hand hand_1 = new Hand();
  private Hand hand_2 = new Hand();
  private Hand hand_3 = new Hand();
  private Match match = new Match();
  private Round round = new Round();
  Collection<Card> cards = new ArrayList<>();
  Card c_1 = new Card(CardRank.QUEEN, CardSuit.CLUB);
  Card c_2 = new Card(CardRank.EIGHT, CardSuit.HEART);
  Card c_3 = new Card(CardRank.SIX, CardSuit.SPADE);
  Card c_4 = new Card(CardRank.QUEEN, CardSuit.DIAMOND);
  Card c_5 = new Card(CardRank.ACE, CardSuit.CLUB);

  @BeforeEach
  void setup() {
    player_1.setName("player_1");
    player_1.setId(1L);
    player_2.setName("player_2");
    player_2.setId(2L);
    player_3.setName("player_3");
    player_3.setId(3L);
    game.setName("game_1");
    participation_1.setPlayer(player_1);
    participation_2.setPlayer(player_2);
    participation_3.setPlayer(player_3);
    participation_1.setGame(game);
    participation_2.setGame(game);
    participation_3.setGame(game);
    hand_1.setParticipation(participation_1);
    hand_2.setParticipation(participation_2);
    hand_3.setParticipation(participation_3);
    match.setGame(game);
    round.setMatch(match);
    c_1.setHand(hand_1);
    c_1.setRound(round);
    cards.add(c_1);
    c_2.setHand(hand_2);
    c_2.setRound(round);
    cards.add(c_2);
    c_3.setHand(hand_3);
    c_3.setRound(round);
    cards.add(c_3);
  }

  @Test
  void get_trick_winner_test() {
    round.setCards(cards);
    assertEquals(round.getTrickWinnerIds().size(), 1);
    assertTrue(round.getTrickWinnerIds().contains(player_1.getId()));
  }

  @Test
  void get_multiple_trick_winner_test() {
    Player player_4 = new Player();
    player_4.setName("player_4");
    player_4.setId(4L);
    Participation participation_4 = new Participation();
    participation_4.setPlayer(player_4);
    participation_4.setGame(game);
    Hand hand_4 = new Hand();
    hand_4.setParticipation(participation_4);
    c_4.setHand(hand_4);
    c_4.setRound(round);
    cards.add(c_4);

    round.setCards(cards);
    assertEquals(round.getTrickWinnerIds().size(), 2);
    assertTrue(round.getTrickWinnerIds().contains(player_1.getId()));
    assertTrue(round.getTrickWinnerIds().contains(player_4.getId()));
    assertFalse(round.getTrickWinnerIds().contains(player_3.getId()));
  }

  @Test
  void get_multiple_same_cards_with_one_trick_winner_test() {

    Player player_4 = new Player();
    player_4.setName("player_4");
    player_4.setId(4L);
    Participation participation_4 = new Participation();
    participation_4.setPlayer(player_4);
    participation_4.setGame(game);
    Hand hand_4 = new Hand();
    hand_4.setParticipation(participation_4);
    c_4.setHand(hand_4);
    c_4.setRound(round);
    cards.add(c_4);

    Player player_5 = new Player();
    player_5.setName("player_5");
    player_5.setId(5L);
    Participation participation_5 = new Participation();
    participation_5.setPlayer(player_5);
    participation_5.setGame(game);
    Hand hand_5 = new Hand();
    hand_5.setParticipation(participation_5);
    c_5.setHand(hand_5);
    c_5.setRound(round);
    cards.add(c_5);

    round.setCards(cards);
    assertEquals(round.getTrickWinnerIds().size(), 1);
    assertTrue(round.getTrickWinnerIds().contains(player_5.getId()));
  }

  @Test
  void empty_cards_list_returns_empty_winner_list() {
    Round emptyRound = new Round();
    assertEquals(emptyRound.getTrickWinnerIds(), List.of());
  }
}
