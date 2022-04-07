package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.validation;

import javax.persistence.EntityManager;

public class OldStateFetcher {
  private final EntityManager entityManager;

  public OldStateFetcher(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  public <T> T getPreviousStateOf(Class<T> className, Long id) {
    T oldState = entityManager.getReference(className, id);
    entityManager.refresh(oldState);
    if (oldState == null) {
      throw new IllegalStateException(
          "This class should only be used on patch requests, were the entity was there before");
    }
    return oldState;
  }
}
