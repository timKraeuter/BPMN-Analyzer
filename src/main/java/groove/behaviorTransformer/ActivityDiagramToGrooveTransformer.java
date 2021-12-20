package groove.behaviorTransformer;

import behavior.activity.ActivityDiagram;
import behavior.activity.expression.BinaryExpression;
import behavior.activity.expression.SetVariableExpression;
import behavior.activity.expression.bool.BooleanBinaryExpression;
import behavior.activity.expression.bool.BooleanUnaryExpression;
import behavior.activity.expression.bool.BooleanUnaryOperator;
import behavior.activity.expression.integer.IntegerCalculationExpression;
import behavior.activity.expression.integer.IntegerComparisonExpression;
import behavior.activity.expression.visitor.ExpressionVisitor;
import behavior.activity.nodes.*;
import behavior.activity.values.BooleanValue;
import behavior.activity.values.IntegerValue;
import behavior.activity.values.Value;
import behavior.activity.values.ValueVisitor;
import behavior.activity.variables.Variable;
import groove.graph.GrooveGraph;
import groove.graph.GrooveGraphBuilder;
import groove.graph.GrooveNode;
import groove.graph.rule.GrooveGraphRule;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

public class ActivityDiagramToGrooveTransformer implements GrooveTransformer<ActivityDiagram> {


    // Possible node labels.
    private static final String TYPE_INITIAL_NODE = TYPE + "InitialNode";
    private static final String TYPE_ACTIVITY_DIAGRAM = TYPE + "ActivityDiagram";
    private static final String TYPE_CONTROL_FLOW = TYPE + "ControlFlow";
    private static final String TYPE_OPAQUE_ACTION = TYPE + "OpaqueAction";
    private static final String TYPE_FORK_NODE = TYPE + "ForkNode";
    private static final String TYPE_JOIN_NODE = TYPE + "JoinNode";
    private static final String TYPE_DECISION_NODE = TYPE + "DecisionNode";
    private static final String TYPE_MERGE_NODE = TYPE + "MergeNode";
    private static final String TYPE_FINAL_NODE = TYPE + "FinalNode";

    private static final String TYPE_INTEGER_VALUE = TYPE + "IntegerValue";
    private static final String TYPE_SUM = TYPE + "Sum";
    private static final String TYPE_DIFFERENCE = TYPE + "Difference";
    private static final String TYPE_SMALLER = TYPE + "Smaller";
    private static final String TYPE_GREATER = TYPE + "Greater";
    private static final String TYPE_GREATER_EQUALS = TYPE + "GreaterEquals";
    private static final String TYPE_EQUALS = TYPE + "Equals";
    private static final String TYPE_SMALLER_EQUALS = TYPE + "SmallerEquals";

    private static final String TYPE_BOOLEAN_VALUE = TYPE + "BooleanValue";
    private static final String TYPE_NOT = TYPE + "Not";
    private static final String TYPE_AND = TYPE + "And";
    private static final String TYPE_OR = TYPE + "Or";

    private static final String TYPE_VARIABLE = TYPE + "Variable";
    private static final String TYPE_SET_VARIABLE_EXPRESSION = TYPE + "SetVariableExpression";

    // Edge labels
    private static final String SOURCE = "source";
    private static final String TARGET = "target";
    private static final String NEW_VALUE = "newValue";
    private static final String VALUE = "value";
    private static final String VAR = "var";
    private static final String NAME = "name";
    private static final String EXP = "exp";
    private static final String ASSIGNEE = "assignee";
    private static final String OPERAND_1 = "1";
    private static final String OPERAND_2 = "2";

