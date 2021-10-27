package groove;

import api.GraphRule;

import java.util.*;

public class GrooveGraphRule implements GraphRule {
    private final String ruleName;

    // We are using list since one Node with exactly the same properties could be added twice by a rule.
    private final Map<String, GrooveNode> nodesToBeAdded;
    private final Map<String, GrooveNode> contextNodes;
    private final Map<String, GrooveNode> nodesToBeDeleted;

    private final Map<String, GrooveEdge> edgesToBeAdded;
    private final Map<String, GrooveEdge> contextEdges;
    private final Map<String, GrooveEdge> edgesToBeDeleted;

    public GrooveGraphRule(String ruleName) {
        this.ruleName = ruleName;
        this.nodesToBeAdded = new LinkedHashMap<>();
        this.contextNodes = new LinkedHashMap<>();
        this.nodesToBeDeleted = new LinkedHashMap<>();
        this.edgesToBeAdded = new LinkedHashMap<>();
        this.contextEdges = new LinkedHashMap<>();
        this.edgesToBeDeleted = new LinkedHashMap<>();
    }

    public void addNewNode(GrooveNode node) {
        if (this.contextNodes.get(node.getId()) != null) {
            throw new RuntimeException(String.format("Node %s already contained as a context node!", node));
        }
        this.nodesToBeAdded.put(node.getId(), node);
    }

    public void addNewEdge(GrooveEdge grooveEdge) {
        // It should be checked elsewhere that the source and target node are in the context of the rule or added by the rule!
        this.edgesToBeAdded.put(grooveEdge.getId(), grooveEdge);
    }

    public String getRuleName() {
        return this.ruleName;
    }

    public List<GrooveNode> getNodesToBeAdded() {
        return new ArrayList<>(this.nodesToBeAdded.values());
    }

    public List<GrooveEdge> getEdgesToBeAdded() {
        return new ArrayList<>(this.edgesToBeAdded.values());
    }

    public Map<String, GrooveNode> getContextAndAddedNodes() {
        Map<String, GrooveNode> addedAndContextNodes = new HashMap<>(this.nodesToBeAdded);
        addedAndContextNodes.putAll(this.contextNodes); // the maps are distinct, see add methods.
        return addedAndContextNodes;
    }
}
