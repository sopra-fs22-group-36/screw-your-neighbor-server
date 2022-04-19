package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.*;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.*;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.sideeffects.CardEventHandler;
import java.lang.reflect.InvocationTargetException;
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
  public void setup() {
    roundRepository.deleteAll();
    cardRepository.deleteAll();
  }

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
    cardRank = CardRank.TEN;
    cardSuit = CardSuit.HEART;
    Card card3 = new Card(cardRank, cardSuit);
    card3.setRound(round);
    cardRepository.save(card3);
    // a fourth round which is not assigned to this round (and therefore shall not be counted)
    cardRank = CardRank.TEN;
    cardSuit = CardSuit.SPADE;
    Card card4 = new Card(cardRank, cardSuit);
    cardRepository.save(card4);
    CardEventHandler cardEventHandler = new CardEventHandler(roundRepository, cardRepository);
    assertEquals(3, cardEventHandler.getNumberOfPlayedCards(round));
  }

  @Test
  public void set_old_round_to_inactive()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    setUpRelatedEntities();
    CardEventHandler cardEventHandler = new CardEventHandler(roundRepository, cardRepository);
    /*
    Accessing private Methods for testing:
    Method privateMethod = CardEventHandler.class.getDeclaredMethod("setOldRoundToInactive", Round.class);
    privateMethod.setAccessible(true);
    privateMethod.invoke(cardEventHandler, round);
     */
    cardEventHandler.setOldRoundToInactive(round);
    Collection<Round> savedRounds = roundRepository.findAll();
    assertTrue(savedRounds.stream().anyMatch(r -> r.isActive() == false));
    // and no remaining round which is active (as we did not create a new round in this test)
    assertFalse(savedRounds.stream().anyMatch(r -> r.isActive() == true));
  }

  /* How do I have to setup the security context correctly to create/save a game?
  @Test
  @WithMockUser(username="player1", roles={"ROLE_PLAYER"})
  public void get_number_of_players() {
    game.setName("game1");
    gameRepository.save(game);
    participation_1.setParticipationNumber(1);
    participation_1.setGame(game);
    participation_2.setParticipationNumber(2);
    participation_2.setGame(game);
    participationRepository.save(participation_1);
    participationRepository.save(participation_2);
    match.setGame(game);
    matchRepository.save(match);
    round.setMatch(match);
    round.setRoundNumber(1);
    roundRepository.save(round);
    card.setRound(round);
    cardRepository.save(card);
    Round savedRound = roundRepository.findAll().get(0);
    assertEquals(2, cardEventHandler.getNumberOfPlayers(savedRound));
  }*/

  public void setUpRelatedEntities() {
    matchRepository.save(match);
    round.setMatch(match);
    round.setRoundNumber(1);
    roundRepository.save(round);
    card.setRound(round);
    cardRepository.save(card);
  }
}
