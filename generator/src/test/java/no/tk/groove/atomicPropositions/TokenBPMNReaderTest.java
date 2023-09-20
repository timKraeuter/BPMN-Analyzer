package no.tk.groove.atomicPropositions;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.common.collect.Lists;
import java.nio.file.Path;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import no.tk.behavior.bpmn.reader.token.TokenBPMNFileReader;
import no.tk.behavior.bpmn.reader.token.model.BPMNProcessSnapshot;
import no.tk.behavior.bpmn.reader.token.model.ProcessSnapshot;
import no.tk.behavior.bpmn.reader.token.model.Token;
import java.io.IOException;
import no.tk.util.FileTestHelper;
import org.junit.jupiter.api.Test;

class TokenBPMNReaderTest {

  public static final String AP_TEST_PATH = "bpmn/atomicPropositions/";

  @Test
  void readSnapshotWithTokens() throws IOException {
    BPMNProcessSnapshot bpmnSnapshot =
        readBPMNSnapshotFromResource(AP_TEST_PATH + "snapshotWithTokens.xml", x -> x);

    assertNotNull(bpmnSnapshot);
    String name = "snapshotWithTokens";
    assertThat(bpmnSnapshot.getName(), is(name));

    // Check for tokens and process snapshots
    assertThat(bpmnSnapshot.getProcessSnapshots().size(), is(1));
    ProcessSnapshot singleProcessSnapshot = bpmnSnapshot.getProcessSnapshots().iterator().next();
    assertThat(singleProcessSnapshot.getSnapshotID(), is("ProcessSnapshot_1hrwrvp"));
    assertThat(singleProcessSnapshot.getProcessID(), is("Process_1"));
    assertThat(singleProcessSnapshot.getTokens().size(), is(3));
    assertThat(
        singleProcessSnapshot.getTokens().stream()
            .map(Token::getElementID)
            .collect(Collectors.toList()),
        is(Lists.newArrayList("Flow_1df3b4l", "Activity_1iduxj0", "Flow_1agdsil")));
    assertThat(
        singleProcessSnapshot.getTokens().stream()
            .map(Token::isShouldExist)
            .collect(Collectors.toList()),
        is(Lists.newArrayList(true, true, false)));
  }

  BPMNProcessSnapshot readBPMNSnapshotFromResource(
      String resourcePath, UnaryOperator<String> elementNameTransformer) throws IOException {
    @SuppressWarnings("ConstantConditions")
    Path model = FileTestHelper.getResource(resourcePath);
    TokenBPMNFileReader tokenBpmnFileReader = new TokenBPMNFileReader(elementNameTransformer);
    return tokenBpmnFileReader.readModelFromFilePath(model);
  }
}
