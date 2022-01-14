package groove.behaviorTransformer.bpmn;

import groove.behaviorTransformer.BehaviorToGrooveTransformerTestHelper;

public abstract class BPMNToGrooveTestBase extends BehaviorToGrooveTransformerTestHelper {

    static final String TYPE_GRAPH_FILE_NAME = "type.gty";
    static final String TERMINATE_RULE_FILE_NAME = "Terminate.gpr";
    private static final String BPMN = "bpmn";

    @Override
    protected void setUpFurther() {
        // Default is to ignore the type graph and the terminate rule.
        this.setFileNameFilter(x -> x.equals(TYPE_GRAPH_FILE_NAME) || x.equals(TERMINATE_RULE_FILE_NAME));
    }

    @Override
    public String getOutputPathSubFolderName() {
        return BPMN;
    }
}
