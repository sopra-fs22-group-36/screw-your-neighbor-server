package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.sideeffects;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.*;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.CardRepository;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.HandRepository;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.MatchRepository;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.RoundRepository;
import java.util.Collection;

public class InstanceCreator {

  private CardRepository cardRepo = null;
  private RoundRepository roundRepo = null;
  private MatchRepository matchRepo = null;
  private HandRepository handRepo = null;

  public InstanceCreator(
      RoundRepository roundRepo,
      CardRepository cardRepo,
      MatchRepository matchRepo,
      HandRepository handRepo) {
    this.roundRepo = roundRepo;
    this.cardRepo = cardRepo;
    this.matchRepo = matchRepo;
    this.handRepo = handRepo;
  }

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

  Round createRound(Match match, int lastRoundNumber) {
    Round round = new Round();
    round.setRoundNumber(lastRoundNumber + 1);
    round.setMatch(match);
    match.getRounds().add(round);
    return round;
  }

  Card createCard(Hand hand, CardDeck cardDeck) {
    Card card = cardDeck.drawCard();
    card.setHand(hand);
    hand.getCards().add(card);
    return card;
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
