package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.validation.bean;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public class NoHtmlValidator implements ConstraintValidator<NoHtml, String> {
  @Override
  public boolean isValid(String value, ConstraintValidatorContext ctx) {
    return value == null || Jsoup.isValid(value, Safelist.none());
  }
}
