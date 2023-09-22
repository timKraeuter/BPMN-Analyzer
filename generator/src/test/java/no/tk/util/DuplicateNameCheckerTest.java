package no.tk.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.Set;
import no.tk.behavior.bpmn.BPMNCollaboration;
import no.tk.reader.BPMNFileReaderTestHelper;
import org.junit.jupiter.api.Test;

class DuplicateNameCheckerTest implements BPMNFileReaderTestHelper {

  @Test
  void checkForDuplicateNames() throws IOException {
    BPMNCollaboration collaboration = readModelFromResourceFolder("duplicateNames.bpmn");
    System.out.println(collaboration);
    Set<String> warnings = DuplicateNameChecker.checkForDuplicateNames(collaboration);
    assertThat(
        warnings,
        is(
            Sets.newHashSet(
                "The name \"a\" is shared between multiple bpmn elements. Please use unique or empty names.")));
  }

  @Test
  void checkForDuplicateNamesSubprocess() throws IOException {
    BPMNCollaboration collaboration = readModelFromResourceFolder("duplicateNameSubprocess.bpmn");
    System.out.println(collaboration);
    Set<String> warnings = DuplicateNameChecker.checkForDuplicateNames(collaboration);
    assertThat(
        warnings,
        is(
            Sets.newHashSet(
                "The name \"a\" is shared between multiple bpmn elements. Please use unique or empty names.",
                "The name \"b\" is shared between multiple bpmn elements. Please use unique or empty names.",
                "The name \"c\" is shared between multiple bpmn elements. Please use unique or empty names.",
                "The name \"d\" is shared between multiple bpmn elements. Please use unique or empty names.")));
  }
}
