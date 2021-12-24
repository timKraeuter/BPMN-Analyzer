package groove.behaviorTransformer;

import behavior.activity.ActivityDiagram;
import behavior.activity.ActivityDiagramBuilder;
import behavior.activity.expression.SetVariableExpression;
import behavior.activity.expression.bool.BooleanBinaryExpression;
import behavior.activity.expression.bool.BooleanBinaryOperator;
import behavior.activity.expression.bool.BooleanUnaryExpression;
import behavior.activity.expression.bool.BooleanUnaryOperator;
import behavior.activity.expression.integer.IntegerCalculationExpression;
import behavior.activity.expression.integer.IntegerCalculationOperator;
import behavior.activity.expression.integer.IntegerComparisonExpression;
import behavior.activity.expression.integer.IntegerComparisonOperator;
import behavior.activity.nodes.*;
import behavior.activity.values.IntegerValue;
import behavior.activity.variables.BooleanVariable;
import behavior.activity.variables.IntegerVariable;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;

class ActivityDiagramToGrooveTransformerTest implements BehaviorToGrooveTransformerTestHelper {

    /**
     * Tests an activity diagram consisting of two subsequent activities.
     * See activity_diagrams/activity.
     */
    @Test
    void testActivity() throws IOException {
        ActivityDiagramBuilder builder = new ActivityDiagramBuilder();
        InitialNode initNode = new InitialNode("initial");
        OpaqueAction action1 = new OpaqueAction("Action1", Collections.emptyList());
        OpaqueAction action2 = new OpaqueAction("Action2", Collections.emptyList());
        ActivityFinalNode finalNode = new ActivityFinalNode("final");

        ActivityDiagram activityDiagram = builder.setName("Activity")
                .setInitialNode(initNode)
                .createControlFlow("", initNode, action1)
                .createControlFlow("", action1, action2)
                .createControlFlow("", action2, finalNode)
                .build();

        this.checkGrooveGeneration(activityDiagram, false, x -> x.equals("type.gty"));
    }

    /**
     * Tests an activity diagram with a decision and merge node.
     * See activity_diagrams/decision.
     */
    @Test
    void testDecision() throws IOException {
        ActivityDiagramBuilder builder = new ActivityDiagramBuilder();
        InitialNode initNode = new InitialNode("initial");
        OpaqueAction action1 = new OpaqueAction("Action1", Collections.emptyList());

        DecisionNode decisionNode = new DecisionNode("decision");
        OpaqueAction action1_1 = new OpaqueAction("Action1_1", Collections.emptyList());
        OpaqueAction action1_2 = new OpaqueAction("Action1_2", Collections.emptyList());
        MergeNode mergeNode = new MergeNode("merge");

        OpaqueAction action2 = new OpaqueAction("Action2", Collections.emptyList());
        ActivityFinalNode finalNode = new ActivityFinalNode("final");

        ActivityDiagram activityDiagram = builder.setName("Decision")
                .setInitialNode(initNode)
                .createControlFlow("", initNode, action1)
                .createControlFlow("", action1, decisionNode)
                .createControlFlow("", decisionNode, action1_1)
                .createControlFlow("", decisionNode, action1_2)
                .createControlFlow("", action1_1, mergeNode)
                .createControlFlow("", action1_2, mergeNode)
                .createControlFlow("", mergeNode, action2)
                .createControlFlow("", action2, finalNode)
                .build();

        this.checkGrooveGeneration(activityDiagram, false, x -> x.equals("type.gty"));
    }

    /**
     * Tests an activity diagram with a fork and join node.
     * See activity_diagrams/fork.
     */
    @Test
    void testFork() throws IOException {
        ActivityDiagramBuilder builder = new ActivityDiagramBuilder();
        InitialNode initNode = new InitialNode("initial");
        OpaqueAction action1 = new OpaqueAction("Action1", Collections.emptyList());

        ForkNode forkNode = new ForkNode("fork");
        OpaqueAction action1_1 = new OpaqueAction("Action1_1", Collections.emptyList());
        OpaqueAction action1_2 = new OpaqueAction("Action1_2", Collections.emptyList());
        JoinNode joinNode = new JoinNode("join");

        OpaqueAction action2 = new OpaqueAction("Action2", Collections.emptyList());
        ActivityFinalNode finalNode = new ActivityFinalNode("final");

        ActivityDiagram activityDiagram = builder.setName("Fork")
                .setInitialNode(initNode)
                .createControlFlow("", initNode, action1)
                .createControlFlow("", action1, forkNode)
                .createControlFlow("", forkNode, action1_1)
                .createControlFlow("", forkNode, action1_2)
                .createControlFlow("", action1_1, joinNode)
                .createControlFlow("", action1_2, joinNode)
                .createControlFlow("", joinNode, action2)
                .createControlFlow("", action2, finalNode)
                .build();

        this.checkGrooveGeneration(activityDiagram, false, x -> x.equals("type.gty"));
    }

