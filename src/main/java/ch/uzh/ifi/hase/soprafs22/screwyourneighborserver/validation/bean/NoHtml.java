package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.validation.bean;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = NoHtmlValidator.class)
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface NoHtml {
  String message() default "Unsafe html content";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
