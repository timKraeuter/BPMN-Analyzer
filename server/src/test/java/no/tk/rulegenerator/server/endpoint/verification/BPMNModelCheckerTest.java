package no.tk.rulegenerator.server.endpoint.verification;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import no.tk.behavior.bpmn.BPMNCollaboration;
import no.tk.behavior.bpmn.reader.BPMNFileReader;
import no.tk.groove.behaviortransformer.BehaviorToGrooveTransformer;
import no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper;
import no.tk.rulegenerator.server.endpoint.dtos.BPMNPropertyCheckingResult;
import no.tk.rulegenerator.server.endpoint.dtos.BPMNSpecificProperty;
import no.tk.rulegenerator.server.endpoint.dtos.BPMNSpecificPropertyCheckingResponse;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BPMNModelCheckerTest {

  @ParameterizedTest
  @ValueSource(
      strings = {
        "no-proper-completion-1",
        "no-proper-completion-2",
        "no-proper-completion-3-unsafe"
      })
  void checkProperCompletionNotFulfilled(String fileName) throws Exception {
    // Given
    BPMNCollaboration bpmnCollaboration = readProperCompletionBPMNFile(fileName);
    Path ggDir = generateGG(bpmnCollaboration);

    // When
    BPMNModelChecker bpmnModelChecker = new BPMNModelChecker(ggDir, bpmnCollaboration);
    BPMNSpecificPropertyCheckingResponse result =
        bpmnModelChecker.checkBPMNProperties(Set.of(BPMNSpecificProperty.PROPER_COMPLETION));

    // Then
    assertThat(
        result.propertyCheckingResults(),
        is(
            List.of(
                new BPMNPropertyCheckingResult(
                    BPMNSpecificProperty.PROPER_COMPLETION, false, "EndEvent_1"))));
  }

  @ParameterizedTest
  @ValueSource(strings = {"proper-completion-1", "proper-completion-2"})
  void checkProperCompletionFulfilled(String fileName) throws Exception {
    // Given
    BPMNCollaboration bpmnCollaboration = readProperCompletionBPMNFile(fileName);
    Path ggDir = generateGG(bpmnCollaboration);

    // When
    BPMNModelChecker bpmnModelChecker = new BPMNModelChecker(ggDir, bpmnCollaboration);
    BPMNSpecificPropertyCheckingResponse result =
        bpmnModelChecker.checkBPMNProperties(Set.of(BPMNSpecificProperty.PROPER_COMPLETION));

    // Then
    assertThat(
        result.propertyCheckingResults(),
        is(
            List.of(
                new BPMNPropertyCheckingResult(BPMNSpecificProperty.PROPER_COMPLETION, true, ""))));
  }

  @Test
  void checkNoDeadActivitiesWithDeadActivities() throws Exception {
    // Given
    BPMNCollaboration bpmnCollaboration = readBPMNModel("ruleGeneratorController/dead.bpmn");
    Path ggDir = generateGG(bpmnCollaboration);

    // When
    BPMNModelChecker bpmnModelChecker = new BPMNModelChecker(ggDir, bpmnCollaboration);
    BPMNSpecificPropertyCheckingResponse result =
        bpmnModelChecker.checkBPMNProperties(Set.of(BPMNSpecificProperty.NO_DEAD_ACTIVITIES));

    // Then
    List<BPMNPropertyCheckingResult> results = result.propertyCheckingResults();
    assertThat(results.size(), is(1));
    BPMNPropertyCheckingResult noDeadResult = results.getFirst();
    assertThat(noDeadResult.name(), is(BPMNSpecificProperty.NO_DEAD_ACTIVITIES));
    assertFalse(noDeadResult.valid());
    // The additional info should contain the dead activity IDs
    assertFalse(noDeadResult.additionalInfo().isEmpty());
  }

  @Test
  void checkNoDeadActivitiesAllAlive() throws Exception {
    // Given
    BPMNCollaboration bpmnCollaboration =
        readBPMNModel("ruleGeneratorController/name-with-numbers.bpmn");
    Path ggDir = generateGG(bpmnCollaboration);

    // When
    BPMNModelChecker bpmnModelChecker = new BPMNModelChecker(ggDir, bpmnCollaboration);
    BPMNSpecificPropertyCheckingResponse result =
        bpmnModelChecker.checkBPMNProperties(Set.of(BPMNSpecificProperty.NO_DEAD_ACTIVITIES));

    // Then
    assertThat(
        result.propertyCheckingResults(),
        is(
            List.of(
                new BPMNPropertyCheckingResult(
                    BPMNSpecificProperty.NO_DEAD_ACTIVITIES, true, ""))));
  }

  @ParameterizedTest
  @ValueSource(strings = {"no-proper-completion-3-unsafe"})
  void checkSafenessNotFulfilled(String fileName) throws Exception {
    // Given
    BPMNCollaboration bpmnCollaboration = readProperCompletionBPMNFile(fileName);
    Path ggDir = generateGG(bpmnCollaboration);

    // When
    BPMNModelChecker bpmnModelChecker = new BPMNModelChecker(ggDir, bpmnCollaboration);
    BPMNSpecificPropertyCheckingResponse result =
        bpmnModelChecker.checkBPMNProperties(Set.of(BPMNSpecificProperty.SAFENESS));

    // Then
    List<BPMNPropertyCheckingResult> results = result.propertyCheckingResults();
    assertThat(results.size(), is(1));
    BPMNPropertyCheckingResult safenessResult = results.getFirst();
    assertThat(safenessResult.name(), is(BPMNSpecificProperty.SAFENESS));
    assertFalse(safenessResult.valid());
  }

  @ParameterizedTest
  @ValueSource(strings = {"proper-completion-1", "proper-completion-2"})
  void checkSafenessFulfilled(String fileName) throws Exception {
    // Given
    BPMNCollaboration bpmnCollaboration = readProperCompletionBPMNFile(fileName);
    Path ggDir = generateGG(bpmnCollaboration);

    // When
    BPMNModelChecker bpmnModelChecker = new BPMNModelChecker(ggDir, bpmnCollaboration);
    BPMNSpecificPropertyCheckingResponse result =
        bpmnModelChecker.checkBPMNProperties(Set.of(BPMNSpecificProperty.SAFENESS));

    // Then
    assertThat(
        result.propertyCheckingResults(),
        is(
            List.of(
                new BPMNPropertyCheckingResult(
                    BPMNSpecificProperty.SAFENESS, true, "CTL: AG(!Unsafe)"))));
  }

  private static Path generateGG(BPMNCollaboration bpmnCollaboration) {
    Path tempDir = Path.of(FileUtils.getTempDirectoryPath());
    BehaviorToGrooveTransformer transformer = new BehaviorToGrooveTransformer(false);
    return transformer.generateGrooveGrammarForBPMNProcessModel(bpmnCollaboration, tempDir);
  }

  private static BPMNCollaboration readProperCompletionBPMNFile(String fileName)
      throws IOException {
    return readBPMNModel("proper_completion/" + fileName + ".bpmn");
  }

  private static BPMNCollaboration readBPMNModel(String path) throws IOException {
    Path modelPath = getResource(path);
    BPMNFileReader bpmnFileReader =
        new BPMNFileReader(BPMNToGrooveTransformerHelper::transformToQualifiedGrooveNameIfNeeded);
    return bpmnFileReader.readModelFromFilePath(modelPath);
  }

  public static Path getResource(String resource) {
    String resourcePath = "/" + resource;
    URL resourceURL = BPMNModelCheckerTest.class.getResource(resourcePath);
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
