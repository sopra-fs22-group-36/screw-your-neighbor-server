package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;

import java.util.Collection;
import java.util.stream.Stream;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class GameBeanValidationTest {

  private Validator validator;

  @BeforeEach
  void setup() {
    ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    validator = validatorFactory.getValidator();
    validatorFactory.close();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("gameNames")
  void validates_game_name(String name, ValidationListMatcher validationListMatcher) {
    Game game = new Game();
    if (name != null) {
      game.setName(name);
    }

    assertThat(validator.validate(game), validationListMatcher);
  }

  private static Stream<Arguments> gameNames() {
    return Stream.of(
            Arguments.of("test", empty()),
            Arguments.of(null, not(empty())),
            Arguments.of("t", not(empty())),
            Arguments.of("test5".repeat(10) + "a", not(empty())),
            Arguments.of("<b>test</b>", not(empty())),
            Arguments.of("<script>alert(1)</script>", not(empty())))
        .map(
            arguments ->
                Arguments.of(arguments.get()[0], ValidationListMatcher.from(arguments.get()[1])));
  }

  @Test
  void null_gameState_is_invalid() {
    Game game = new Game();
    game.setName("test");
    game.setGameState(null);

    assertThat(validator.validate(game), not(empty()));
  }

  private interface ValidationListMatcher
      extends Matcher<Collection<? extends ConstraintViolation<Game>>> {

    static ValidationListMatcher from(Object object) {
      //noinspection unchecked
      return from((Matcher<Collection<? extends ConstraintViolation<Game>>>) object);
    }

    static ValidationListMatcher from(
        Matcher<Collection<? extends ConstraintViolation<Game>>> source) {
      return new ValidationListMatcher() {
        @Override
        public boolean matches(Object actual) {
          return source.matches(actual);
        }

        @Override
        public void describeMismatch(Object actual, Description mismatchDescription) {
          source.describeMismatch(actual, mismatchDescription);
        }

        @Override
        public void _dont_implement_Matcher___instead_extend_BaseMatcher_() {}

        @Override
        public void describeTo(Description description) {
          source.describeTo(description);
        }
      };
    }
  }
}
