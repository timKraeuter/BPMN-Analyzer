package no.tk.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;

public class FileTestHelper {

  public static void testFileEquals(Path expected, Path actual) {
    try {
      final String actualString =
          Files.readString(actual).replaceAll("\r?\n", "\r\n"); // force consistent line separators
      final String expectedString =
          Files.readString(expected)
              .replaceAll("\r?\n", "\r\n"); // force consistent line separators
      assertThat(
          String.format(
              "The file %s is not equal to the file %s!",
              expected.getFileName(), actual.getFileName()),
          actualString,
          is(expectedString));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /** Test if the files in the dirs are equal. Shallow, i.e., no recursion! */
  @SuppressWarnings("ConstantConditions")
  public static void testDirEquals(
      Path expected, Path actual, Function<String, Boolean> fileNameFilter) throws IOException {
    Assertions.assertNotNull(expected);
    Assertions.assertNotNull(actual);

    Map<String, Path> expectedFileNamesToFile;
    try (Stream<Path> expectedFiles = Files.list(expected);
        Stream<Path> actualFiles = Files.list(actual)) {
      expectedFileNamesToFile =
          expectedFiles.collect(
              Collectors.toMap(o -> o.getFileName().toString(), Function.identity()));

      Map<String, Path> actualFileNamesToFile =
          actualFiles
              .filter(file -> !fileNameFilter.apply(file.getFileName().toString()))
              .collect(Collectors.toMap(o -> o.getFileName().toString(), Function.identity()));
      // First check if the folders have files with the same name.
      assertThat(actualFileNamesToFile.keySet(), is(expectedFileNamesToFile.keySet()));
      // Check each individual file.
      expectedFileNamesToFile.forEach(
          (expectedFileName, expectedFile) -> {
            if (!fileNameFilter.apply(expectedFileName)) {
              Path actualFile = actualFileNamesToFile.get(expectedFileName);
              testFileEquals(expectedFile, actualFile);
            }
          });
    }
  }

  public static Path getResource(String resource) {
    String resourcePath = "/" + resource;
    URL resourceURL = FileTestHelper.class.getResource(resourcePath);
    if (resourceURL == null) {
      throw new RuntimeException(
          String.format("Resource with the path \"%s\" could not be found!", resourcePath));
    }
    try {
      return Paths.get(resourceURL.toURI());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
}