    /**
     * Tests an activity diagram which sets variables.
     * See activity_diagrams/vars.
     */
    @Test
    void testVariables() throws IOException {
        ActivityDiagramBuilder builder = new ActivityDiagramBuilder();
        InitialNode initNode = new InitialNode("initial");
        IntegerVariable x = new IntegerVariable("x", 0);
        IntegerVariable y = new IntegerVariable("y", 0);
        SetVariableExpression<IntegerValue> setX = new SetVariableExpression<>(new IntegerValue(5), x);
        SetVariableExpression<IntegerValue> setY = new SetVariableExpression<>(new IntegerValue(37), y);
        IntegerVariable sumVar = new IntegerVariable("sum", 0);
        IntegerCalculationExpression sum = new IntegerCalculationExpression(
                x,
                y,
                sumVar,
                IntegerCalculationOperator.ADD);
        OpaqueAction action1 = new OpaqueAction("Action1", Lists.newArrayList(setX, setY));
        OpaqueAction action2 = new OpaqueAction("Action2", Lists.newArrayList(sum));
        ActivityFinalNode finalNode = new ActivityFinalNode("final");

        ActivityDiagram activityDiagram = builder.setName("Vars")
                .setInitialNode(initNode)
                .createControlFlow("", initNode, action1)
                .createControlFlow("", action1, action2)
                .createControlFlow("", action2, finalNode)
                .addLocalVariable(x)
                .addLocalVariable(y)
                .addLocalVariable(sumVar)
                .build();

        this.checkGrooveGeneration(activityDiagram, false, fileName -> fileName.equals("type.gty"));
    }

    /**
     * Tests an activity diagram with one activity which has a lot of different expressions.
     */
    @Test
    void testExpression() throws IOException {
        ActivityDiagramBuilder builder = new ActivityDiagramBuilder();
        InitialNode initNode = new InitialNode("initial");

        IntegerVariable x = new IntegerVariable("x", 1);
        IntegerVariable y = new IntegerVariable("y", 2);
        IntegerVariable diffVar = new IntegerVariable("diff", 0);
        BooleanVariable xEqualsX = new BooleanVariable("x equals x", false);
        BooleanVariable xSeqX = new BooleanVariable("x smaller equals x", false);
        BooleanVariable xSmallerX = new BooleanVariable("x smaller x", false);
        BooleanVariable xGeqX = new BooleanVariable("x greater equals x", false);
        BooleanVariable xGreaterX = new BooleanVariable("x greater x", false);

        BooleanVariable a = new BooleanVariable("A", true);
        BooleanVariable notA = new BooleanVariable("Not A", true);
        BooleanVariable b = new BooleanVariable("B", false);
        BooleanVariable aAndB = new BooleanVariable("A and B", true);
        BooleanVariable aOrB = new BooleanVariable("A or B", true);

        IntegerCalculationExpression diff = new IntegerCalculationExpression(
                x,
                y,
                diffVar,
                IntegerCalculationOperator.SUBTRACT);
        IntegerComparisonExpression x_equals_x = new IntegerComparisonExpression(x, x, xEqualsX, IntegerComparisonOperator.EQUALS);
        IntegerComparisonExpression x_seq_x = new IntegerComparisonExpression(x, x, xSeqX, IntegerComparisonOperator.SMALLER_EQUALS);
        IntegerComparisonExpression x_smaller_x = new IntegerComparisonExpression(x, x, xSmallerX, IntegerComparisonOperator.SMALLER);
        IntegerComparisonExpression x_geq_x = new IntegerComparisonExpression(x, x, xGeqX, IntegerComparisonOperator.GREATER_EQUALS);
        IntegerComparisonExpression x_greater_x = new IntegerComparisonExpression(x, x, xGreaterX, IntegerComparisonOperator.GREATER);
        BooleanUnaryExpression notAExp = new BooleanUnaryExpression(a, notA, BooleanUnaryOperator.NOT);
        BooleanBinaryExpression aAndBExp = new BooleanBinaryExpression(a, b, BooleanBinaryOperator.AND, aAndB);
        BooleanBinaryExpression aOrBxp = new BooleanBinaryExpression(a, b, BooleanBinaryOperator.OR, aOrB);

        OpaqueAction action1 = new OpaqueAction(
                "Action1",
                Lists.newArrayList(x_equals_x, diff, notAExp, aAndBExp, aOrBxp, x_seq_x, x_smaller_x, x_geq_x, x_greater_x));
        ActivityFinalNode finalNode = new ActivityFinalNode("final");

        ActivityDiagram activityDiagram = builder.setName("Exps")
                .setInitialNode(initNode)
                .createControlFlow("", initNode, action1)
                .createControlFlow("", action1, finalNode)
                .addLocalVariable(x)
                .addLocalVariable(y)
                .addLocalVariable(a)
                .addLocalVariable(notA)
                .addLocalVariable(b)
                .addLocalVariable(aAndB)
                .addLocalVariable(xEqualsX)
                .addLocalVariable(xGeqX)
                .addLocalVariable(xGreaterX)
                .addLocalVariable(xSmallerX)
                .addLocalVariable(xSeqX)
                .addLocalVariable(aOrB)
                .addLocalVariable(diffVar)
                .build();

        this.checkGrooveGeneration(activityDiagram, false, fileName -> fileName.equals("type.gty"));
    }

