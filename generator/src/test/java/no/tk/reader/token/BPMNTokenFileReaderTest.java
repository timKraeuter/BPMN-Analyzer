package no.tk.reader.token;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import no.tk.behavior.bpmn.reader.token.model.CollaborationSnapshot;
import no.tk.behavior.bpmn.reader.token.model.ProcessSnapshot;
import no.tk.behavior.bpmn.reader.token.model.Token;
import org.junit.jupiter.api.Test;

class BPMNTokenFileReaderTest implements BPMNTokenFileReaderTestHelper {

  @Test
  void readSnapshotWithTokens() throws IOException {
    CollaborationSnapshot collaborationSnapshot =
        readBPMNSnapshotFromResource("snapshotWithTokens.xml");

    assertNotNull(collaborationSnapshot);
    assertThat(collaborationSnapshot.getName(), is("snapshotWithTokens"));

    // Check for tokens and process snapshots
    assertThat(collaborationSnapshot.getProcessSnapshots().size(), is(1));
    ProcessSnapshot processSnapshot = collaborationSnapshot.getProcessSnapshots().iterator().next();
    assertThat(processSnapshot.getSnapshotID(), is("ProcessSnapshot_1hrwrvp"));
    assertThat(processSnapshot.getSnapshotNameIfExists(), is("AtomicPropositionTest"));
    assertThat(processSnapshot.getTokens().size(), is(3));
    assertTrue(processSnapshot.isShouldExist());
    assertThat(
        getElementIDsForTokens(processSnapshot),
        is(
            Lists.newArrayList(
                "StartEvent_1 -> Activity_1iduxj0 (Flow_1df3b4l)",
                "Activity_1iduxj0 (Activity_1iduxj0)",
                "Activity_1iduxj0 -> Event_0v0iz0c (Flow_1agdsil)")));
    assertThat(
        getShouldExistListForTokens(processSnapshot), is(Lists.newArrayList(true, true, false)));
  }

  @Test
  void readMultipleSnapshotWithTokens() throws IOException {
    CollaborationSnapshot collaborationSnapshot =
        readBPMNSnapshotFromResource("multipleSnapshotsWithTokens.xml");

    assertNotNull(collaborationSnapshot);
    assertThat(collaborationSnapshot.getName(), is("multipleSnapshotsWithTokens"));

    // Check for tokens and process snapshots
    assertThat(collaborationSnapshot.getProcessSnapshots().size(), is(3));

    // Check snapshot 1
    ProcessSnapshot snapshot1 = getSnapshotWithID(collaborationSnapshot, "ProcessSnapshot_1hrwrvp");
    assertThat(snapshot1.getSnapshotID(), is("ProcessSnapshot_1hrwrvp"));
    assertThat(snapshot1.getSnapshotNameIfExists(), is("AtomicPropositionTest"));
    assertThat(snapshot1.getTokens().size(), is(2));
    assertTrue(snapshot1.isShouldExist());
    assertThat(
        getElementIDsForTokens(snapshot1),
        is(
            Lists.newArrayList(
                "StartEvent_1 -> Activity_1iduxj0 (Flow_1df3b4l)",
                "Activity_1iduxj0 -> Event_0v0iz0c (Flow_1agdsil)")));
    assertThat(getShouldExistListForTokens(snapshot1), is(Lists.newArrayList(true, false)));

    // Check snapshot 2
    ProcessSnapshot snapshot2 = getSnapshotWithID(collaborationSnapshot, "ProcessSnapshot_01ch00c");
    assertThat(snapshot2.getSnapshotID(), is("ProcessSnapshot_01ch00c"));
    assertThat(snapshot2.getSnapshotNameIfExists(), is("AtomicPropositionTest"));
    assertThat(snapshot2.getTokens().size(), is(1));
    assertTrue(snapshot2.isShouldExist());
    assertThat(
        getElementIDsForTokens(snapshot2),
        is(Lists.newArrayList("Activity_1iduxj0 (Activity_1iduxj0)")));
    assertThat(getShouldExistListForTokens(snapshot2), is(Lists.newArrayList(true)));

    // Check snapshot 3
    ProcessSnapshot snapshot3 = getSnapshotWithID(collaborationSnapshot, "ProcessSnapshot_0jd6wrd");
    assertThat(snapshot3.getSnapshotID(), is("ProcessSnapshot_0jd6wrd"));
    assertThat(snapshot3.getSnapshotNameIfExists(), is("AtomicPropositionTest"));
    assertThat(snapshot3.getTokens().size(), is(0));
    assertFalse(snapshot3.isShouldExist());
  }

