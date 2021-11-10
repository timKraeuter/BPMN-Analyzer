package behavior.bpmn;

public class SequenceFlow {
    private final ControleFlowNode source;
    private final ControleFlowNode target;

    public SequenceFlow(ControleFlowNode source, ControleFlowNode target) {
        this.source = source;
        this.target = target;
    }
}