    /**
     * Tests the semantics using the example from the TTC 2015.
     * See activity_diagrams/ttc_workflow.
     */
    @Test
    void testTTCExample() throws IOException {
        BooleanVariable not_internal = new BooleanVariable("not internal", false);
        BooleanVariable internal = new BooleanVariable("internal", false);
        BooleanUnaryExpression notInternalExp = new BooleanUnaryExpression(
                not_internal,
                internal,
                BooleanUnaryOperator.NOT);

        ActivityDiagramBuilder builder = new ActivityDiagramBuilder();
        InitialNode initNode = new InitialNode("initial");
        OpaqueAction register = new OpaqueAction("register", Lists.newArrayList(notInternalExp));
        DecisionNode decisionNode = new DecisionNode("decision");

        OpaqueAction get_welcome_package = new OpaqueAction("get_welcome_package", Lists.newArrayList());
        OpaqueAction assign_to_project_external = new OpaqueAction("assign_to_project_external", Lists.newArrayList());
        ForkNode forkNode = new ForkNode("fork");

        OpaqueAction add_to_website = new OpaqueAction("add_to_website", Lists.newArrayList());
        OpaqueAction assign_to_project = new OpaqueAction("assign_to_project", Lists.newArrayList());

        JoinNode joinNode = new JoinNode("join");
        OpaqueAction manager_interview = new OpaqueAction("manager_interview", Lists.newArrayList());
        OpaqueAction manager_report = new OpaqueAction("manager_report", Lists.newArrayList());

        MergeNode mergeNode = new MergeNode("merge");
        OpaqueAction authorize_payment = new OpaqueAction("authorize_payment", Lists.newArrayList());
        ActivityFinalNode finalNode = new ActivityFinalNode("final");

        ActivityDiagram ttc_workflow = builder.setInitialNode(initNode)
                .setName("TTC_Workflow")
                .createControlFlow("", initNode, register)
                .createControlFlow("", register, decisionNode)
                .createControlFlowWithGuard("", decisionNode, assign_to_project_external, not_internal)
                .createControlFlow("", assign_to_project_external, mergeNode)
                .createControlFlowWithGuard("", decisionNode, get_welcome_package, internal)
                .createControlFlow("", get_welcome_package, forkNode)
                .createControlFlow("", forkNode, add_to_website)
                .createControlFlow("", forkNode, assign_to_project)
                .createControlFlow("", add_to_website, joinNode)
                .createControlFlow("", assign_to_project, joinNode)
                .createControlFlow("", joinNode, manager_interview)
                .createControlFlow("", manager_interview, manager_report)
                .createControlFlow("", manager_report, mergeNode)
                .createControlFlow("", mergeNode, authorize_payment)
                .createControlFlow("", authorize_payment, finalNode)
                .addLocalVariable(not_internal)
                .addInputVariable(internal)
                .build();

        this.checkGrooveGeneration(ttc_workflow);
    }

//    @Test
    void perf1() throws IOException {
        ActivityDiagramBuilder builder = new ActivityDiagramBuilder();
        InitialNode initNode = new InitialNode("initial");

        builder.setName("perf1")
                .setInitialNode(initNode);

        ActivityNode previous = initNode;
        for (int i = 0; i < 1000; i++) {
            OpaqueAction action = new OpaqueAction("activity" + i, Lists.newArrayList());
            builder.createControlFlow("", previous, action);
            previous = action;
        }

        ActivityFinalNode finalNode = new ActivityFinalNode("final");
        builder.createControlFlow("", previous, finalNode);

        this.checkGrooveGeneration(builder.build());
    }
}