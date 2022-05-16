package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.controller;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.Game;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.GameState;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.sideeffects.GameEventHandler;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

@RestController
@Transactional
public class FollowUpGameController {
  private final GameRepository gameRepository;
  private final GameEventHandler gameEventHandler;

  private final RepositoryEntityLinks repositoryEntityLinks;

  @Autowired
  public FollowUpGameController(
      GameRepository gameRepository,
      GameEventHandler gameEventHandler,
      RepositoryEntityLinks repositoryEntityLinks) {
    this.gameRepository = gameRepository;
    this.gameEventHandler = gameEventHandler;
    this.repositoryEntityLinks = repositoryEntityLinks;
  }

  @PostMapping("/games/{gameId}/nextGame")
  public ResponseEntity<EntityModel<Game>> createNextGame(@PathVariable Long gameId) {
    Game previousGame =
        gameRepository
            .findById(gameId)
            .orElseThrow(
                () ->
                    new HttpClientErrorException(
                        HttpStatus.NOT_FOUND,
                        "The game with the id %s does not exist.".formatted(gameId)));

    if (previousGame.getNextGame() != null) {
      throw new HttpClientErrorException(
          HttpStatus.UNPROCESSABLE_ENTITY, "nextGame already exists");
    }

    Game newGame = new Game();
    newGame.setGameState(GameState.FINDING_PLAYERS);
    newGame.setName(previousGame.getName());
    newGame = gameRepository.save(newGame);

    gameEventHandler.onAfterCreate(newGame);

    previousGame.setNextGame(newGame);
    try {
      gameRepository.save(previousGame);
    } catch (ObjectOptimisticLockingFailureException e) {
      throw new HttpClientErrorException(
          HttpStatus.CONFLICT,
          "Two people tried to update game at the same time. Reload and try again.");
    }

    EntityModel<Game> entityModel = EntityModel.of(newGame);
    Link linkToGame = repositoryEntityLinks.forType(Game::getId).linkToItemResource(newGame);
    entityModel.add(linkToGame.withSelfRel());

    return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
  }
}
