package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.security.expressions;

import static ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.CardValue.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.*;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util.GameBuilder;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

class CustomMethodSecurityExpressionRootTest {
  private static final String PLAYER_NAME_1 = "player1";
  private static final String PLAYER_NAME_2 = "player2";

  private Player playerInSession = null;
  private CustomMethodSecurityExpressionRoot customMethodSecurityExpressionRoot;
  private Player player1;
  private Player player2;
  private Player playerNotInGame;
  private List<Card> cardsPlayer1;
  private List<Card> cardsPlayer2;

  @BeforeEach
  void setup() {
    Authentication authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenAnswer(__ -> playerInSession);

    customMethodSecurityExpressionRoot = new CustomMethodSecurityExpressionRoot(authentication);

    player1 = new Player();
    player1.setId(1L);
    player1.setName(PLAYER_NAME_1);

    player2 = new Player();
    player1.setId(2L);
    player2.setName(PLAYER_NAME_2);

    playerNotInGame = new Player();
    player1.setId(3L);
    playerNotInGame.setName("playerNotInGame");

    Game game =
        GameBuilder.builder("game1")
            .withParticipationWith(player1)
            .withParticipationWith(player2)
            .withMatch()
            .withHandForPlayer(PLAYER_NAME_1)
            .withCards(ACE_OF_CLUBS, KING_OF_HEARTS)
            .finishHand()
            .withHandForPlayer(PLAYER_NAME_2)
            .finishHand()
            .withRound()
            .withPlayedCard(PLAYER_NAME_1, ACE_OF_CLUBS)
            .finishRound()
            .finishMatch()
            .build();

    cardsPlayer1 =
        game.getParticipations().stream()
            .filter(participation -> participation.getPlayer() == player1)
            .map(Participation::getHands)
            .flatMap(Collection::stream)
            .map(Hand::getCards)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

    cardsPlayer2 =
        game.getParticipations().stream()
            .filter(participation -> participation.getPlayer() == player2)
            .map(Participation::getHands)
            .flatMap(Collection::stream)
            .map(Hand::getCards)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
  }

  @Test
  void when_player_is_not_part_of_the_game_isOwnCard_returns_false() {
    playerInSession = playerNotInGame;

    cardsPlayer1.forEach(card -> assertFalse(customMethodSecurityExpressionRoot.isOwnCard(card)));
    cardsPlayer2.forEach(card -> assertFalse(customMethodSecurityExpressionRoot.isOwnCard(card)));
  }

  @Test
  void when_no_player_in_session_isOwnCard_returns_false() {
    playerInSession = null;

    cardsPlayer1.forEach(card -> assertFalse(customMethodSecurityExpressionRoot.isOwnCard(card)));
    cardsPlayer2.forEach(card -> assertFalse(customMethodSecurityExpressionRoot.isOwnCard(card)));
  }

  @Test
  void cards_of_player1_belong_to_player1() {
    playerInSession = player1;

    cardsPlayer1.forEach(card -> assertTrue(customMethodSecurityExpressionRoot.isOwnCard(card)));
    cardsPlayer2.forEach(card -> assertFalse(customMethodSecurityExpressionRoot.isOwnCard(card)));
  }

  @Test
  void cards_of_player2_belong_to_player2() {
    playerInSession = player2;

    cardsPlayer1.forEach(card -> assertFalse(customMethodSecurityExpressionRoot.isOwnCard(card)));
    cardsPlayer2.forEach(card -> assertTrue(customMethodSecurityExpressionRoot.isOwnCard(card)));
  }
}
