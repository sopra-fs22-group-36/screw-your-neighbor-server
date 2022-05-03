package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.validation;

import static java.util.Objects.nonNull;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.*;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.ParticipationRepository;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.PlayerRepository;
import java.util.Collection;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

@Component
@RepositoryEventHandler
public class CardValidator {

  private final OldStateFetcher oldStateFetcher;
  private final PlayerRepository playerRepository;
  private final ParticipationRepository participationRepository;

  public CardValidator(
      PlayerRepository playerRepository,
      ParticipationRepository participationRepository,
      OldStateFetcher oldStateFetcher1) {

    this.playerRepository = playerRepository;
    this.participationRepository = participationRepository;
    this.oldStateFetcher = oldStateFetcher1;
  }

  @SuppressWarnings("unused")
  @HandleBeforeSave
  public void onUpdateCard(Card card) {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
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
      if (card.getHand().getParticipation().getPlayer().getId() != playerId) {
        throw new HttpClientErrorException(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "The card you're trying to play is not available in your hand.");
      }
      Collection<Card> cards = card.getHand().getCards();
      long count = cards.stream().filter(c -> nonNull(c.getRound())).count();
      if (count >= card.getRound().getRoundNumber()) {
        throw new HttpClientErrorException(
            HttpStatus.UNPROCESSABLE_ENTITY, "You already played a card in this round.");
      }
      if (!card.getHand().isTurnActive()) {
        throw new HttpClientErrorException(HttpStatus.UNPROCESSABLE_ENTITY, "It's not your turn.");
      }
      Card cardBefore = oldStateFetcher.getPreviousStateOf(card.getClass(), card.getId());
      Round otherRound = cardBefore.getRound();
      if (nonNull(otherRound)) {
        throw new HttpClientErrorException(
            HttpStatus.UNPROCESSABLE_ENTITY, "You already played this card");
      }
    }
  }
}
