package no.tk.groove.behaviortransformer.bpmn.atomic.propositions;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import no.tk.behavior.bpmn.reader.token.model.BPMNProcessSnapshot;
import no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTestBase;
import no.tk.groove.graph.rule.GrooveGraphRule;
import no.tk.reader.token.BPMNTokenFileReaderTestHelper;
import no.tk.util.FileTestHelper;
import org.junit.jupiter.api.Test;

class BPMNTokenAtomicPropositionGeneratorTest extends BPMNToGrooveTestBase
    implements BPMNTokenFileReaderTestHelper {

  @Test
  void generateAtomicPropositionOneSnapshot() throws IOException {
    BPMNProcessSnapshot bpmnProcessSnapshot =
        readBPMNSnapshotFromResource("snapshotWithTokens.xml");

    BPMNTokenAtomicPropositionGenerator generator = new BPMNTokenAtomicPropositionGenerator(true);

    GrooveGraphRule graphRule = generator.generateAtomicProposition(bpmnProcessSnapshot);

    assertThat(graphRule.getRuleName(), is("snapshotWithTokens"));
    assertThat(graphRule.getContextNodes().size(), is(7));
    assertThat(graphRule.getNACNodes().size(), is(1));
    assertThat(graphRule.getContextEdges().size(), is(7));
    assertThat(graphRule.getAllNodes().size(), is(8));
    assertThat(graphRule.getEdgesToBeAdded().size(), is(0));
    assertThat(graphRule.getEdgesToBeDeleted().size(), is(0));
  }

  @Test
  void generateAtomicPropositionMultipleSnapshot() throws IOException {
    BPMNProcessSnapshot bpmnProcessSnapshot =
        readBPMNSnapshotFromResource("multipleSnapshotsWithTokens.xml");

    BPMNTokenAtomicPropositionGenerator generator = new BPMNTokenAtomicPropositionGenerator(true);

    GrooveGraphRule graphRule = generator.generateAtomicProposition(bpmnProcessSnapshot);

    assertThat(graphRule.getRuleName(), is("multipleSnapshotsWithTokens"));
    assertThat(graphRule.getContextNodes().size(), is(11));
    assertThat(graphRule.getNACNodes().size(), is(1));
    assertThat(graphRule.getContextEdges().size(), is(9));
    assertThat(graphRule.getAllNodes().size(), is(12));
    assertThat(graphRule.getEdgesToBeAdded().size(), is(0));
    assertThat(graphRule.getEdgesToBeDeleted().size(), is(0));
  }

  @Test
  void generateAtomicPropositionToFiles() throws Exception {
    BPMNProcessSnapshot bpmnProcessSnapshot =
        readBPMNSnapshotFromResource("snapshotWithTokens.xml");

    BPMNTokenAtomicPropositionGenerator generator = new BPMNTokenAtomicPropositionGenerator(true);

    Path targetFolder = Paths.get(this.getOutputPathIncludingSubFolder(), "snapshotWithTokens.gps");
    generator.generateAndWriteAtomicProposition(bpmnProcessSnapshot, targetFolder);

    String ruleName = "snapshotWithTokens";
    FileTestHelper.testDirEquals(getExpectedFolder(ruleName), targetFolder, x -> false);
  }

  @Test
  void generateAtomicPropositionToFilesWithWhitespaceNames() throws Exception {
    BPMNProcessSnapshot bpmnProcessSnapshot = readBPMNSnapshotFromResource("orderHandling.xml");

    BPMNTokenAtomicPropositionGenerator generator = new BPMNTokenAtomicPropositionGenerator(true);

    Path targetFolder = Paths.get(this.getOutputPathIncludingSubFolder(), "orderHandling.gps");
    generator.generateAndWriteAtomicProposition(bpmnProcessSnapshot, targetFolder);

    String ruleName = "orderHandling";
    FileTestHelper.testDirEquals(getExpectedFolder(ruleName), targetFolder, x -> false);
  }

  private Path getExpectedFolder(String ruleName) {
    String path = String.format("%s/%s.gps", getTestResourcePathSubFolderName(), ruleName);
    return FileTestHelper.getResource(path);
  }
}
