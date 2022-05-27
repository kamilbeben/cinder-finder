package pl.beben.ermatchmaker.service.utils;

import lombok.AllArgsConstructor;
import lombok.ToString;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class QueryUtilsTest {

  @ToString
  @AllArgsConstructor
  class QueryUtilsTestCase {
    String query;
    List<String> input;
    List<String> output;
  }
  
  List<QueryUtilsTestCase> testCases = Arrays.asList(
    new QueryUtilsTestCase(
      "aNdR",
      Arrays.asList("Konopny kek Andrzej", "Waldemar", "Andrzej", "Przepiorka", "LELEandrEEE", "Konopny Andrzej"),
      Arrays.asList("Andrzej", "LELEandrEEE", "Konopny Andrzej", "Konopny kek Andrzej")
    ),

    new QueryUtilsTestCase(
      null,
      Arrays.asList("Konopny kek Andrzej", "Waldemar", "Andrzej", "Przepiorka", "LELEandrEEE", "Konopny Andrzej"),
      Arrays.asList("Konopny kek Andrzej", "Waldemar", "Andrzej", "Przepiorka", "LELEandrEEE", "Konopny Andrzej")
    ),

    new QueryUtilsTestCase(
      "tesco",
      Arrays.asList(null, "tesco", "market tesco market", "lidl"),
      Arrays.asList("tesco", "market tesco market")
    )
  );
  
  @Test
  public void test_ok() {
    testCases.forEach(testCase -> {

      Assert.assertEquals(
        testCase + " has failed",
        testCase.output,
        testCase.input.stream()
          .filter(inputElement -> QueryUtils.matches(inputElement, testCase.query))
          .sorted((left, right) -> QueryUtils.compareByQuery(left, right, testCase.query))
          .collect(Collectors.toList())
      );
      
    });
  }
  
}