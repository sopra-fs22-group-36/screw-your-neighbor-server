package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.security.expressions;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Card;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Game;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Participation;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Player;
import java.util.Objects;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

public class CustomMethodSecurityExpressionRoot extends SecurityExpressionRoot
    implements MethodSecurityExpressionOperations {

  private Object filterObject;
  private Object returnObject;

  public CustomMethodSecurityExpressionRoot(Authentication authentication) {
    super(authentication);
  }

  @SuppressWarnings("unused")
  public boolean playsIn(Game game) {
    Object principal = getPrincipal();
    if (!(principal instanceof Player)) {
      return false;
    }
    Player player = (Player) principal;
    return game.getParticipations().stream()
        .filter(Participation::isActive)
        .map(Participation::getPlayer)
        .anyMatch(participatingPlayer -> isSamePlayer(player, participatingPlayer));
  }

  public boolean isOwnCard(Card card) {
    Object principal = getPrincipal();
    if (!(principal instanceof Player)) {
      return false;
    }
    Player player = (Player) principal;

    Player cardOwner = card.getHand().getParticipation().getPlayer();
    return isSamePlayer(player, cardOwner);
  }

  private boolean isSamePlayer(Player player, Player participatingPlayer) {
    return participatingPlayer == player
        || Objects.equals(participatingPlayer.getId(), player.getId());
  }

  @Override
  public Object getFilterObject() {
    return this.filterObject;
  }

  @Override
  public Object getReturnObject() {
    return this.returnObject;
  }

  @Override
  public Object getThis() {
    return this;
  }

  @Override
  public void setFilterObject(Object obj) {
    this.filterObject = obj;
  }

  @Override
  public void setReturnObject(Object obj) {
    this.returnObject = obj;
  }
}
