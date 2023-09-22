package no.tk.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.Set;
import java.util.function.Predicate;
import no.tk.behavior.bpmn.BPMNCollaboration;
import no.tk.behavior.bpmn.FlowElement;
import no.tk.reader.BPMNFileReaderTestHelper;
import org.junit.jupiter.api.Test;

class DuplicateNameCheckerTest implements BPMNFileReaderTestHelper {

  @Test
  void checkForDuplicateNames() throws IOException {
    BPMNCollaboration collaboration = readModelFromResourceFolder("duplicateNames.bpmn");
    Set<String> warnings =
        DuplicateNameChecker.checkForDuplicateNames(collaboration, flowElement -> false);
    assertThat(warnings, is(Sets.newHashSet("a")));
  }

  @Test
  void checkForDuplicateNamesSubprocess() throws IOException {
    BPMNCollaboration collaboration = readModelFromResourceFolder("duplicateNameSubprocess.bpmn");
    Predicate<FlowElement> exclusion = DuplicateNameChecker.signalLinkErrorEscalationExclusion();
    Set<String> warnings = DuplicateNameChecker.checkForDuplicateNames(collaboration, exclusion);
    assertThat(warnings, is(Sets.newHashSet("a", "b", "c", "d")));
  }
}
