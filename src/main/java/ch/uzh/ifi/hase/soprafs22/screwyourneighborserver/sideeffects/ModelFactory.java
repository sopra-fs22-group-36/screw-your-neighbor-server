package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.sideeffects;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.*;
import java.util.Collection;
import org.springframework.stereotype.Component;

@Component
public class ModelFactory {

  Hand createHand(Match match, Participation participation) {
    Hand hand = new Hand();
    hand.setParticipation(participation);
    participation.getHands().add(hand);
    hand.setMatch(match);
    match.getHands().add(hand);
    return hand;
  }

  Match createMatch(Game game) {
    Match match = new Match();
    match.setMatchNumber(1);
    match.setMatchState(MatchState.ANNOUNCING);
    match.setGame(game);
    game.getMatches().add(match);
    return match;
  }

  Round addRound(Match match, int lastRoundNumber) {
    Round round = new Round();
    round.setRoundNumber(lastRoundNumber + 1);
    round.setMatch(match);
    match.getRounds().add(round);
    return round;
  }

  void addCardTo(Hand hand, Card card) {
    card.setHand(hand);
    hand.getCards().add(card);
  }

  void assignParticipationNumbers(Game game) {
    Collection<Participation> part = game.getParticipations();
    int i = 0;
    for (var p : part) {
      p.setParticipationNumber(i);
      i++;
    }
  }
}
