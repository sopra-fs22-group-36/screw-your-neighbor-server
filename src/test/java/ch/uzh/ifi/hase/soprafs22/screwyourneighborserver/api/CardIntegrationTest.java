package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.*;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.*;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.sideeffects.CardEventHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CardIntegrationTest {

  private RoundRepository roundRepo = mock(RoundRepository.class);
  private CardRepository cardRepo = mock(CardRepository.class);

  @Spy private CardEventHandler cardEventHandler = new CardEventHandler(roundRepo, cardRepo);
  // @Mock
  // private CardEventHandler cardEventHandler = new CardEventHandler(roundRepo, cardRepo);
  // private CardEventHandler cardEventHandler =
  //        Mockito.mock(CardEventHandler.class, withSettings().useConstructor(roundRepo,
  // cardRepo));

  Match match = Mockito.mock(Match.class);
  Round round = Mockito.mock(Round.class);
  CardRank cardRank = CardRank.ACE;
  CardSuit cardSuit = CardSuit.DIAMOND;
  Card card_1 = Mockito.mock(Card.class, withSettings().useConstructor(cardRank, cardSuit));

  @BeforeEach
  @AfterEach
  public void setup() {
    Mockito.doReturn(round).when(card_1).getRound();
    Mockito.doReturn(match).when(round).getMatch();
  }

  @Test
  public void play_not_last_card_no_new_round() {
    Mockito.doReturn(1).when(cardEventHandler).getNumberOfPlayedCards(any(Round.class));
    Mockito.doReturn(2).when(cardEventHandler).getNumberOfPlayers(any(Round.class));
    cardEventHandler.handleAfterSave(card_1);
    verify(cardEventHandler, never()).createRound(any(Match.class), anyInt());
  }

  @Test
  public void play_last_card_new_round() {
    Mockito.doReturn(2).when(cardEventHandler).getNumberOfPlayedCards(any(Round.class));
    Mockito.doReturn(2).when(cardEventHandler).getNumberOfPlayers(any(Round.class));
    cardEventHandler.handleAfterSave(card_1);
    verify(cardEventHandler, times(1)).createRound(any(Match.class), anyInt());
  }
}
