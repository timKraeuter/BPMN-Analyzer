package no.tk.groove.behaviortransformer.bpmn;

import no.tk.behavior.bpmn.BPMNCollaboration;
import no.tk.reader.BPMNFileReaderTestHelper;
import com.google.common.collect.Sets;
import no.tk.groove.behaviortransformer.BehaviorToGrooveTransformerTestHelper;

import java.util.Set;

public abstract class BPMNToGrooveTestBase extends BehaviorToGrooveTransformerTestHelper
    implements BPMNFileReaderTestHelper {
  public static final String BPMN_BPMN_MODELS_SEMANTICS_TEST_FOLDER =
      "bpmn/bpmnModelsSemanticsTest/";
  public static final Set<String> fixedRules =
      Sets.newHashSet(
          BPMNToGrooveTransformer.ALL_TERMINATED_FILE_NAME,
          BPMNToGrooveTransformer.TERMINATE_RULE_FILE_NAME,
          BPMNToGrooveTransformer.UNSAFE_FILE_NAME,
          BPMNToGrooveTransformer.TYPE_GRAPH_FILE_NAME);

  @Override
  protected void setUpFurther() {
    // Default is to ignore the type graph, the terminate rule and the interrupt rule.
    this.setFileNameFilter(fixedRules::contains);
  }

  @Override
  public String getTestResourcePathSubFolderName() {
    return "bpmn/groove/";
  }

  @Override
  public String getOutputPathSubFolderName() {
    return "bpmn/";
  }

  protected void testGrooveGenerationForBPMNResourceFile(String resourceFileName) throws Exception {
    BPMNCollaboration collaboration = readModelFromResourceFolder(resourceFileName);
    this.checkGrooveGeneration(collaboration);
  }

  protected void testGrooveGenerationWithIDsForBPMNResourceFile(String resourceFileName)
      throws Exception {
    BPMNCollaboration collaboration = readModelFromResourceFolder(resourceFileName);
    this.checkGrooveGenerationWithIDs(collaboration);
  }
}
