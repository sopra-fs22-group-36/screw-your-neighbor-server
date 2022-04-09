package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collection;
import org.junit.jupiter.api.Test;

public class RoundTest {

  @Test
  public void get_trick_winner_test() {
    Player player_1 = new Player();
    Player player_2 = new Player();
    Player player_3 = new Player();
    player_1.setName("player_1");
    player_2.setName("player_2");
    player_3.setName("player_3");
    Game game = new Game();
    game.setName("game_1");
    Participation participation_1 = new Participation();
    Participation participation_2 = new Participation();
    Participation participation_3 = new Participation();
    participation_1.setPlayer(player_1);
    participation_2.setPlayer(player_2);
    participation_3.setPlayer(player_3);
    participation_1.setGame(game);
    participation_2.setGame(game);
    participation_3.setGame(game);
    Hand hand_1 = new Hand();
    hand_1.setParticipation(participation_1);
    Hand hand_2 = new Hand();
    hand_2.setParticipation(participation_2);
    Hand hand_3 = new Hand();
    hand_3.setParticipation(participation_3);
    Match match = new Match();
    match.setGame(game);
    Round round = new Round();
    round.setMatch(match);
    Collection<Card> cards = new ArrayList<>();
    Card c_1 = new Card(CardRank.ACE, CardSuit.CLUB);
    c_1.setHand(hand_1);
    c_1.setRound(round);
    cards.add(c_1);
    Card c_2 = new Card(CardRank.EIGHT, CardSuit.HEART);
    c_2.setHand(hand_2);
    c_2.setRound(round);
    cards.add(c_2);
    Card c_3 = new Card(CardRank.SIX, CardSuit.SPADE);
    c_3.setHand(hand_3);
    c_3.setRound(round);
    cards.add(c_3);
    round.setCards(cards);
    round.setTrickWinner();
    // card_1 represents a fourth card being played i.e. triggering the
    assertEquals(round.getTrickWinner().getPlayer().getName(), player_1.getName());
    // exchange cards of two hands, so there is another winner of the trick:
    c_1.setHand(hand_2);
    c_2.setHand(hand_1);
    round.setTrickWinner();
    assertEquals(round.getTrickWinner().getPlayer().getName(), player_2.getName());
  }
}
