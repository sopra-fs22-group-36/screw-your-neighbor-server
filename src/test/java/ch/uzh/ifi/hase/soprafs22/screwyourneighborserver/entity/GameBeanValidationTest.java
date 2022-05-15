package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameBeanValidationTest {

  private Validator validator;

  @BeforeEach
  void setup() {
    ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    validator = validatorFactory.getValidator();
    validatorFactory.close();
  }

  @Test
  void valid_game_is_accepted() {
    Game game = new Game();
    game.setName("test");

    assertThat(validator.validate(game), empty());
  }

  @Test
  void game_with_too_short_name_is_not_valid() {
    Game game = new Game();
    game.setName("t");

    assertThat(validator.validate(game), not(empty()));
  }

  @Test
  void game_with_too_long_name_is_not_valid() {
    Game game = new Game();
    game.setName("test5".repeat(10) + "a");

    assertThat(validator.validate(game), not(empty()));
  }

  @Test
  void game_with_html_tags_in_name_is_not_valid() {
    Game game = new Game();
    game.setName("<b>test</b>");

    assertThat(validator.validate(game), not(empty()));
  }

  @Test
  void game_with_script_tag_in_name_is_not_valid() {
    Game game = new Game();
    game.setName("<script>alert(1)</script>");

    assertThat(validator.validate(game), not(empty()));
  }
}
