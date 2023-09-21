package no.tk.groove.behaviortransformer.bpmn.atomic.propositions;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import no.tk.behavior.bpmn.reader.token.model.BPMNProcessSnapshot;
import no.tk.groove.behaviortransformer.GrooveTransformer;
import no.tk.groove.graph.rule.GrooveGraphRule;
import no.tk.reader.token.BPMNTokenFileReaderTestHelper;
import org.junit.jupiter.api.Test;

class BPMNTokenAtomicPropositionGeneratorTest implements BPMNTokenFileReaderTestHelper {
  // TODO: Detailed node and edges check once ids are clear.
  @Test
  void generateAtomicPropositionOneSnapshot() throws IOException {
    BPMNProcessSnapshot bpmnProcessSnapshot =
        readBPMNSnapshotFromResource("snapshotWithTokens.xml");

    BPMNTokenAtomicPropositionGenerator generator = new BPMNTokenAtomicPropositionGenerator();

    GrooveGraphRule graphRule = generator.generateAtomicProposition(bpmnProcessSnapshot);

    assertThat(graphRule.getRuleName(), is("snapshotWithTokens"));
    assertThat(graphRule.getContextNodes().size(), is(8));
    assertTrue(hasNACToken(graphRule));
    assertThat(graphRule.getContextEdges().size(), is(7));
    assertThat(graphRule.getAllNodes().size(), is(graphRule.getContextNodes().size()));
    assertThat(graphRule.getEdgesToBeAdded().size(), is(0));
    assertThat(graphRule.getEdgesToBeDeleted().size(), is(0));
  }

  @Test
  void generateAtomicPropositionMultipleSnapshot() throws IOException {
    BPMNProcessSnapshot bpmnProcessSnapshot =
        readBPMNSnapshotFromResource("multipleSnapshotsWithTokens.xml");

    BPMNTokenAtomicPropositionGenerator generator = new BPMNTokenAtomicPropositionGenerator();

    GrooveGraphRule graphRule = generator.generateAtomicProposition(bpmnProcessSnapshot);

    assertThat(graphRule.getRuleName(), is("multipleSnapshotsWithTokens"));
    assertThat(graphRule.getContextNodes().size(), is(12));
    assertTrue(hasNACToken(graphRule));
    assertThat(graphRule.getContextEdges().size(), is(9));
    assertThat(graphRule.getAllNodes().size(), is(graphRule.getContextNodes().size()));
    assertThat(graphRule.getEdgesToBeAdded().size(), is(0));
    assertThat(graphRule.getEdgesToBeDeleted().size(), is(0));
  }

  private boolean hasNACToken(GrooveGraphRule graphRule) {
    return graphRule.getContextNodes().stream()
        .anyMatch(grooveNode -> grooveNode.getName().startsWith(GrooveTransformer.NOT));
  }
}
