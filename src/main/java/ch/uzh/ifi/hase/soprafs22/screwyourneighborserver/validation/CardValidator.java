package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.validation;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.*;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.PlayerRepository;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

@Component
@RepositoryEventHandler
public class CardValidator {

  private final OldStateFetcher oldStateFetcher;
  private final PlayerRepository playerRepository;

  @Autowired
  public CardValidator(PlayerRepository playerRepository, OldStateFetcher oldStateFetcher) {

    this.playerRepository = playerRepository;
    this.oldStateFetcher = oldStateFetcher;
  }

  @SuppressWarnings({"unused", "SpringElInspection", "ELValidationInspection"})
  @HandleBeforeSave
  @PreAuthorize("isOwnCard(#card)")
  public void onUpdateCard(@P("card") Card card) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Round newRound = card.getRound();
    if (!(authentication.getPrincipal() instanceof Player)) {
      throw new HttpClientErrorException(
          HttpStatus.UNAUTHORIZED, "You're not authorized to play in this game.");
    } else {
      Long playerId = ((Player) authentication.getPrincipal()).getId();
      Player player =
          playerRepository
              .findById(playerId)
              .orElseThrow(
                  () ->
                      new HttpClientErrorException(
                          HttpStatus.UNAUTHORIZED, "You're not an authorized player"));
      if (!card.getHand().getParticipation().getPlayer().getId().equals(playerId)) {
        throw new HttpClientErrorException(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "The card you're trying to play is not available in your hand.");
      }
      if (isNull(card.getHand().getAnnouncedScore())
          || card.getHand().getMatch().getMatchState() == MatchState.ANNOUNCING) {
        throw new HttpClientErrorException(
            HttpStatus.UNPROCESSABLE_ENTITY, "You can not play a card in the announcing round.");
      }
      Card cardBefore = oldStateFetcher.getPreviousStateOf(card.getClass(), card.getId());
      Hand hand_before = cardBefore.getHand();
      if (!hand_before.isTurnActive()) {
        throw new HttpClientErrorException(HttpStatus.UNPROCESSABLE_ENTITY, "It's not your turn.");
      }
      Collection<Card> cards = card.getHand().getCards();
      long count = cards.stream().filter(c -> nonNull(c.getRound())).count();
      if (count > card.getRound().getRoundNumber()) {
        throw new HttpClientErrorException(
            HttpStatus.UNPROCESSABLE_ENTITY, "You already played a card in this round.");
      }
      Round roundBefore = cardBefore.getRound();
      if (nonNull(roundBefore)) {
        throw new HttpClientErrorException(
            HttpStatus.UNPROCESSABLE_ENTITY, "You already played this card");
      }
    }
  }
}
