package groove.graph.rule;

import api.GraphRuleGenerator;
import api.Node;
import behavior.Behavior;
import groove.graph.GrooveEdge;
import groove.graph.GrooveNode;

import java.util.*;
import java.util.stream.Stream;

public class GrooveRuleBuilder implements GraphRuleGenerator {
    private final List<GrooveGraphRule> rules = new ArrayList<>();
    private GrooveGraphRule currentRule = null;
    private final String prefix;

    public GrooveRuleBuilder() {
        this.prefix = "";
    }

    public GrooveRuleBuilder(Behavior behavior, boolean addPrefix) {
        this.prefix = getPotentialPrefix(behavior, addPrefix);
    }

    public static String getPotentialPrefix(Behavior behavior, boolean addPrefix) {
        final String prefix;
        if (addPrefix) {
            prefix = behavior.getName() + "_";
        } else {
            prefix = "";
        }
        return prefix;
    }

    public static Stream<GrooveGraphRule> createSynchedRules(Map<String, Set<GrooveGraphRule>> nameToToBeSynchedRules) {
        GrooveRuleBuilder ruleGenerator = new GrooveRuleBuilder();
        nameToToBeSynchedRules.forEach((synchedRuleName, synchedRules) -> {
            ruleGenerator.startRule(synchedRuleName);

            synchedRules.stream()
                        .sorted(Comparator.comparing(GrooveGraphRule::getRuleName))
                        .forEach(grooveGraphRule -> {
                            Map<String, GrooveNode> oldIdToNewNode = new HashMap<>();
                            // Nodes
                            grooveGraphRule.getNodesToBeAdded().forEach(addNode -> {
                                GrooveNode createdAddNode = ruleGenerator.addNode(addNode.getName());
                                oldIdToNewNode.put(addNode.getId(), createdAddNode);
                            });
                            grooveGraphRule.getNodesToBeDeleted().forEach(delNode -> {
                                GrooveNode createdDelNode = ruleGenerator.deleteNode(delNode.getName());
                                oldIdToNewNode.put(delNode.getId(), createdDelNode);
                            });
                            grooveGraphRule.getContextNodes().forEach(contextNode -> {
                                GrooveNode createdContextNode = ruleGenerator.contextNode(contextNode.getName());
                                oldIdToNewNode.put(contextNode.getId(), createdContextNode);
                            });

                            // Edges
                            grooveGraphRule.getEdgesToBeAdded().forEach(addEdge -> ruleGenerator.addEdge(
                                    addEdge.getName(),
                                    oldIdToNewNode.get(addEdge.getSourceNode().getId()),
                                    oldIdToNewNode.get(addEdge.getTargetNode().getId())));
                            grooveGraphRule.getEdgesToBeDeleted().forEach(delEdge -> ruleGenerator.deleteEdge(
                                    delEdge.getName(),
                                    oldIdToNewNode.get(delEdge.getSourceNode().getId()),
                                    oldIdToNewNode.get(delEdge.getTargetNode().getId())));
                        });

            ruleGenerator.buildRule();
        });
        return ruleGenerator.getRules();
    }

    @Override
    public void startRule(String ruleName) {
        this.currentRule = new GrooveGraphRule(this.addPrefix(ruleName));
    }

    @Override
    public GrooveNode contextNode(String nodeName) {
        String prefixedNodeName = this.addPrefix(nodeName);

        assert this.currentRule != null;
        GrooveNode contextNode = new GrooveNode(prefixedNodeName);
        this.currentRule.addContextNode(contextNode);
        return contextNode;
    }

    private String addPrefix(String name) {
        return this.prefix + name;
    }

    @Override
    public GrooveNode addNode(String nodeName) {
        assert this.currentRule != null;

        String prefixedNodeName = this.addPrefix(nodeName);

        GrooveNode newNode = new GrooveNode(prefixedNodeName);
        this.currentRule.addNewNode(newNode);
        return newNode;
    }

    @Override
    public void addEdge(String edgeName, Node source, Node target) {

        assert this.currentRule != null;
        Map<String, GrooveNode> contextAndAddedNodes = this.currentRule.getContextAndAddedNodes();
        GrooveNode sourceNode = contextAndAddedNodes.get(source.getId());
        GrooveNode targetNode = contextAndAddedNodes.get(target.getId());

        this.checkNodeContainment(source, target, sourceNode, targetNode);

        String prefixedEdgeName = this.addPrefix(edgeName);
        this.currentRule.addNewEdge(new GrooveEdge(prefixedEdgeName, sourceNode, targetNode));
    }

    @Override
    public GrooveNode deleteNode(String nodeName) {
        assert this.currentRule != null;

        String prefixedNodeName = this.addPrefix(nodeName);
        GrooveNode deleteNode = new GrooveNode(prefixedNodeName);
        this.currentRule.addDelNode(deleteNode);
        return deleteNode;
    }

    @Override
    public void deleteEdge(String edgeName, Node source, Node target) {
        assert this.currentRule != null;
        Map<String, GrooveNode> contextAndAddedNodes = this.currentRule.getAllNodes();

        GrooveNode sourceNode = contextAndAddedNodes.get(source.getId());
        GrooveNode targetNode = contextAndAddedNodes.get(target.getId());

        this.checkNodeContainment(source, target, sourceNode, targetNode);

        String prefixedEdgeName = this.addPrefix(edgeName);
        this.currentRule.addDelEdge(new GrooveEdge(prefixedEdgeName, sourceNode, targetNode));
    }

    @Override
    public void contextEdge(String name, GrooveNode source, GrooveNode target) {
        assert this.currentRule != null;
        Map<String, GrooveNode> nodes = this.currentRule.getAllNodes();

        GrooveNode sourceNode = nodes.get(source.getId());
        GrooveNode targetNode = nodes.get(target.getId());

        this.checkNodeContainment(source, target, sourceNode, targetNode);

        String prefixedEdgeName = this.addPrefix(name);
        this.currentRule.addContextEdge(new GrooveEdge(prefixedEdgeName, sourceNode, targetNode));
    }

    private void checkNodeContainment(Node source, Node target, GrooveNode sourceNode, GrooveNode targetNode) {
        if (sourceNode == null) {
            throw new RuntimeException(String.format("Source node %s not contained in the rule!", source));
        }
        if (targetNode == null) {
            throw new RuntimeException(String.format("Target node %s not contained in the rule!", target));
        }
    }

    @Override
    public GrooveGraphRule buildRule() {
        GrooveGraphRule newRule = this.currentRule;
        this.rules.add(newRule);
        this.currentRule = null;
        return newRule;
    }

    public Stream<GrooveGraphRule> getRules() {
        return this.rules.stream();
    }
}
