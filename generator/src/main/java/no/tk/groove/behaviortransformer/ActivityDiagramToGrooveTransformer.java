package no.tk.groove.behaviortransformer;

import io.github.timkraeuter.groove.graph.GrooveGraph;
import io.github.timkraeuter.groove.graph.GrooveGraphBuilder;
import io.github.timkraeuter.groove.graph.GrooveNode;
import io.github.timkraeuter.groove.rule.GrooveGraphRule;
import io.github.timkraeuter.groove.rule.GrooveRuleBuilder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import no.tk.behavior.activity.ActivityDiagram;
import no.tk.behavior.activity.expression.Expression;
import no.tk.behavior.activity.expression.SetVariableExpression;
import no.tk.behavior.activity.expression.bool.BooleanBinaryExpression;
import no.tk.behavior.activity.expression.bool.BooleanUnaryExpression;
import no.tk.behavior.activity.expression.integer.IntegerCalculationExpression;
import no.tk.behavior.activity.expression.integer.IntegerComparisonExpression;
import no.tk.behavior.activity.expression.visitor.ExpressionVisitor;
import no.tk.behavior.activity.nodes.*;
import no.tk.behavior.activity.values.BooleanValue;
import no.tk.behavior.activity.values.IntegerValue;
import no.tk.behavior.activity.values.Value;
import no.tk.behavior.activity.values.ValueVisitor;
import no.tk.behavior.activity.variables.BooleanVariable;
import no.tk.behavior.activity.variables.Variable;
import no.tk.behavior.bpmn.auxiliary.exceptions.GrooveGenerationRuntimeException;
import no.tk.behavior.bpmn.auxiliary.exceptions.ShouldNotHappenRuntimeException;
import org.apache.commons.io.file.PathUtils;

public class ActivityDiagramToGrooveTransformer implements GrooveTransformer<ActivityDiagram> {
  private static final String TYPE_GRAPH_DIR = "/ActivityDiagramTypeGraph";

  // Possible node labels.
  private static final String TYPE_ACTIVITY_DIAGRAM = TYPE + "ActivityDiagram";
  private static final String TYPE_TOKEN = TYPE + "Token";
  private static final String TYPE_CONTROL_TOKEN = TYPE + "ControlToken";
  private static final String TYPE_FORKED_TOKEN = TYPE + "ForkedToken";

  private static final String TYPE_INTEGER_VALUE = TYPE + "IntegerValue";
  private static final String TYPE_BOOLEAN_VALUE = TYPE + "BooleanValue";

  private static final String TYPE_VARIABLE = TYPE + "Variable";

  // Edge labels
  private static final String VALUE = "value";
  private static final String NAME = "name";
  private static final String BASE_TOKEN = "baseToken";
  private static final String POSITION = "position";
  private static final String RUNNING = "running";

  private final boolean doLayout;

  public ActivityDiagramToGrooveTransformer(boolean doLayout) {
    this.doLayout = doLayout;
  }

  @Override
  public GrooveGraph generateStartGraph(ActivityDiagram activityDiagram) {
    GrooveGraphBuilder builder = new GrooveGraphBuilder().setName(activityDiagram.getName());

    GrooveNode activityDiagramNode = new GrooveNode(TYPE_ACTIVITY_DIAGRAM);
    activityDiagramNode.addAttribute(RUNNING, false);
    activityDiagramNode.addAttribute("name", activityDiagram.getName());
    builder.addNode(activityDiagramNode);

    this.createNodesForLocalAndInputVariables(activityDiagram, builder);

    return builder.build();
  }

  private void createNodesForLocalAndInputVariables(
      ActivityDiagram activityDiagram, GrooveGraphBuilder builder) {
    activityDiagram
        .localVariables()
        .forEach(localVar -> this.createAndInitVariable(builder, localVar));
    // TODO: This should be done according to given parameters of the input variables NOT the
    // initial values.
    activityDiagram
        .inputVariables()
        .forEach(inputVariable -> this.createAndInitVariable(builder, inputVariable));
  }

