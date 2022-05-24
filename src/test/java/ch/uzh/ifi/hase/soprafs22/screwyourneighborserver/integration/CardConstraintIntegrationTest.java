package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.integration;

import static ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.CardValue.*;
import static org.junit.jupiter.api.Assertions.*;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.*;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.*;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.ClearDBAfterTestListener;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.GameBuilder;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.WithPersistedPlayer;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;

@SpringBootTest
@TestExecutionListeners(
    value = {ClearDBAfterTestListener.class},
    mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class CardConstraintIntegrationTest {
  @Autowired private CardRepository cardRepository;
  @Autowired private GameRepository gameRepository;
  @Autowired private ParticipationRepository participationRepository;
  @Autowired private PlayerRepository playerRepository;
  private static final String PLAYER_NAME_1 = "player1";

  @Test
  @WithPersistedPlayer(playerName = PLAYER_NAME_1)
  void one_player_plays_multiple_cards_in_one_round_throws_error() {
    Player player = playerRepository.findByName(PLAYER_NAME_1).orElseThrow();

    Game game =
        GameBuilder.builder("game1", gameRepository, participationRepository, playerRepository)
            .withParticipationWith(player)
            .withGameState(GameState.PLAYING)
            .withMatch()
            .withMatchState(MatchState.PLAYING)
            .withHandForPlayer(player.getName())
            .withCards(ACE_OF_CLUBS, QUEEN_OF_CLUBS)
            .withAnnouncedScore(1)
            .finishHand()
            .withRound()
            .withPlayedCard(player.getName(), ACE_OF_CLUBS)
            .finishRound()
            .finishMatch()
            .build();

    game = gameRepository.saveAll(List.of(game)).get(0);
    Hand hand =
        game.getLastMatch().orElseThrow().getHands().stream()
            .filter(
                h ->
                    h.getCards().stream().filter(c -> c.getCardRank() == CardRank.QUEEN).count()
                        > 0)
            .findAny()
            .orElseThrow();
    Card card =
        hand.getCards().stream()
            .filter(c -> c.getCardRank() == CardRank.QUEEN)
            .findAny()
            .orElseThrow();
    Round round = game.getLastMatch().orElseThrow().getLastRound().orElseThrow();
    card.setRound(round);
    Exception exception =
        assertThrows(
            org.springframework.dao.DataIntegrityViolationException.class,
            () -> cardRepository.save(card));
  }

  @Test
  void change_cards_value_does_not_work() {
    Card card = new Card(CardRank.QUEEN, CardSuit.HEART);
    cardRepository.saveAll(List.of(card));
    Collection<Card> cardsBeforeSave = cardRepository.findAll();
    Card cardBeforeSave = cardsBeforeSave.stream().findAny().get();
    card.setCardRank(CardRank.KING);
    cardRepository.saveAll(List.of(card));
    Collection<Card> cardsAfterSave = cardRepository.findAll();
    Card cardAfterSave = cardsAfterSave.stream().findAny().get();

    assertEquals(1, cardsBeforeSave.size());
    assertEquals(CardRank.QUEEN, cardBeforeSave.getCardRank());
    assertEquals(CardSuit.HEART, cardAfterSave.getCardSuit());
    assertEquals(1, cardsAfterSave.size());
    assertEquals(CardRank.QUEEN, cardAfterSave.getCardRank());
    assertEquals(CardSuit.HEART, cardAfterSave.getCardSuit());
  }
}
