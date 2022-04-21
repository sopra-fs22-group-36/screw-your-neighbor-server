package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.sideeffects;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.*;
import org.springframework.stereotype.Component;

@Component
public class ModelFactory {

  Hand addHand(Match match, Participation participation) {
    Hand hand = new Hand();
    hand.setParticipation(participation);
    participation.getHands().add(hand);
    hand.setMatch(match);
    match.getHands().add(hand);
    return hand;
  }

  Match addMatch(Game game, int newMatchNumber) {
    Match match = new Match();
    match.setMatchNumber(newMatchNumber);
    match.setMatchState(MatchState.ANNOUNCING);
    match.setGame(game);
    game.getMatches().add(match);
    return match;
  }

  Round addRound(Match match, int newRoundNumber) {
    Round round = new Round();
    round.setRoundNumber(newRoundNumber);
    round.setMatch(match);
    match.getRounds().add(round);
    return round;
  }

  void addCardTo(Hand hand, Card card) {
    card.setHand(hand);
    hand.getCards().add(card);
  }
}