  @Test
  void readMultipleSnapshotWithTokensWithoutParticipants() throws IOException {
    CollaborationSnapshot collaborationSnapshot =
        readBPMNSnapshotFromResource("snapshotWithTokensWithoutParticipants.xml");

    assertNotNull(collaborationSnapshot);
    assertThat(collaborationSnapshot.getName(), is("snapshotWithTokensWithoutParticipants"));

    // Check for tokens and process snapshots
    assertThat(collaborationSnapshot.getProcessSnapshots().size(), is(3));

    // Check snapshot 1
    ProcessSnapshot snapshot1 = getSnapshotWithID(collaborationSnapshot, "ProcessSnapshot_0guz4p2");
    assertThat(snapshot1.getSnapshotID(), is("ProcessSnapshot_0guz4p2"));
    assertNull(snapshot1.getSnapshotNameIfExists());
    assertThat(snapshot1.getTokens().size(), is(2));
    assertTrue(snapshot1.isShouldExist());
    assertThat(
        getElementIDsForTokens(snapshot1),
        is(
            Lists.newArrayList(
                "Activity_1j00qsl (Activity_1j00qsl)",
                "StartEvent_1 -> Activity_1j00qsl (Flow_09mltb5)")));
    assertThat(getShouldExistListForTokens(snapshot1), is(Lists.newArrayList(false, true)));

    // Check snapshot 2
    ProcessSnapshot snapshot2 = getSnapshotWithID(collaborationSnapshot, "ProcessSnapshot_0soxahe");
    assertThat(snapshot2.getSnapshotID(), is("ProcessSnapshot_0soxahe"));
    assertNull(snapshot2.getSnapshotNameIfExists());
    assertThat(snapshot2.getTokens().size(), is(1));
    assertTrue(snapshot2.isShouldExist());
    assertThat(
        getElementIDsForTokens(snapshot2),
        is(Lists.newArrayList("Activity_1j00qsl -> Event_00vrokd (Flow_0wrsps5)")));
    assertThat(getShouldExistListForTokens(snapshot2), is(Lists.newArrayList(true)));

    // Check snapshot 3
    ProcessSnapshot snapshot3 = getSnapshotWithID(collaborationSnapshot, "ProcessSnapshot_1ejf247");
    assertThat(snapshot3.getSnapshotID(), is("ProcessSnapshot_1ejf247"));
    assertNull(snapshot3.getSnapshotNameIfExists());
    assertThat(snapshot3.getTokens().size(), is(0));
    assertFalse(snapshot3.isShouldExist());
  }

  @Test
  void readWhitespacesInNames() throws IOException {
    CollaborationSnapshot collaborationSnapshot = readBPMNSnapshotFromResource("orderHandling.xml");

    assertNotNull(collaborationSnapshot);
    assertThat(collaborationSnapshot.getName(), is("orderHandling"));

    // Check for tokens and process snapshots
    assertThat(collaborationSnapshot.getProcessSnapshots().size(), is(1));

    // Check snapshot 1
    ProcessSnapshot snapshot1 = getSnapshotWithID(collaborationSnapshot, "ProcessSnapshot_0lx4wgn");
    assertThat(snapshot1.getSnapshotID(), is("ProcessSnapshot_0lx4wgn"));
    assertThat(snapshot1.getSnapshotNameIfExists(), is("Order handling"));
    assertThat(snapshot1.getTokens().size(), is(2));
    assertTrue(snapshot1.isShouldExist());
    assertThat(
        getElementIDsForTokens(snapshot1),
        is(
            Lists.newArrayList(
                "Retrieve payment (Activity_1jgyh05)", "Ship goods (Activity_0lgvp3u)")));
    assertThat(getShouldExistListForTokens(snapshot1), is(Lists.newArrayList(false, false)));
  }

  private List<Boolean> getShouldExistListForTokens(ProcessSnapshot snapshot2) {
    return snapshot2.getTokens().stream().map(Token::isShouldExist).toList();
  }

  private List<String> getElementIDsForTokens(ProcessSnapshot snapshot1) {
    return snapshot1.getTokens().stream().map(Token::getElementID).toList();
  }

  private ProcessSnapshot getSnapshotWithID(CollaborationSnapshot bpmnSnapshot, String id) {
    return bpmnSnapshot.getProcessSnapshots().stream()
        .filter(processSnapshot -> processSnapshot.getSnapshotID().equals(id))
        .findFirst()
        .orElseThrow();
  }
}
