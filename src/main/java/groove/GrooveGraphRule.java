package groove;

import api.GraphRule;

import java.util.ArrayList;
import java.util.List;

public class GrooveGraphRule implements GraphRule {
    // We are using list since one Node with exactly the same properties could be added twice by a rule.
    private final List<GrooveNode> nodesToBeAdded;
    private final List<GrooveNode> nodesToBeDeleted;

    private final List<GrooveEdge> edgesToBeAdded;
    private final List<GrooveEdge> edgesToBeDeleted;

    public GrooveGraphRule() {
        this.nodesToBeAdded = new ArrayList<>();
        this.nodesToBeDeleted = new ArrayList<>();
        this.edgesToBeAdded = new ArrayList<>();
        this.edgesToBeDeleted = new ArrayList<>();
    }
}