    @Override
    public GrooveGraph generateStartGraph(ActivityDiagram activityDiagram, boolean addPrefix) {
        GrooveGraphBuilder builder = new GrooveGraphBuilder().setName(activityDiagram.getName());

        Map<ActivityNode, GrooveNode> createdNodesIndex = new LinkedHashMap<>();

        // Create initial node in groove
        InitialNode initialNode = activityDiagram.getInitialNode();
        GrooveNode initialNodeGroove = new GrooveNode(TYPE_INITIAL_NODE);
        builder.addNode(initialNodeGroove);
        createdNodesIndex.put(initialNode, initialNodeGroove);

        // Create activity diagram node in groove
        GrooveNode activityDiagramNode = new GrooveNode(TYPE_ACTIVITY_DIAGRAM);
        activityDiagramNode.addAttribute("running", false);
        activityDiagramNode.addAttribute("name", activityDiagram.getName());
        builder.addNode(activityDiagramNode);
        builder.addEdge("start", activityDiagramNode, initialNodeGroove);

        // Create variables in groove
        Map<String, GrooveNode> variableNameToNode = new HashMap<>();
        activityDiagram.localVariables().forEach(localVar -> this.createAndInitVariable(builder, localVar, variableNameToNode));
        activityDiagram.inputVariables().forEach(inputVariable -> {
            // TODO: This should be done according to given parameters of the input variables NOT the initial values.
            this.createAndInitVariable(builder, inputVariable, variableNameToNode);
        });

        // Create activity nodes in groove
        activityDiagram.getNodes().forEach(activityNode -> activityNode.accept(new ActivityNodeVisitor() {
            @Override
            public void handle(DecisionNode decisionNode) {
                GrooveNode decisionNodeGroove = new GrooveNode(TYPE_DECISION_NODE);
                builder.addNode(decisionNodeGroove);
                createdNodesIndex.put(decisionNode, decisionNodeGroove);
            }

            @Override
            public void handle(ForkNode forkNode) {
                GrooveNode forkNodeGroove = new GrooveNode(TYPE_FORK_NODE);
                builder.addNode(forkNodeGroove);
                createdNodesIndex.put(forkNode, forkNodeGroove);
            }

            @Override
            public void handle(InitialNode initialNode) {
                // Is ignored because it was already created earlier!
            }

            @Override
            public void handle(JoinNode joinNode) {
                GrooveNode joinNodeGroove = new GrooveNode(TYPE_JOIN_NODE);
                builder.addNode(joinNodeGroove);
                createdNodesIndex.put(joinNode, joinNodeGroove);
            }

            @Override
            public void handle(MergeNode mergeNode) {
                GrooveNode mergeNodeGroove = new GrooveNode(TYPE_MERGE_NODE);
                builder.addNode(mergeNodeGroove);
                createdNodesIndex.put(mergeNode, mergeNodeGroove);
            }

            @Override
            public void handle(OpaqueAction opaqueAction) {
                GrooveNode opaqueActionGroove = new GrooveNode(TYPE_OPAQUE_ACTION);
                opaqueActionGroove.addAttribute(NAME, opaqueAction.getName());
                builder.addNode(opaqueActionGroove);
                createdNodesIndex.put(opaqueAction, opaqueActionGroove);

                ActivityDiagramToGrooveTransformer.this.convertActionExpressions(opaqueAction, opaqueActionGroove, builder, variableNameToNode);
            }

            @Override
            public void handle(ActivityFinalNode activityFinalNode) {
                GrooveNode activityFinalNodeGroove = new GrooveNode(TYPE_FINAL_NODE);
                builder.addNode(activityFinalNodeGroove);
                createdNodesIndex.put(activityFinalNode, activityFinalNodeGroove);
            }
        }));
        // Create control flows in groove
        activityDiagram.getEdges().forEach(
                edge -> {
                    GrooveNode controlFlowNode = new GrooveNode(TYPE_CONTROL_FLOW);
                    builder.addNode(controlFlowNode);
                    builder.addEdge(SOURCE, controlFlowNode, createdNodesIndex.get(edge.getSource()));
                    builder.addEdge(TARGET, controlFlowNode, createdNodesIndex.get(edge.getTarget()));
                });

        return builder.build();
    }

