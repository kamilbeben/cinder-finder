package pl.beben.ermatchmaker.service.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QueryUtils {
  
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
