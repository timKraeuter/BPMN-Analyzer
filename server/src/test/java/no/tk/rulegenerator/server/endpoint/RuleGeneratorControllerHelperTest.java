package no.tk.rulegenerator.server.endpoint;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.apache.commons.io.file.PathUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RuleGeneratorControllerHelperTest {

  // --- sanitizeModelName tests (exercised through getGGOrStateSpaceDirName) ---

  @Test
  void testDirNameSanitizesForwardSlash() {
    // Given
    Instant fixedTime = Instant.now();

    // When
    String dirName =
        RuleGeneratorControllerHelper.getGGOrStateSpaceDirName("../../etc/passwd", fixedTime);

    // Then: The model name portion (after the second underscore) should not contain path separators
    String modelNamePart = extractModelNamePart(dirName);
    assertFalse(modelNamePart.contains("/"), "Forward slashes should be sanitized");
    assertFalse(modelNamePart.contains(".."), "Double dots should be sanitized");
  }

  @Test
  void testDirNameSanitizesBackslash() {
    // Given
    Instant fixedTime = Instant.now();

    // When
    String dirName =
        RuleGeneratorControllerHelper.getGGOrStateSpaceDirName(
            "..\\..\\windows\\system32", fixedTime);

    // Then
    String modelNamePart = extractModelNamePart(dirName);
    assertFalse(modelNamePart.contains("\\"), "Backslashes should be sanitized");
    assertFalse(modelNamePart.contains(".."), "Double dots should be sanitized");
  }

  @Test
  void testDirNameSanitizesNullByte() {
    // Given
    Instant fixedTime = Instant.now();

    // When
    String dirName =
        RuleGeneratorControllerHelper.getGGOrStateSpaceDirName("model\0.bpmn", fixedTime);

    // Then
    String modelNamePart = extractModelNamePart(dirName);
    assertFalse(modelNamePart.contains("\0"), "Null bytes should be sanitized");
  }

  @Test
  void testDirNamePreservesNormalModelName() {
    // Given
    Instant fixedTime = Instant.now();

    // When
    String dirName =
        RuleGeneratorControllerHelper.getGGOrStateSpaceDirName("my-bpmn-model", fixedTime);

    // Then
    String modelNamePart = extractModelNamePart(dirName);
    assertThat(modelNamePart, is("my-bpmn-model"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"../secret", "..\\secret", "foo/../../bar", "a\0b"})
  void testDirNameContainsNoPathTraversalCharacters(String maliciousName) {
    // When
    String dirName =
        RuleGeneratorControllerHelper.getGGOrStateSpaceDirName(maliciousName, Instant.now());

    // Then
    String modelNamePart = extractModelNamePart(dirName);
    assertFalse(modelNamePart.contains("/"), "Should not contain forward slash");
    assertFalse(modelNamePart.contains("\\"), "Should not contain backslash");
    assertFalse(modelNamePart.contains("\0"), "Should not contain null byte");
    assertFalse(modelNamePart.contains(".."), "Should not contain double dots");
  }

  // --- deleteGGsAndStateSpacesOlderThanOneHour robustness tests ---

  @Test
  void testDeleteSkipsFileWithNoUnderscore() throws IOException {
    // Given: A file in the temp dir that doesn't follow the timestamp_uuid_name pattern
    Path tempDir = Path.of(RuleGeneratorControllerHelper.GRAPH_GRAMMAR_TEMP_DIR);
    deleteIfExists(tempDir);
    Files.createDirectories(tempDir);
    Path unexpectedFile = Files.createDirectories(tempDir.resolve("no-underscore-here"));

    // When: Cleanup should not throw
    assertDoesNotThrow(RuleGeneratorControllerHelper::deleteGGsAndStateSpacesOlderThanOneHour);

    // Then: The unexpected file still exists (was skipped, not deleted or crashed)
    assertTrue(Files.exists(unexpectedFile));

    // Cleanup
    PathUtils.delete(unexpectedFile);
  }

  @Test
  void testDeleteSkipsFileWithUnparseableTimestamp() throws IOException {
    // Given: A file with an underscore but a non-timestamp prefix
    Path tempDir = Path.of(RuleGeneratorControllerHelper.GRAPH_GRAMMAR_TEMP_DIR);
    deleteIfExists(tempDir);
    Files.createDirectories(tempDir);
    Path badTimestamp = Files.createDirectories(tempDir.resolve("not-a-date_uuid_model"));

    // When: Cleanup should not throw
    assertDoesNotThrow(RuleGeneratorControllerHelper::deleteGGsAndStateSpacesOlderThanOneHour);

    // Then: The file still exists (was skipped)
    assertTrue(Files.exists(badTimestamp));

    // Cleanup
    PathUtils.delete(badTimestamp);
  }

  @Test
  void testDeleteStillRemovesOldFilesAlongsideUnexpectedOnes() throws IOException {
    // Given: Mix of a valid old GG, a valid young GG, and an unexpected file
    Path tempDir = Path.of(RuleGeneratorControllerHelper.GRAPH_GRAMMAR_TEMP_DIR);
    deleteIfExists(tempDir);
    Files.createDirectories(tempDir);

    String oldGG =
        RuleGeneratorControllerHelper.getGGOrStateSpaceDirName(
            "old", Instant.now().minus(1, ChronoUnit.HOURS));
    String youngGG = RuleGeneratorControllerHelper.getGGOrStateSpaceDirName("young");

    Path oldGGPath = Files.createDirectories(tempDir.resolve(oldGG));
    Path youngGGPath = Files.createDirectories(tempDir.resolve(youngGG));
    Path unexpectedFile = Files.createDirectories(tempDir.resolve("random-junk"));

    // When
    RuleGeneratorControllerHelper.deleteGGsAndStateSpacesOlderThanOneHour();

    // Then: Old is deleted, young and unexpected survive
    assertFalse(Files.exists(oldGGPath), "Old GG should be deleted");
    assertTrue(Files.exists(youngGGPath), "Young GG should survive");
    assertTrue(Files.exists(unexpectedFile), "Unexpected file should be skipped");

    // Cleanup
    PathUtils.delete(youngGGPath);
    PathUtils.delete(unexpectedFile);
  }

  /** Extracts the model name portion from a dir name like "timestamp_uuid_modelName". */
  private String extractModelNamePart(String dirName) {
    // Format: "dd.MM.yyyy-HH.mm.ss_uuid_modelName"
    int firstUnderscore = dirName.indexOf("_");
    int secondUnderscore = dirName.indexOf("_", firstUnderscore + 1);
    return dirName.substring(secondUnderscore + 1);
  }

  private void deleteIfExists(Path dir) throws IOException {
    if (Files.exists(dir)) {
      PathUtils.deleteDirectory(dir);
    }
  }
}
