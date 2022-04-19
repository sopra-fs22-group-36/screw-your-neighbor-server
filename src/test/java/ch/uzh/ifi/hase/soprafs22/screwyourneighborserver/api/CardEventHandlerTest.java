package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.*;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.*;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.sideeffects.CardEventHandler;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
  private CardEventHandler cardEventHandler = new CardEventHandler(roundRepoMock, cardRepoMock);
  // @Mock
  // private CardEventHandler cardEventHandler = new CardEventHandler(roundRepo, cardRepo);
  // private CardEventHandler cardEventHandler =
  //        Mockito.mock(CardEventHandler.class, withSettings().useConstructor(roundRepo,
  // cardRepo));

  Match matchMock = Mockito.mock(Match.class);
  Round roundMock = Mockito.mock(Round.class);
  CardRank cardRank = CardRank.ACE;
  CardSuit cardSuit = CardSuit.DIAMOND;
  Card cardMock = Mockito.mock(Card.class, withSettings().useConstructor(cardRank, cardSuit));

  Participation participation_1 = new Participation();
  Participation participation_2 = new Participation();
  Collection<Participation> participations = new ArrayList<>();
  Game game = new Game();
  Match match = new Match();
  Round round = new Round();
  Card card = new Card(cardRank, cardSuit);

  @BeforeEach
  @AfterEach
  public void setup() {}

  @Test
  public void play_not_last_card_no_new_round() {
    Mockito.doReturn(roundMock).when(cardMock).getRound();
    Mockito.doReturn(matchMock).when(roundMock).getMatch();
    Mockito.doReturn(1).when(cardEventHandler).getNumberOfPlayedCards(any(Round.class));
    Mockito.doReturn(2).when(cardEventHandler).getNumberOfPlayers(any(Round.class));
    cardEventHandler.handleAfterSave(cardMock);
    verify(cardEventHandler, never()).createRound(any(Match.class), anyInt());
    verify(cardEventHandler, never()).setOldRoundToInactive(any(Round.class));
  }

  @Test
  public void play_last_card_new_round() {
    Mockito.doReturn(roundMock).when(cardMock).getRound();
    Mockito.doReturn(matchMock).when(roundMock).getMatch();
    Mockito.doReturn(2).when(cardEventHandler).getNumberOfPlayedCards(any(Round.class));
    Mockito.doReturn(2).when(cardEventHandler).getNumberOfPlayers(any(Round.class));
    cardEventHandler.handleAfterSave(cardMock);
    verify(cardEventHandler, times(1)).createRound(any(Match.class), anyInt());
    verify(cardEventHandler, times(1)).setOldRoundToInactive(any(Round.class));
  }

  @Test
  public void create_new_round() {
    setUpRelatedEntities();
    CardEventHandler cardEventHandler = new CardEventHandler(roundRepository, cardRepository);
    Round newRound = cardEventHandler.createRound(match, round.getRoundNumber());
    assertEquals(2, newRound.getRoundNumber());
    Collection<Round> savedRounds = roundRepository.findAll();
    assertTrue(savedRounds.stream().anyMatch(r -> r.getRoundNumber() == 2));
  }

  @Test
  public void get_number_of_played_cards_one_card() {
    setUpRelatedEntities();
    CardEventHandler cardEventHandler = new CardEventHandler(roundRepository, cardRepository);
    assertEquals(1, cardEventHandler.getNumberOfPlayedCards(round));
  }

  @Test
  public void get_number_of_played_cards_no_card() {
    matchRepository.save(match);
    round.setMatch(match);
    round.setRoundNumber(1);
    roundRepository.save(round);
    CardEventHandler cardEventHandler = new CardEventHandler(roundRepository, cardRepository);
    assertEquals(0, cardEventHandler.getNumberOfPlayedCards(round));
  }

  @Test
  public void get_number_of_played_cards_multiple_cards() {
    setUpRelatedEntities();
    cardRank = CardRank.EIGHT;
    cardSuit = CardSuit.HEART;
    Card card2 = new Card(cardRank, cardSuit);
    card2.setRound(round);
    cardRepository.save(card2);
    card.setRound(round);
    cardRank = CardRank.TEN;
    cardSuit = CardSuit.HEART;
    Card card3 = new Card(cardRank, cardSuit);
    card3.setRound(round);
    // a fourth round which is not assigned to this round (and therefore shall not be counted)
    cardRank = CardRank.TEN;
    cardSuit = CardSuit.SPADE;
    Card card4 = new Card(cardRank, cardSuit);
    cardRepository.save(card3);
    CardEventHandler cardEventHandler = new CardEventHandler(roundRepository, cardRepository);
    assertEquals(3, cardEventHandler.getNumberOfPlayedCards(round));
  }

  @Test
  public void set_old_round_to_inactive() {
    setUpRelatedEntities();
    CardEventHandler cardEventHandler = new CardEventHandler(roundRepository, cardRepository);
    cardEventHandler.setOldRoundToInactive(round);
    Collection<Round> savedRounds = roundRepository.findAll();
    assertTrue(savedRounds.stream().anyMatch(r -> r.isActive() == false));
  }

  public void setUpRelatedEntities() {
    matchRepository.save(match);
    round.setMatch(match);
    round.setRoundNumber(1);
    roundRepository.save(round);
    card.setRound(round);
    cardRepository.save(card);
  }
}
