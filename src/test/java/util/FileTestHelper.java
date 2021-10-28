package util;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class FileTestHelper {
    public static void testFileEquals(File expected, File actual) {
        try {
            assertThat(
                    FileUtils.readFileToString(actual, StandardCharsets.UTF_8),
                    is(FileUtils.readFileToString(expected, StandardCharsets.UTF_8)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Test if the files in the dirs are equal. Shallow, i.e., no recursion!
     */
    @SuppressWarnings("ConstantConditions")
    public static void testDirEquals(File expected, File actual, Function<String, Boolean> fileNameFilter) {
        Assertions.assertNotNull(expected);
        Assertions.assertNotNull(actual);

        Map<String, File> expectedFileNamesToFile = Arrays.stream(expected.listFiles())
                                                          .collect(Collectors.toMap(File::getName, Function.identity()));

        Map<String, File> actualFileNamesToFile = Arrays.stream(actual.listFiles())
                                                        .collect(Collectors.toMap(File::getName, Function.identity()));
        // First check if the folders have files with the same name.
        assertThat(expectedFileNamesToFile.keySet(), is(actualFileNamesToFile.keySet()));
        // Check each individual file.
        expectedFileNamesToFile.forEach((expectedFileName, expectedFile) -> {
            if (!fileNameFilter.apply(expectedFileName)) {
                File actualFile = actualFileNamesToFile.get(expectedFileName);
                testFileEquals(expectedFile, actualFile);
            }
        });
    }
}
