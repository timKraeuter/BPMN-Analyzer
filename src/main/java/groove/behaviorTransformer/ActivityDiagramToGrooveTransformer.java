package groove.behaviorTransformer;

import behavior.activity.ActivityDiagram;
import behavior.activity.edges.ControlFlow;
import behavior.activity.expression.BinaryExpression;
import behavior.activity.expression.Expression;
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
import groove.graph.rule.GrooveRuleBuilder;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class ActivityDiagramToGrooveTransformer implements GrooveTransformer<ActivityDiagram> {

    // Possible node labels.
    private static final String TYPE_INITIAL_NODE = TYPE + "InitialNode";
    private static final String TYPE_ACTIVITY_NODE = TYPE + "ActivityNode";
    private static final String TYPE_ACTIVITY_DIAGRAM = TYPE + "ActivityDiagram";
    private static final String TYPE_CONTROL_FLOW = TYPE + "ControlFlow";
    private static final String TYPE_OPAQUE_ACTION = TYPE + "OpaqueAction";
    private static final String TYPE_FORK_NODE = TYPE + "ForkNode";
    private static final String TYPE_JOIN_NODE = TYPE + "JoinNode";
    private static final String TYPE_DECISION_NODE = TYPE + "DecisionNode";
    private static final String TYPE_MERGE_NODE = TYPE + "MergeNode";
    private static final String TYPE_FINAL_NODE = TYPE + "FinalNode";
    private static final String TYPE_TOKEN = TYPE + "Token";
    private static final String TYPE_CONTROL_TOKEN = TYPE + "ControlToken";
    private static final String TYPE_FORKED_TOKEN = TYPE + "ForkedToken";

    private static final String TYPE_VALUE = TYPE + "Value";
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
    private static final String TOKENS = "tokens";
    private static final String BASE_TOKEN = "baseToken";
    private static final String GUARD = "guard";

    // Special groove labels
    private static final String STRING = "string:";
    private static final String BOOL = "bool:";
    private static final String FALSE = BOOL + "false";
    private static final String TRUE = BOOL + "true";
    private static final String BOOL_NOT = "bool:not";
    private static final String BOOL_AND = "bool:and";
    private static final String BOOL_OR = "bool:or";
    private static final String UNEQUALS = "!=";
    private static final String INT = "int:";
    private static final String ARG_0 = "arg:0";
    private static final String ARG_1 = "arg:1";
    private static final String INT_ADD = "int:add";
    private static final String INT_SUB = "int:sub";
    private static final String PROD = "prod:";
    private static final String INT_LT = "int:lt";
    private static final String INT_LE = "int:le";
    private static final String INT_EQ = "int:eq";
    private static final String INT_GE = "int:ge";
    private static final String INT_GT = "int:gt";
    private boolean doLayout;

    public ActivityDiagramToGrooveTransformer(boolean doLayout) {
        this.doLayout = doLayout;
    }

    @Override
    public GrooveGraph generateStartGraph(ActivityDiagram activityDiagram, boolean addPrefix) {
        GrooveGraphBuilder builder = new GrooveGraphBuilder().setName(activityDiagram.getName());

        Map<ActivityNode, GrooveNode> createdNodesIndex = new LinkedHashMap<>();

        this.createNodeForActivityDiagramAndInitial(activityDiagram, builder, createdNodesIndex);

        Map<String, GrooveNode> variableNameToNode = new HashMap<>();
        this.createNodesForLocalAndInputVariables(activityDiagram, builder, variableNameToNode);

        this.createNodesForActivityNodes(activityDiagram, builder, createdNodesIndex, variableNameToNode);

        this.addControlFlowsToGraph(activityDiagram, builder, createdNodesIndex, variableNameToNode);

        return builder.build();
    }

    private void createNodesForActivityNodes(
            ActivityDiagram activityDiagram,
            GrooveGraphBuilder builder,
            Map<ActivityNode, GrooveNode> createdNodesIndex,
            Map<String, GrooveNode> variableNameToNode) {
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
    }

    private void addControlFlowsToGraph(
            ActivityDiagram activityDiagram,
            GrooveGraphBuilder builder,
            Map<ActivityNode, GrooveNode> createdNodesIndex,
            Map<String, GrooveNode> variableNameToNode) {
        activityDiagram.getEdges().forEach(
                edge -> {
                    GrooveNode controlFlowNode = new GrooveNode(TYPE_CONTROL_FLOW);
                    builder.addNode(controlFlowNode);
                    builder.addEdge(SOURCE, controlFlowNode, createdNodesIndex.get(edge.getSource()));
                    builder.addEdge(TARGET, controlFlowNode, createdNodesIndex.get(edge.getTarget()));
                    if (edge.getSource().isDecisionNode()) {
                        this.connectGuardOrAddDefaultGuard(builder, variableNameToNode, edge, controlFlowNode);
                    }
                });
    }

    private void connectGuardOrAddDefaultGuard(
            GrooveGraphBuilder builder,
            Map<String, GrooveNode> variableNameToNode,
            ControlFlow decisionEdge,
            GrooveNode decisionEdgeGrooveNode) {
        GrooveNode varNode;
        if (decisionEdge.getGuardIfExists() != null) {
            varNode = variableNameToNode.get(decisionEdge.getGuardIfExists().getName());
        } else {
            varNode = new GrooveNode(TYPE_VARIABLE);
            GrooveNode boolValue = new GrooveNode(TYPE_BOOLEAN_VALUE);
            builder.addEdge(VALUE, varNode, boolValue);
            builder.addEdge(VALUE, boolValue, new GrooveNode(TRUE));
        }
        builder.addEdge(GUARD, decisionEdgeGrooveNode, varNode);
    }

    private void createNodesForLocalAndInputVariables(
            ActivityDiagram activityDiagram,
            GrooveGraphBuilder builder,
            Map<String, GrooveNode> variableNameToNode) {
        activityDiagram.localVariables().forEach(localVar -> this.createAndInitVariable(builder, localVar, variableNameToNode));
        activityDiagram.inputVariables().forEach(inputVariable -> {
            // TODO: This should be done according to given parameters of the input variables NOT the initial values.
            this.createAndInitVariable(builder, inputVariable, variableNameToNode);
        });
    }

    private void createNodeForActivityDiagramAndInitial(
            ActivityDiagram activityDiagram,
            GrooveGraphBuilder builder,
            Map<ActivityNode, GrooveNode> createdNodesIndex) {
        InitialNode initialNode = activityDiagram.getInitialNode();
        GrooveNode initialNodeGroove = new GrooveNode(TYPE_INITIAL_NODE);
        builder.addNode(initialNodeGroove);
        createdNodesIndex.put(initialNode, initialNodeGroove);

        GrooveNode activityDiagramNode = new GrooveNode(TYPE_ACTIVITY_DIAGRAM);
        activityDiagramNode.addAttribute("running", false);
        activityDiagramNode.addAttribute("name", activityDiagram.getName());
        builder.addNode(activityDiagramNode);
        builder.addEdge("start", activityDiagramNode, initialNodeGroove);
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
        GrooveRuleBuilder ruleBuilder = new GrooveRuleBuilder(activityDiagram, addPrefix);

        this.addStartRule(activityDiagram, ruleBuilder);

        activityDiagram.getNodes().forEach(activityNode -> {
            ruleBuilder.startRule(activityNode.getName());
            activityNode.accept(new ActivityNodeVisitor() {
                @Override
                public void handle(DecisionNode decisionNode) {
                    GrooveNode decision = ruleBuilder.contextNode(TYPE_DECISION_NODE);

                    GrooveNode outFlow = ActivityDiagramToGrooveTransformer.this.addTokenFlow(decision, ruleBuilder);

                    // Connected guard must be true
                    GrooveNode guardVar = ruleBuilder.contextNode(TYPE_VARIABLE);
                    ruleBuilder.contextEdge(GUARD, outFlow, guardVar);
                    GrooveNode boolValue = ruleBuilder.contextNode(TYPE_BOOLEAN_VALUE);
                    ruleBuilder.contextEdge(VALUE, guardVar, boolValue);
                    GrooveNode trueValue = ruleBuilder.contextNode(TRUE);
                    ruleBuilder.contextEdge(VALUE, boolValue, trueValue);
                }

                @Override
                public void handle(ForkNode forkNode) {
                    GrooveNode fork = ruleBuilder.contextNode(TYPE_FORK_NODE);
                    GrooveNode inFlow = ruleBuilder.contextNode(TYPE_CONTROL_FLOW);
                    ruleBuilder.contextEdge(TARGET, inFlow, fork);

                    GrooveNode controlToken = ruleBuilder.contextNode(TYPE_CONTROL_TOKEN);
                    ruleBuilder.deleteEdge(TOKENS, inFlow, controlToken);

                    AtomicReference<GrooveNode> previousFlow = new AtomicReference<>();
                    forkNode.getOutgoingFlows().forEach(controlFlow -> {
                        GrooveNode outFlow = ruleBuilder.contextNode(TYPE_CONTROL_FLOW);
                        ruleBuilder.contextEdge(SOURCE, outFlow, fork);
                        if (previousFlow.get() != null) {
                            ruleBuilder.contextEdge(UNEQUALS, previousFlow.get(), outFlow);
                        }
                        previousFlow.set(outFlow);

                        GrooveNode forkedToken = ruleBuilder.addNode(TYPE_FORKED_TOKEN);
                        ruleBuilder.addEdge(BASE_TOKEN, forkedToken, controlToken);
                        ruleBuilder.addEdge(TOKENS, outFlow, forkedToken);
                    });
                }

                @Override
                public void handle(InitialNode initialNode) {
                    GrooveNode initNode = ruleBuilder.contextNode(TYPE_INITIAL_NODE);
                    GrooveNode controlFlowNode = ruleBuilder.contextNode(TYPE_CONTROL_FLOW);
                    ruleBuilder.contextEdge(SOURCE, controlFlowNode, initNode);

                    GrooveNode activityNode = ruleBuilder.contextNode(TYPE_ACTIVITY_NODE);
                    ruleBuilder.contextEdge(TARGET, controlFlowNode, activityNode);

                    GrooveNode tokenNode = ruleBuilder.contextNode(TYPE_TOKEN);
                    ruleBuilder.deleteEdge(TOKENS, initNode, tokenNode);
                    ruleBuilder.addEdge(TOKENS, controlFlowNode, tokenNode);
                }

                @Override
                public void handle(JoinNode joinNode) {
                    GrooveNode join = ruleBuilder.contextNode(TYPE_JOIN_NODE);
                    GrooveNode outFlow = ruleBuilder.contextNode(TYPE_CONTROL_FLOW);
                    ruleBuilder.contextEdge(SOURCE, outFlow, join);

                    GrooveNode token = ruleBuilder.contextNode(TYPE_TOKEN);
                    ruleBuilder.addEdge(TOKENS, outFlow, token);

                    AtomicReference<GrooveNode> previousFlow = new AtomicReference<>();
                    joinNode.getIncomingFlows().forEach(controlFlow -> {
                        GrooveNode inFlow = ruleBuilder.contextNode(TYPE_CONTROL_FLOW);
                        ruleBuilder.contextEdge(TARGET, inFlow, join);
                        if (previousFlow.get() != null) {
                            ruleBuilder.contextEdge(UNEQUALS, previousFlow.get(), inFlow);
                        }
                        previousFlow.set(inFlow);

                        GrooveNode forkedToken = ruleBuilder.deleteNode(TYPE_FORKED_TOKEN);
                        ruleBuilder.deleteEdge(TOKENS, inFlow, forkedToken);
                        ruleBuilder.deleteEdge(BASE_TOKEN, forkedToken, token);
                    });

                }

                @Override
                public void handle(MergeNode mergeNode) {
                    GrooveNode merge = ruleBuilder.contextNode(TYPE_MERGE_NODE);
                    ActivityDiagramToGrooveTransformer.this.addTokenFlow(merge, ruleBuilder);

                }

                @Override
                public void handle(OpaqueAction opaqueAction) {
                    GrooveNode action = ActivityDiagramToGrooveTransformer.this.createNodeWithStringAttributeAndType(ruleBuilder, TYPE_OPAQUE_ACTION, opaqueAction.getName(), "name");

                    ActivityDiagramToGrooveTransformer.this.addTokenFlow(action, ruleBuilder);

                    opaqueAction.expressions().forEach(
                            expression -> ActivityDiagramToGrooveTransformer.this.convertExpressionsToGroove(
                                    action,
                                    expression,
                                    ruleBuilder));
                }

                @Override
                public void handle(ActivityFinalNode activityFinalNode) {
                    GrooveNode activityDiagramNode = ActivityDiagramToGrooveTransformer.this.createDiagramNodeWithName(ruleBuilder, activityDiagram);

                    ruleBuilder.addEdge("running", activityDiagramNode, ruleBuilder.contextNode(FALSE));
                    ruleBuilder.deleteEdge("running", activityDiagramNode, ruleBuilder.contextNode(TRUE));

                    GrooveNode flowNode = ruleBuilder.contextNode(TYPE_CONTROL_FLOW);
                    GrooveNode finalNode = ruleBuilder.contextNode(TYPE_FINAL_NODE);
                    GrooveNode tokenNode = ruleBuilder.deleteNode(TYPE_TOKEN);

                    ruleBuilder.contextEdge(TARGET, flowNode, finalNode);
                    ruleBuilder.deleteEdge(TOKENS, flowNode, tokenNode);

                }
            });
            ruleBuilder.buildRule();
        });

        return ruleBuilder.getRules();
    }

    private void convertExpressionsToGroove(GrooveNode action, Expression expression, GrooveRuleBuilder ruleBuilder) {
        expression.accept(new ExpressionVisitor() {
            @Override
            public <VALUE extends Value> void handle(SetVariableExpression<VALUE> setVariableExpression) {
                GrooveNode exp = ruleBuilder.deleteNode(TYPE_SET_VARIABLE_EXPRESSION);
                ruleBuilder.deleteEdge(EXP, action, exp);

                // Create variable in context
                GrooveNode var = ActivityDiagramToGrooveTransformer.this.createContextVariableWithName(
                        setVariableExpression.getVariableToBeSet().getName(),
                        ruleBuilder);

                // Swap the value of the variable
                ruleBuilder.deleteEdge(VAR, exp, var);
                ruleBuilder.deleteEdge(VALUE, var, ruleBuilder.deleteNode(TYPE_VALUE));
                GrooveNode newValue = ruleBuilder.contextNode(TYPE_VALUE);
                ruleBuilder.deleteEdge(NEW_VALUE, exp, newValue);
                ruleBuilder.addEdge(VALUE, var, newValue);
            }

            @Override
            public void handle(IntegerCalculationExpression integerCalculationExpression) {
                GrooveNode exp = null;
                String grooveOperator = "";
                switch (integerCalculationExpression.getOperator()) {
                    case ADD:
                        exp = ruleBuilder.deleteNode(TYPE_SUM);
                        grooveOperator = INT_ADD;
                        break;
                    case SUBTRACT:
                        exp = ruleBuilder.deleteNode(TYPE_DIFFERENCE);
                        grooveOperator = INT_SUB;
                        break;
                }
                ruleBuilder.deleteEdge(EXP, action, exp);

                // Operand 1
                GrooveNode operand1ValueInt = ActivityDiagramToGrooveTransformer.this.createOperand(
                        exp,
                        ruleBuilder,
                        integerCalculationExpression.getNameOfOperand1(),
                        OPERAND_1,
                        TYPE_INTEGER_VALUE,
                        INT);

                // Operand 2
                GrooveNode operand2ValueInt = ActivityDiagramToGrooveTransformer.this.createOperand(
                        exp,
                        ruleBuilder,
                        integerCalculationExpression.getNameOfOperand2(),
                        OPERAND_2,
                        TYPE_INTEGER_VALUE,
                        INT);

                // Assignee
                GrooveNode newAssigneeValue = ActivityDiagramToGrooveTransformer.this.createAssignee(
                        exp,
                        ruleBuilder,
                        integerCalculationExpression.getNameOfAssignee(),
                        TYPE_INTEGER_VALUE,
                        INT);

                // Groove operator for calculation
                ActivityDiagramToGrooveTransformer.this.addGrooveNodesForCalculation(
                        grooveOperator,
                        operand1ValueInt,
                        operand2ValueInt,
                        newAssigneeValue,
                        ruleBuilder);
            }

            @Override
            public void handle(IntegerComparisonExpression integerComparisonExpression) {
                GrooveNode exp = null;
                String grooveOperator = "";
                switch (integerComparisonExpression.getOperator()) {
                    case SMALLER:
                        exp = ruleBuilder.deleteNode(TYPE_SMALLER);
                        grooveOperator = INT_LT;
                        break;
                    case SMALLER_EQUALS:
                        exp = ruleBuilder.deleteNode(TYPE_SMALLER_EQUALS);
                        grooveOperator = INT_LE;
                        break;
                    case EQUALS:
                        exp = ruleBuilder.deleteNode(TYPE_EQUALS);
                        grooveOperator = INT_EQ;
                        break;
                    case GREATER_EQUALS:
                        exp = ruleBuilder.deleteNode(TYPE_GREATER_EQUALS);
                        grooveOperator = INT_GE;
                        break;
                    case GREATER:
                        exp = ruleBuilder.deleteNode(TYPE_GREATER);
                        grooveOperator = INT_GT;
                        break;
                }
                ruleBuilder.deleteEdge(EXP, action, exp);

                // Operand 1
                GrooveNode operand1ValueInt = ActivityDiagramToGrooveTransformer.this.createOperand(
                        exp,
                        ruleBuilder,
                        integerComparisonExpression.getNameOfOperand1(),
                        OPERAND_1,
                        TYPE_INTEGER_VALUE,
                        INT);

                // Operand 2
                GrooveNode operand2ValueInt = ActivityDiagramToGrooveTransformer.this.createOperand(
                        exp,
                        ruleBuilder,
                        integerComparisonExpression.getNameOfOperand2(),
                        OPERAND_2,
                        TYPE_INTEGER_VALUE,
                        INT);

                // Assignee
                GrooveNode newAssigneeValue = ActivityDiagramToGrooveTransformer.this.createAssignee(
                        exp,
                        ruleBuilder,
                        integerComparisonExpression.getNameOfAssignee(),
                        TYPE_BOOLEAN_VALUE,
                        BOOL);

                // Groove operator for calculation
                ActivityDiagramToGrooveTransformer.this.addGrooveNodesForCalculation(
                        grooveOperator,
                        operand1ValueInt,
                        operand2ValueInt,
                        newAssigneeValue,
                        ruleBuilder);
            }

            @Override
            public void handle(BooleanBinaryExpression booleanBinaryExpression) {
                GrooveNode exp = null;
                String grooveOperator = "";
                switch (booleanBinaryExpression.getOperator()) {
                    case AND:
                        exp = ruleBuilder.deleteNode(TYPE_AND);
                        grooveOperator = BOOL_AND;
                        break;
                    case OR:
                        exp = ruleBuilder.deleteNode(TYPE_OR);
                        grooveOperator = BOOL_OR;
                        break;
                }
                ruleBuilder.deleteEdge(EXP, action, exp);

                // Operand 1
                GrooveNode operand1ValueBool = ActivityDiagramToGrooveTransformer.this.createOperand(
                        exp,
                        ruleBuilder,
                        booleanBinaryExpression.getNameOfOperand1(),
                        OPERAND_1,
                        TYPE_BOOLEAN_VALUE,
                        BOOL);

                // Operand 2
                GrooveNode operand2ValueBool = ActivityDiagramToGrooveTransformer.this.createOperand(
                        exp,
                        ruleBuilder,
                        booleanBinaryExpression.getNameOfOperand2(),
                        OPERAND_2,
                        TYPE_BOOLEAN_VALUE,
                        BOOL);

                // Assignee
                GrooveNode newAssigneeValue = ActivityDiagramToGrooveTransformer.this.createAssignee(
                        exp,
                        ruleBuilder,
                        booleanBinaryExpression.getNameOfAssignee(),
                        TYPE_BOOLEAN_VALUE,
                        BOOL);

                // Groove operator for calculation
                ActivityDiagramToGrooveTransformer.this.addGrooveNodesForCalculation(
                        grooveOperator,
                        operand1ValueBool,
                        operand2ValueBool,
                        newAssigneeValue,
                        ruleBuilder);
            }

            @Override
            public void handle(BooleanUnaryExpression booleanUnaryExpression) {
                GrooveNode exp = ruleBuilder.deleteNode(TYPE_NOT);
                ruleBuilder.deleteEdge(EXP, action, exp);

                // Operand
                GrooveNode operandValueBool = ActivityDiagramToGrooveTransformer.this.createOperand(
                        exp,
                        ruleBuilder,
                        booleanUnaryExpression.getOperand().getName(),
                        OPERAND_1,
                        TYPE_BOOLEAN_VALUE,
                        BOOL);

                // Assignee
                GrooveNode newAssigneeValue = ActivityDiagramToGrooveTransformer.this.createAssignee(
                        exp,
                        ruleBuilder,
                        booleanUnaryExpression.getAssignee().getName(),
                        TYPE_BOOLEAN_VALUE,
                        BOOL);

                // Groove operator for calculation
                GrooveNode operatorNode = ruleBuilder.contextNode(PROD);
                ruleBuilder.contextEdge(ARG_0, operatorNode, operandValueBool);
                ruleBuilder.contextEdge(BOOL_NOT, operatorNode, newAssigneeValue);
            }
        });
    }

    private GrooveNode createOperand(
            GrooveNode exp,
            GrooveRuleBuilder ruleBuilder,
            String operandVariableName,
            String operandEdgeName,
            String activityDiagramValueType,
            String primitiveGrooveValueType) {
        GrooveNode operand2Var = ActivityDiagramToGrooveTransformer.this.createContextVariableWithName(
                operandVariableName,
                ruleBuilder);
        ruleBuilder.deleteEdge(operandEdgeName, exp, operand2Var);

        GrooveNode operand2Value = ruleBuilder.contextNode(activityDiagramValueType);
        ruleBuilder.contextEdge(VALUE, operand2Var, operand2Value);
        GrooveNode operand2ValueInt = ruleBuilder.contextNode(primitiveGrooveValueType);
        ruleBuilder.contextEdge(VALUE, operand2Value, operand2ValueInt);

        return operand2ValueInt;
    }

    private GrooveNode createAssignee(
            GrooveNode exp,
            GrooveRuleBuilder ruleBuilder,
            String assigneeVarName,
            String activityDiagramValueType,
            String primitiveGrooveValueType) {
        GrooveNode assigneeVar = ActivityDiagramToGrooveTransformer.this.createContextVariableWithName(
                assigneeVarName,
                ruleBuilder);
        ruleBuilder.deleteEdge(ASSIGNEE, exp, assigneeVar);

        GrooveNode assigneeValue = ruleBuilder.contextNode(activityDiagramValueType);
        ruleBuilder.contextEdge(VALUE, assigneeVar, assigneeValue);
        GrooveNode oldAssigneeValue = ruleBuilder.contextNode(primitiveGrooveValueType);
        ruleBuilder.deleteEdge(VALUE, assigneeValue, oldAssigneeValue);
        GrooveNode newAssigneeValue = ruleBuilder.contextNode(primitiveGrooveValueType);
        ruleBuilder.addEdge(VALUE, assigneeValue, newAssigneeValue);

        return newAssigneeValue;
    }

    private void addGrooveNodesForCalculation(
            String grooveOperator,
            GrooveNode operand1Value,
            GrooveNode operand2Value,
            GrooveNode newAssigneeValue,
            GrooveRuleBuilder ruleBuilder) {
        GrooveNode operatorNode = ruleBuilder.contextNode(PROD);
        ruleBuilder.contextEdge(ARG_0, operatorNode, operand1Value);
        ruleBuilder.contextEdge(ARG_1, operatorNode, operand2Value);
        ruleBuilder.contextEdge(grooveOperator, operatorNode, newAssigneeValue);
    }

    private GrooveNode createContextVariableWithName(String variableName, GrooveRuleBuilder ruleBuilder) {
        return this.createNodeWithStringAttributeAndType(
                ruleBuilder,
                TYPE_VARIABLE,
                variableName,
                NAME);
    }

    private GrooveNode createNodeWithStringAttributeAndType(
            GrooveRuleBuilder ruleBuilder,
            String nodeType,
            String attributeValue,
            String attributeName) {
        GrooveNode var = ruleBuilder.contextNode(nodeType);
        GrooveNode varName = ruleBuilder.contextNode(
                ActivityDiagramToGrooveTransformer.this.createStringNodeLabel(attributeValue));
        ruleBuilder.contextEdge(attributeName, var, varName);
        return var;
    }

    private GrooveNode addTokenFlow(GrooveNode activityNode, GrooveRuleBuilder ruleBuilder) {
        GrooveNode inFlow = ruleBuilder.contextNode(TYPE_CONTROL_FLOW);
        ruleBuilder.contextEdge(TARGET, inFlow, activityNode);

        GrooveNode outFlow = ruleBuilder.contextNode(TYPE_CONTROL_FLOW);
        ruleBuilder.contextEdge(SOURCE, outFlow, activityNode);

        GrooveNode token = ruleBuilder.contextNode(TYPE_TOKEN);
        ruleBuilder.deleteEdge(TOKENS, inFlow, token);
        ruleBuilder.addEdge(TOKENS, outFlow, token);

        return outFlow;
    }

    private GrooveNode createDiagramNodeWithName(GrooveRuleBuilder ruleBuilder, ActivityDiagram activityDiagram) {
        return this.createNodeWithStringAttributeAndType(
                ruleBuilder,
                TYPE_ACTIVITY_DIAGRAM,
                activityDiagram.getName(),
                "name");
    }

    private String createStringNodeLabel(String stringValue) {
        return String.format("%s\"%s\"", STRING, stringValue);
    }

    private void addStartRule(ActivityDiagram activityDiagram, GrooveRuleBuilder ruleBuilder) {
        ruleBuilder.startRule(String.format("%s_start", activityDiagram.getName()));

        GrooveNode activityDiagramNode = this.createDiagramNodeWithName(ruleBuilder, activityDiagram);

        GrooveNode falseNode = ruleBuilder.contextNode(FALSE);
        GrooveNode trueNode = ruleBuilder.contextNode(TRUE);
        ruleBuilder.deleteEdge("running", activityDiagramNode, falseNode);
        ruleBuilder.addEdge("running", activityDiagramNode, trueNode);

        GrooveNode initNode = ruleBuilder.contextNode(TYPE_INITIAL_NODE);
        ruleBuilder.contextEdge("start", activityDiagramNode, initNode);
        GrooveNode token = ruleBuilder.addNode(TYPE_CONTROL_TOKEN);
        ruleBuilder.addEdge(TOKENS, initNode, token);

        ruleBuilder.buildRule();
    }

    @Override
    public void generateAndWriteRulesFurther(ActivityDiagram activityDiagram, boolean addPrefix, File targetFolder) {
        this.copyTypeGraph(targetFolder);
    }

    @Override
    public boolean isLayoutActivated() {
        return this.doLayout;
    }

    private void copyTypeGraph(File targetFolder) {
        File sourceDirectory = new File(this.getClass().getResource("/ActivityDiagram").getFile());
        try {
            FileUtils.copyDirectory(sourceDirectory, targetFolder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
