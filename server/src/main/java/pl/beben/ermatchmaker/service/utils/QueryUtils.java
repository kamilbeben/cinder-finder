package pl.beben.ermatchmaker.service.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QueryUtils {

  private static final String EMPTY = "";

  public static boolean matches(String text, String query) {
    return
      query == null ||
      (
        text != null &&
        normalize(text).contains(normalize(query))
      );
  }
  
  public static int compareByQuery(String leftText, String rightText, String query) {
    
    if (query == null)
      return 0;
    
    final var normalizedQuery = normalize(query);
    final var normalizedLeftText = Optional.ofNullable(normalize(leftText)).orElse(EMPTY);
    final var normalizedRightText = Optional.ofNullable(normalize(rightText)).orElse(EMPTY);

    final var leftTextStartsWithQuery = normalizedLeftText.startsWith(normalizedQuery);
    final var indexOfQueryInLeftText = normalizedLeftText.indexOf(normalizedQuery);
    final var rightTextStartsWithQuery = normalizedRightText.startsWith(normalizedQuery);
    final var indexOfQueryInRightText = normalizedRightText.indexOf(normalizedQuery);
    
    if (leftTextStartsWithQuery || rightTextStartsWithQuery)
      return leftTextStartsWithQuery
        ? -1
        : 1;
    
    if (indexOfQueryInLeftText != -1 && indexOfQueryInRightText != -1)
      return Integer.compare(indexOfQueryInLeftText, indexOfQueryInRightText);
    
    if (indexOfQueryInLeftText != -1 || indexOfQueryInRightText != -1)
      return indexOfQueryInLeftText != -1
        ? -1
        : 1;
      
    return 0;
  }
  
  public static String normalize(String text) {
    return text == null
      ? null
      : StringUtils.deleteAny(
          StringUtils.trimAllWhitespace(
            text.toLowerCase()
          ),
          ";'\",._-"
        );
  }
  
}
