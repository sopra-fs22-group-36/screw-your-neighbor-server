package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.*;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.*;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.sideeffects.CardEventHandler;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CardEventHandlerTest {

  private RoundRepository roundRepoMock = mock(RoundRepository.class);
  private CardRepository cardRepoMock = mock(CardRepository.class);
  @Autowired private RoundRepository roundRepository;
  @Autowired private MatchRepository matchRepository;
  @Autowired private CardRepository cardRepository;
  @Autowired private GameRepository gameRepository;
  @Autowired private ParticipationRepository participationRepository;

  @Spy
  private CardEventHandler cardEventHandlerSpy = new CardEventHandler(roundRepoMock, cardRepoMock);

  private Participation participation1;
  private Participation participation2;
  private Participation participation3;
  private Game game;
  private Match match;
  private Round round;
  private Card card1;
  private Card card2;
  private Card card3;
  private CardEventHandler cardEventHandler;

  @BeforeEach
  void setup() {
    roundRepository.deleteAll();
    cardRepository.deleteAll();
    participation1 = new Participation();
    participation2 = new Participation();
    participation3 = new Participation();
    game = new Game();
    match = new Match();
    round = new Round();
    CardRank cardRank = CardRank.ACE;
    CardSuit cardSuit = CardSuit.DIAMOND;
    card1 = new Card(cardRank, cardSuit);
    cardRank = CardRank.EIGHT;
    cardSuit = CardSuit.DIAMOND;
    card2 = new Card(cardRank, cardSuit);
    cardRank = CardRank.EIGHT;
    cardSuit = CardSuit.HEART;
    card3 = new Card(cardRank, cardSuit);
    cardEventHandler = new CardEventHandler(roundRepository, cardRepository);
    matchRepository.save(match);
    round.setMatch(match);
    round.setRoundNumber(1);
    roundRepository.save(round);

    participation1.setParticipationNumber(1);
    participation1.setGame(game);
    game.getParticipations().add(participation1);
    participation2.setParticipationNumber(2);
    participation2.setGame(game);
    game.getParticipations().add(participation2);
    participation3.setParticipationNumber(3);
    participation3.setGame(game);
    game.getParticipations().add(participation3);
    match.setGame(game);
    round.setMatch(match);
    round.setRoundNumber(1);
  }

  @Test
  void play_first_card_no_new_round() {
    card1.setRound(round);
    cardRepository.save(card1);
    // there's a random second card, with no round set (i.e. not played)
    cardRepository.save(card2);
    cardEventHandler.handleAfterSave(card1);
    Collection<Round> savedRounds = roundRepository.findAll();
    // no new round will be created, as there are there players in the game, but only one card
    // played
    assertEquals(1, savedRounds.size());
    // current round still active
    assertTrue(savedRounds.stream().anyMatch(r -> r.isActive() == true));
    // no round set to inactive yet
    assertFalse(savedRounds.stream().anyMatch(r -> r.isActive() == false));
  }

  @Test
  void play_not_last_card_no_new_round() {
    // two cards are already played
    card1.setRound(round);
    cardRepository.save(card1);
    card2.setRound(round);
    cardRepository.save(card2);
    // there's a random third card, with no round set (i.e. not played)
    cardRepository.save(card3);
    cardEventHandler.handleAfterSave(card2);
    Collection<Round> savedRounds = roundRepository.findAll();
    assertEquals(1, savedRounds.size());
    // current round still active
    assertTrue(savedRounds.stream().anyMatch(r -> r.isActive() == true));
    // no round set to inactive yet
    assertFalse(savedRounds.stream().anyMatch(r -> r.isActive() == false));
  }

  @Test
  void play_last_card_new_round() {
    // the cards of all three players are already assigned to the round too (i.e. has been played)
    card1.setRound(round);
    cardRepository.save(card1);
    card2.setRound(round);
    cardRepository.save(card2);
    card3.setRound(round);
    cardRepository.save(card3);
    cardEventHandler.handleAfterSave(card3);
    Collection<Round> savedRounds = roundRepository.findAll();
    assertEquals(2, savedRounds.size());
    // old round was set to inactive
    assertTrue(savedRounds.stream().anyMatch(r -> r.isActive() == false));
    // the new round is now active
    assertTrue(savedRounds.stream().anyMatch(r -> r.isActive() == true));
  }
}