    private void convertActionExpressions(
            OpaqueAction opaqueAction,
            GrooveNode opaqueActionGroove,
            GrooveGraphBuilder builder,
            Map<String, GrooveNode> variableNameToNode) {
        opaqueAction.expressions().forEach(expression -> expression.accept(new ExpressionVisitor() {
            @Override
            public <VALUE extends Value> void handle(SetVariableExpression<VALUE> setVariableExpression) {
                GrooveNode expressionNode = new GrooveNode(TYPE_SET_VARIABLE_EXPRESSION);
                builder.addEdge(EXP, opaqueActionGroove, expressionNode);

                GrooveNode newValueNode = ActivityDiagramToGrooveTransformer.this.createValueNode(
                        setVariableExpression.getValue());
                builder.addEdge(NEW_VALUE, expressionNode, newValueNode);

                String varName = setVariableExpression.getVariableToBeSet().getName();
                GrooveNode variableNode = ActivityDiagramToGrooveTransformer.this.getVarForName(varName, variableNameToNode);
                builder.addEdge(VAR, expressionNode, variableNode);
            }

            @Override
            public void handle(IntegerCalculationExpression integerCalculationExpression) {
                GrooveNode expNode = null;
                switch (integerCalculationExpression.getOperator()) {
                    case ADD:
                        expNode = new GrooveNode(TYPE_SUM);
                        break;
                    case SUBTRACT:
                        expNode = new GrooveNode(TYPE_DIFFERENCE);
                        break;
                }
                ActivityDiagramToGrooveTransformer.this.addExpressionToAction(
                        integerCalculationExpression,
                        expNode,
                        builder,
                        opaqueActionGroove,
                        variableNameToNode);
            }

            @Override
            public void handle(IntegerComparisonExpression integerComparisonExpression) {
                GrooveNode expNode = null;
                switch (integerComparisonExpression.getOperator()) {

                    case SMALLER:
                        expNode = new GrooveNode(TYPE_SMALLER);
                        break;
                    case SMALLER_EQUALS:
                        expNode = new GrooveNode(TYPE_SMALLER_EQUALS);
                        break;
                    case EQUALS:
                        expNode = new GrooveNode(TYPE_EQUALS);
                        break;
                    case GREATER_EQUALS:
                        expNode = new GrooveNode(TYPE_GREATER_EQUALS);
                        break;
                    case GREATER:
                        expNode = new GrooveNode(TYPE_GREATER);
                        break;
                }
                ActivityDiagramToGrooveTransformer.this.addExpressionToAction(
                        integerComparisonExpression,
                        expNode,
                        builder,
                        opaqueActionGroove,
                        variableNameToNode);

            }

            @Override
            public void handle(BooleanBinaryExpression booleanBinaryExpression) {
                GrooveNode expNode = null;
                switch (booleanBinaryExpression.getOperator()) {
                    case AND:
                        expNode = new GrooveNode(TYPE_AND);
                        break;
                    case OR:
                        expNode = new GrooveNode(TYPE_OR);
                        break;
                }
                ActivityDiagramToGrooveTransformer.this.addExpressionToAction(
                        booleanBinaryExpression,
                        expNode,
                        builder,
                        opaqueActionGroove,
                        variableNameToNode);
            }

            @Override
            public void handle(BooleanUnaryExpression booleanUnaryExpression) {
                assert BooleanUnaryOperator.values().length == 1;
                GrooveNode notExpNode = new GrooveNode(TYPE_NOT);
                builder.addEdge(EXP, opaqueActionGroove, notExpNode);

                String assigneeVarName = booleanUnaryExpression.getAssignee().getName();
                GrooveNode assigneeVar = ActivityDiagramToGrooveTransformer.this.getVarForName(
                        assigneeVarName,
                        variableNameToNode);
                builder.addEdge(ASSIGNEE, notExpNode, assigneeVar);

                String operand1VarName = booleanUnaryExpression.getOperand().getName();
                GrooveNode operand1Var = ActivityDiagramToGrooveTransformer.this.getVarForName(
                        operand1VarName,
                        variableNameToNode);
                builder.addEdge(OPERAND_1, notExpNode, operand1Var);
            }
        }));
    }

    private GrooveNode getVarForName(String varName, Map<String, GrooveNode> variableNameToNode) {
        GrooveNode variableNode = variableNameToNode.get(varName);
        if (variableNode == null) {
            throw new RuntimeException(String.format("Variable with name %s not initialized correctly!", varName));
        }
        return variableNode;
    }

    private void addExpressionToAction(BinaryExpression binaryExpression,
                                       GrooveNode expNode,
                                       GrooveGraphBuilder builder,
                                       GrooveNode opaqueActionGroove,
                                       Map<String, GrooveNode> variableNameToNode) {
        builder.addEdge(EXP, opaqueActionGroove, expNode);

        GrooveNode assigneeVar = this.getVarForName(binaryExpression.getNameOfAssignee(), variableNameToNode);
        builder.addEdge(ASSIGNEE, expNode, assigneeVar);

        GrooveNode operand1Var = this.getVarForName(binaryExpression.getNameOfOperand1(), variableNameToNode);
        builder.addEdge(OPERAND_1, expNode, operand1Var);
        GrooveNode operand2Var = this.getVarForName(binaryExpression.getNameOfOperand2(), variableNameToNode);
        builder.addEdge(OPERAND_2, expNode, operand2Var);
    }

    private void createAndInitVariable(
            GrooveGraphBuilder builder,
            Variable<? extends Value> var,
            Map<String, GrooveNode> variableNameToNode) {
        GrooveNode variableNode = new GrooveNode(TYPE_VARIABLE);
        variableNameToNode.put(var.getName(), variableNode);
        variableNode.addAttribute(NAME, var.getName());

        GrooveNode initialValueNode = ActivityDiagramToGrooveTransformer.this.createValueNode(
                var.getInitialValue());
        builder.addEdge(VALUE, variableNode, initialValueNode);
    }

    private <VALUE extends Value> GrooveNode createValueNode(VALUE value) {
        return value.accept(new ValueVisitor<>() {
            @Override
            public GrooveNode handle(IntegerValue integerValue) {
                GrooveNode integerValueNode = new GrooveNode(TYPE_INTEGER_VALUE);
                integerValueNode.addAttribute(VALUE, integerValue.getValue());
                return integerValueNode;
            }

            @Override
            public GrooveNode handle(BooleanValue booleanValue) {
                GrooveNode booleanValueNode = new GrooveNode(TYPE_BOOLEAN_VALUE);
                booleanValueNode.addAttribute(VALUE, booleanValue.getValue());
                return booleanValueNode;
            }
        });
    }

    @Override
    public Stream<GrooveGraphRule> generateRules(ActivityDiagram activityDiagram, boolean addPrefix) {
        // TODO: implement!
        return Stream.of();
    }
}