  private void createAndInitVariable(
      GrooveGraphBuilder builder, Variable<? extends Value> variable) {
    GrooveNode variableNode = new GrooveNode(TYPE_VARIABLE);
    variableNode.addAttribute(NAME, variable.getName());

    GrooveNode initialValueNode =
        ActivityDiagramToGrooveTransformer.this.createValueNode(variable.getInitialValue());
    builder.addEdge(VALUE, variableNode, initialValueNode);
  }

  private <V extends Value> GrooveNode createValueNode(V value) {
    return value.accept(
        new ValueVisitor<>() {
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
  public Stream<GrooveGraphRule> generateRules(ActivityDiagram activityDiagram) {
    GrooveRuleBuilder ruleBuilder = new GrooveRuleBuilder();

    this.addStartRule(activityDiagram, ruleBuilder);

    activityDiagram
        .getNodes()
        .forEach(
            activityNode ->
                activityNode.accept(
                    new ActivityNodeVisitor() {
                      @Override
                      public void handle(DecisionNode decisionNode) {
                        createDecisionNodeRules(decisionNode, ruleBuilder);
                      }

                      @Override
                      public void handle(ForkNode forkNode) {
                        createForkNodeRule(forkNode, ruleBuilder);
                      }

                      @Override
                      public void handle(InitialNode initialNode) {
                        createInitialNodeRule(initialNode, ruleBuilder);
                      }

                      @Override
                      public void handle(JoinNode joinNode) {
                        createJointNodeRule(joinNode, ruleBuilder);
                      }

                      @Override
                      public void handle(MergeNode mergeNode) {
                        createMergeNodeRules(mergeNode, ruleBuilder);
                      }

                      @Override
                      public void handle(OpaqueAction opaqueAction) {
                        createOpaqueActionRules(opaqueAction, ruleBuilder);
                      }

                      @Override
                      public void handle(ActivityFinalNode activityFinalNode) {
                        createActivityFinalNodeRule(
                            activityFinalNode, ruleBuilder, activityDiagram);
                      }
                    }));

    return ruleBuilder.getRules();
  }

  private void createDecisionNodeRules(DecisionNode decisionNode, GrooveRuleBuilder ruleBuilder) {
    decisionNode
        .getOutgoingFlows()
        .forEach(
            controlFlow -> {
              String targetName = controlFlow.getTarget().getName();
              ruleBuilder.startRule(decisionNode.getName() + "_to_" + targetName);

              ActivityDiagramToGrooveTransformer.this.updateTokenPosition(
                  decisionNode.getName(), targetName, ruleBuilder);

              // Guard
              BooleanVariable guardIfExists = controlFlow.getGuardIfExists();
              if (guardIfExists != null) {
                GrooveNode guardVar =
                    ActivityDiagramToGrooveTransformer.this.createContextVariableWithName(
                        guardIfExists.getName(), ruleBuilder);

                GrooveNode boolValue = ruleBuilder.contextNode(TYPE_BOOLEAN_VALUE);
                ruleBuilder.contextEdge(VALUE, guardVar, boolValue);
                GrooveNode trueValue = ruleBuilder.contextNode(TRUE);
                ruleBuilder.contextEdge(VALUE, boolValue, trueValue);
              }

              ruleBuilder.buildRule();
            });
  }

  private void createForkNodeRule(ForkNode forkNode, GrooveRuleBuilder ruleBuilder) {
    String forkNodeName = forkNode.getName();

    ruleBuilder.startRule(forkNodeName);

    GrooveNode token = ruleBuilder.contextNode(TYPE_TOKEN);
    GrooveNode oldTokenPosition =
        ruleBuilder.contextNode(GrooveTransformerHelper.createStringNodeLabel(forkNodeName));
    ruleBuilder.deleteEdge(POSITION, token, oldTokenPosition);

    forkNode
        .getOutgoingFlows()
        .forEach(
            controlFlow -> {
              GrooveNode forkedToken = ruleBuilder.addNode(TYPE_FORKED_TOKEN);
              ruleBuilder.addEdge(BASE_TOKEN, forkedToken, token);
              GrooveNode forkedTokenPosition =
                  ruleBuilder.contextNode(
                      GrooveTransformerHelper.createStringNodeLabel(
                          controlFlow.getTarget().getName()));
              ruleBuilder.addEdge(POSITION, forkedToken, forkedTokenPosition);
            });

    ruleBuilder.buildRule();
  }

  private void createInitialNodeRule(InitialNode initialNode, GrooveRuleBuilder ruleBuilder) {
    if (initialNode.getOutgoingFlows().count() != 1L) {
      throw new GrooveGenerationRuntimeException(
          "The initial node must have exactly one outgoing flow!");
    }

    String initialNodeName = initialNode.getName();
    initialNode
        .getOutgoingFlows()
        .forEach(
            controlFlow -> {
              ruleBuilder.startRule(initialNodeName);

              ActivityDiagramToGrooveTransformer.this.updateTokenPosition(
                  initialNodeName, controlFlow.getTarget().getName(), ruleBuilder);

              ruleBuilder.buildRule();
            });
  }

  private void createJointNodeRule(JoinNode joinNode, GrooveRuleBuilder ruleBuilder) {
    if (joinNode.getOutgoingFlows().count() != 1L) {
      throw new GrooveGenerationRuntimeException(
          "A join node must have exactly one outgoing flow!");
    }
    joinNode
        .getOutgoingFlows()
        .forEach(
            outFlow -> {
              String joinNodeName = joinNode.getName();
              ruleBuilder.startRule(joinNodeName);

              GrooveNode baseToken = ruleBuilder.contextNode(TYPE_TOKEN);
              GrooveNode newTokenPosition =
                  ruleBuilder.contextNode(
                      GrooveTransformerHelper.createStringNodeLabel(outFlow.getTarget().getName()));
              ruleBuilder.addEdge(POSITION, baseToken, newTokenPosition);

              AtomicReference<GrooveNode> previousToken = new AtomicReference<>();
              joinNode
                  .getIncomingFlows()
                  .forEach(
                      controlFlow -> {
                        GrooveNode forkedToken = ruleBuilder.deleteNode(TYPE_FORKED_TOKEN);
                        if (previousToken.get() != null) {
                          ruleBuilder.contextEdge(UNEQUALS, previousToken.get(), forkedToken);
                        }
                        previousToken.set(forkedToken);

                        GrooveNode forkedTokenPosition =
                            ruleBuilder.contextNode(
                                GrooveTransformerHelper.createStringNodeLabel(joinNodeName));
                        ruleBuilder.contextEdge(POSITION, forkedToken, forkedTokenPosition);
                        ruleBuilder.deleteEdge(BASE_TOKEN, forkedToken, baseToken);
                      });

              ruleBuilder.buildRule();
            });
  }

  private void createMergeNodeRules(MergeNode mergeNode, GrooveRuleBuilder ruleBuilder) {
    if (mergeNode.getOutgoingFlows().count() != 1L) {
      throw new GrooveGenerationRuntimeException(
          "A merge node must have exactly one outgoing flow!");
    }
    mergeNode
        .getOutgoingFlows()
        .forEach(
            controlFlow -> {
              String mergeNodeName = mergeNode.getName();
              ruleBuilder.startRule(mergeNodeName);
              ActivityDiagramToGrooveTransformer.this.updateTokenPosition(
                  mergeNodeName, controlFlow.getTarget().getName(), ruleBuilder);
              ruleBuilder.buildRule();
            });
  }

  private void createOpaqueActionRules(OpaqueAction opaqueAction, GrooveRuleBuilder ruleBuilder) {
    if (opaqueAction.getOutgoingFlows().count() != 1L) {
      throw new GrooveGenerationRuntimeException(
          "An opaque action must have exactly one outgoing flow!");
    }

    opaqueAction
        .getOutgoingFlows()
        .forEach(
            controlFlow -> {
              ruleBuilder.startRule(opaqueAction.getName());

              ActivityDiagramToGrooveTransformer.this.updateTokenPosition(
                  opaqueAction.getName(), controlFlow.getTarget().getName(), ruleBuilder);
              opaqueAction
                  .expressions()
                  .forEach(
                      expression ->
                          ActivityDiagramToGrooveTransformer.this.convertExpressionsToGroove(
                              expression, ruleBuilder));

              ruleBuilder.buildRule();
            });
  }

  private void createActivityFinalNodeRule(
      ActivityFinalNode activityFinalNode,
      GrooveRuleBuilder ruleBuilder,
      ActivityDiagram activityDiagram) {
    String finalNodeName = activityFinalNode.getName();
    ruleBuilder.startRule(finalNodeName);
    GrooveNode activityDiagramNode =
        ActivityDiagramToGrooveTransformer.this.createDiagramNodeWithName(
            ruleBuilder, activityDiagram);
    ruleBuilder.addEdge(RUNNING, activityDiagramNode, ruleBuilder.contextNode(FALSE));
    ruleBuilder.deleteEdge(RUNNING, activityDiagramNode, ruleBuilder.contextNode(TRUE));

    GrooveNode token = ruleBuilder.deleteNode(TYPE_CONTROL_TOKEN);
    GrooveNode initStringAttribute =
        ruleBuilder.contextNode(GrooveTransformerHelper.createStringNodeLabel(finalNodeName));
    ruleBuilder.deleteEdge(POSITION, token, initStringAttribute);

    ruleBuilder.buildRule();
  }

  private void updateTokenPosition(
      String oldPosition, String newPosition, GrooveRuleBuilder ruleBuilder) {
    GrooveNode token = ruleBuilder.contextNode(TYPE_TOKEN);
    GrooveNode oldTokenPosition =
        ruleBuilder.contextNode(GrooveTransformerHelper.createStringNodeLabel(oldPosition));
    ruleBuilder.deleteEdge(POSITION, token, oldTokenPosition);

    GrooveNode newTokenPosition =
        ruleBuilder.contextNode(GrooveTransformerHelper.createStringNodeLabel(newPosition));
    ruleBuilder.addEdge(POSITION, token, newTokenPosition);
  }

  private void convertExpressionsToGroove(Expression expression, GrooveRuleBuilder ruleBuilder) {
    expression.accept(
        new ExpressionVisitor() {
          @Override
          public <V extends Value> void handle(SetVariableExpression<V> setVariableExpression) {
            // Create variable in context
            GrooveNode variable =
                ActivityDiagramToGrooveTransformer.this.createContextVariableWithName(
                    setVariableExpression.getVariableToBeSet().getName(), ruleBuilder);

            // Swap the value of the variable
            setVariableExpression
                .getValue()
                .accept(
                    new ValueVisitor<Void>() {
                      @Override
                      public Void handle(IntegerValue integerValue) {
                        GrooveNode valueNode = ruleBuilder.contextNode(TYPE_INTEGER_VALUE);
                        ruleBuilder.contextEdge(VALUE, variable, valueNode);
                        ruleBuilder.deleteEdge(VALUE, valueNode, ruleBuilder.contextNode(INT));
                        GrooveNode newValue =
                            ruleBuilder.contextNode(INT + integerValue.getValue());
                        ruleBuilder.addEdge(VALUE, valueNode, newValue);
                        return null;
                      }

                      @Override
                      public Void handle(BooleanValue booleanValue) {
                        GrooveNode valueNode = ruleBuilder.contextNode(TYPE_BOOLEAN_VALUE);
                        ruleBuilder.contextEdge(VALUE, variable, valueNode);
                        GrooveNode newValue;
                        if (booleanValue.getValue()) {
                          newValue = ruleBuilder.contextNode(TRUE);
                        } else {
                          newValue = ruleBuilder.contextNode(FALSE);
                        }
                        ruleBuilder.deleteEdge(VALUE, valueNode, ruleBuilder.contextNode(BOOL));
                        ruleBuilder.addEdge(VALUE, valueNode, newValue);
                        return null;
                      }
                    });
          }

          @Override
          public void handle(IntegerCalculationExpression integerCalculationExpression) {
            String grooveOperator = "";
            switch (integerCalculationExpression.getOperator()) {
              case ADD:
                grooveOperator = INT_ADD;
                break;
              case SUBTRACT:
                grooveOperator = INT_SUB;
                break;
            }

            // Operand 1
            GrooveNode operand1ValueInt =
                ActivityDiagramToGrooveTransformer.this.createOperand(
                    ruleBuilder,
                    integerCalculationExpression.getNameOfOperand1(),
                    TYPE_INTEGER_VALUE,
                    INT);

            // Operand 2
            GrooveNode operand2ValueInt =
                ActivityDiagramToGrooveTransformer.this.createOperand(
                    ruleBuilder,
                    integerCalculationExpression.getNameOfOperand2(),
                    TYPE_INTEGER_VALUE,
                    INT);

            // Assignee
            GrooveNode newAssigneeValue =
                ActivityDiagramToGrooveTransformer.this.createAssignee(
                    ruleBuilder,
                    integerCalculationExpression.getNameOfAssignee(),
                    TYPE_INTEGER_VALUE,
                    INT);

            // Groove operator for calculation
            ActivityDiagramToGrooveTransformer.this.addGrooveNodesForCalculation(
                grooveOperator, operand1ValueInt, operand2ValueInt, newAssigneeValue, ruleBuilder);
          }

          @Override
          public void handle(IntegerComparisonExpression integerComparisonExpression) {
            String grooveOperator = "";
            switch (integerComparisonExpression.getOperator()) {
              case SMALLER:
                grooveOperator = INT_LT;
                break;
              case SMALLER_EQUALS:
                grooveOperator = INT_LE;
                break;
              case EQUALS:
                grooveOperator = INT_EQ;
                break;
              case GREATER_EQUALS:
                grooveOperator = INT_GE;
                break;
              case GREATER:
                grooveOperator = INT_GT;
                break;
            }

            // Operand 1
            GrooveNode operand1ValueInt =
                ActivityDiagramToGrooveTransformer.this.createOperand(
                    ruleBuilder,
                    integerComparisonExpression.getNameOfOperand1(),
                    TYPE_INTEGER_VALUE,
                    INT);

            // Operand 2
            GrooveNode operand2ValueInt =
                ActivityDiagramToGrooveTransformer.this.createOperand(
                    ruleBuilder,
                    integerComparisonExpression.getNameOfOperand2(),
                    TYPE_INTEGER_VALUE,
                    INT);

            // Assignee
            GrooveNode newAssigneeValue =
                ActivityDiagramToGrooveTransformer.this.createAssignee(
                    ruleBuilder,
                    integerComparisonExpression.getNameOfAssignee(),
                    TYPE_BOOLEAN_VALUE,
                    BOOL);

            // Groove operator for calculation
            ActivityDiagramToGrooveTransformer.this.addGrooveNodesForCalculation(
                grooveOperator, operand1ValueInt, operand2ValueInt, newAssigneeValue, ruleBuilder);
          }

          @Override
          public void handle(BooleanBinaryExpression booleanBinaryExpression) {
            String grooveOperator = "";
            switch (booleanBinaryExpression.getOperator()) {
              case AND:
                grooveOperator = BOOL_AND;
                break;
              case OR:
                grooveOperator = BOOL_OR;
                break;
            }

            // Operand 1
            GrooveNode operand1ValueBool =
                ActivityDiagramToGrooveTransformer.this.createOperand(
                    ruleBuilder,
                    booleanBinaryExpression.getNameOfOperand1(),
                    TYPE_BOOLEAN_VALUE,
                    BOOL);

            // Operand 2
            GrooveNode operand2ValueBool =
                ActivityDiagramToGrooveTransformer.this.createOperand(
                    ruleBuilder,
                    booleanBinaryExpression.getNameOfOperand2(),
                    TYPE_BOOLEAN_VALUE,
                    BOOL);

            // Assignee
            GrooveNode newAssigneeValue =
                ActivityDiagramToGrooveTransformer.this.createAssignee(
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
            // Operand
            GrooveNode operandValueBool =
                ActivityDiagramToGrooveTransformer.this.createOperand(
                    ruleBuilder,
                    booleanUnaryExpression.getOperand().getName(),
                    TYPE_BOOLEAN_VALUE,
                    BOOL);

            // Assignee
            GrooveNode newAssigneeValue =
                ActivityDiagramToGrooveTransformer.this.createAssignee(
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
      GrooveRuleBuilder ruleBuilder,
      String operandVariableName,
      String activityDiagramValueType,
      String primitiveGrooveValueType) {
    GrooveNode operandVar =
        ActivityDiagramToGrooveTransformer.this.createContextVariableWithName(
            operandVariableName, ruleBuilder);

    GrooveNode operandValue = ruleBuilder.contextNode(activityDiagramValueType);
    ruleBuilder.contextEdge(VALUE, operandVar, operandValue);
    GrooveNode operand2ValueInt = ruleBuilder.contextNode(primitiveGrooveValueType);
    ruleBuilder.contextEdge(VALUE, operandValue, operand2ValueInt);

    return operand2ValueInt;
  }

  private GrooveNode createAssignee(
      GrooveRuleBuilder ruleBuilder,
      String assigneeVarName,
      String activityDiagramValueType,
      String primitiveGrooveValueType) {
    GrooveNode assigneeVar =
        ActivityDiagramToGrooveTransformer.this.createContextVariableWithName(
            assigneeVarName, ruleBuilder);

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

  private GrooveNode createContextVariableWithName(
      String variableName, GrooveRuleBuilder ruleBuilder) {
    return this.createNodeWithStringAttributeAndType(
        ruleBuilder, TYPE_VARIABLE, variableName, NAME);
  }

  private GrooveNode createNodeWithStringAttributeAndType(
      GrooveRuleBuilder ruleBuilder, String nodeType, String attributeValue, String attributeName) {
    GrooveNode variable = ruleBuilder.contextNode(nodeType);
    GrooveNode varName =
        ruleBuilder.contextNode(GrooveTransformerHelper.createStringNodeLabel(attributeValue));
    ruleBuilder.contextEdge(attributeName, variable, varName);
    return variable;
  }

  private GrooveNode createDiagramNodeWithName(
      GrooveRuleBuilder ruleBuilder, ActivityDiagram activityDiagram) {
    return this.createNodeWithStringAttributeAndType(
        ruleBuilder, TYPE_ACTIVITY_DIAGRAM, activityDiagram.getName(), "name");
  }

  private void addStartRule(ActivityDiagram activityDiagram, GrooveRuleBuilder ruleBuilder) {
    ruleBuilder.startRule(String.format("%s_start", activityDiagram.getName()));

    GrooveNode activityDiagramNode = this.createDiagramNodeWithName(ruleBuilder, activityDiagram);

    GrooveNode falseNode = ruleBuilder.contextNode(FALSE);
    GrooveNode trueNode = ruleBuilder.contextNode(TRUE);
    ruleBuilder.deleteEdge(RUNNING, activityDiagramNode, falseNode);
    ruleBuilder.addEdge(RUNNING, activityDiagramNode, trueNode);

    GrooveNode token = ruleBuilder.addNode(TYPE_CONTROL_TOKEN);
    GrooveNode initStringAttribute =
        ruleBuilder.contextNode(
            GrooveTransformerHelper.createStringNodeLabel(
                activityDiagram.getInitialNode().getName()));
    ruleBuilder.addEdge(POSITION, token, initStringAttribute);

    ruleBuilder.buildRule();
  }

  @Override
  public void generateAndWriteRulesFurther(ActivityDiagram activityDiagram, Path targetFolder) {
    this.copyTypeGraph(targetFolder);
  }

  @Override
  public boolean isLayoutActivated() {
    return this.doLayout;
  }

  private void copyTypeGraph(Path targetFolder) {
    try {
      Path sourceDirectory = Paths.get(this.getClass().getResource(TYPE_GRAPH_DIR).toURI());
      PathUtils.copyDirectory(sourceDirectory, targetFolder, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException | URISyntaxException e) {
      throw new ShouldNotHappenRuntimeException(e);
    }
  }
}
