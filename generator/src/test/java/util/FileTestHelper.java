package util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;

public class FileTestHelper {
  public static void testFileEquals(File expected, File actual) {
    try {
      final String actualString =
          FileUtils.readFileToString(actual, StandardCharsets.UTF_8)
              .replaceAll("\r?\n", "\r\n"); // force consistent line separators
      final String expectedString =
          FileUtils.readFileToString(expected, StandardCharsets.UTF_8)
              .replaceAll("\r?\n", "\r\n"); // force consistent line separators
      assertThat(
          String.format(
              "The file %s is not equal to the file %s!", expected.getName(), actual.getName()),
          actualString,
          is(expectedString));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /** Test if the files in the dirs are equal. Shallow, i.e., no recursion! */
  @SuppressWarnings("ConstantConditions")
  public static void testDirEquals(
      File expected, File actual, Function<String, Boolean> fileNameFilter) {
    Assertions.assertNotNull(expected);
    Assertions.assertNotNull(actual);

    Map<String, File> expectedFileNamesToFile =
        Arrays.stream(expected.listFiles())
            .collect(Collectors.toMap(File::getName, Function.identity()));

    Map<String, File> actualFileNamesToFile =
        Arrays.stream(actual.listFiles())
            .filter(file -> !fileNameFilter.apply(file.getName()))
            .collect(Collectors.toMap(File::getName, Function.identity()));
    // First check if the folders have files with the same name.
    assertThat(actualFileNamesToFile.keySet(), is(expectedFileNamesToFile.keySet()));
    // Check each individual file.
    expectedFileNamesToFile.forEach(
        (expectedFileName, expectedFile) -> {
          if (!fileNameFilter.apply(expectedFileName)) {
            File actualFile = actualFileNamesToFile.get(expectedFileName);
            testFileEquals(expectedFile, actualFile);
          }
        });
  }
}
