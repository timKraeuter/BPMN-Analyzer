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

class ActivityDiagramToGrooveTransformerTest extends BehaviorToGrooveTransformerTestHelper {

    private static final String TYPE_GRAPH_FILE_NAME = "type.gty";

    @Override
    protected void setUpFurther() {
        // Default is to ignore the type graph.
        this.setFileNameFilter(x -> x.equals(TYPE_GRAPH_FILE_NAME));
    }

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

        this.setFileNameFilter(x -> false); // Expect copied type graph
        this.checkGrooveGeneration(activityDiagram);
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

        this.checkGrooveGeneration(activityDiagram);
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

        this.checkGrooveGeneration(activityDiagram);
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

        this.checkGrooveGeneration(activityDiagram);
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

        this.checkGrooveGeneration(activityDiagram);
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
                .createControlFlow("", decisionNode, assign_to_project_external, not_internal)
                .createControlFlow("", assign_to_project_external, mergeNode)
                .createControlFlow("", decisionNode, get_welcome_package, internal)
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
        // Around 5000ms
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

    //    @Test
    void perf3_2() throws IOException {
        // Around 630 ms
        IntegerVariable i_var = new IntegerVariable("i", 1);

        IntegerVariable value2 = new IntegerVariable("value2", 2);
        BooleanVariable iG2 = new BooleanVariable("iG2", false);
        BooleanVariable iE2 = new BooleanVariable("iE2", false);
        BooleanVariable iL2 = new BooleanVariable("iL2", false);
        IntegerVariable loop = new IntegerVariable("loop", 0);
        IntegerVariable iterations = new IntegerVariable("iterations", 141);
        BooleanVariable loopEiterations = new BooleanVariable("loopEiterations", false);
        BooleanVariable loopLiterations = new BooleanVariable("loopLiterations", false);
        IntegerVariable value1 = new IntegerVariable("value1", 1);

        ActivityDiagramBuilder builder = new ActivityDiagramBuilder();
        InitialNode initNode = new InitialNode("initial");
        OpaqueAction a = new OpaqueAction("a", Lists.newArrayList(
                new IntegerComparisonExpression(i_var, value2, iG2, IntegerComparisonOperator.GREATER),
                new IntegerComparisonExpression(i_var, value2, iE2, IntegerComparisonOperator.EQUALS),
                new IntegerComparisonExpression(i_var, value2, iL2, IntegerComparisonOperator.SMALLER)
        ));
        OpaqueAction b = new OpaqueAction("b", Lists.newArrayList());
        OpaqueAction c = new OpaqueAction("c", Lists.newArrayList());
        OpaqueAction d = new OpaqueAction("d", Lists.newArrayList());
        MergeNode mergeE = new MergeNode("mergeE");
        OpaqueAction e = new OpaqueAction("e", Lists.newArrayList());
        DecisionNode decisionI = new DecisionNode("decisionI");
        OpaqueAction f = new OpaqueAction("f", Lists.newArrayList());
        OpaqueAction g = new OpaqueAction("g", Lists.newArrayList());
        OpaqueAction h = new OpaqueAction("h", Lists.newArrayList());
        OpaqueAction i = new OpaqueAction("i", Lists.newArrayList());
        OpaqueAction j = new OpaqueAction("j", Lists.newArrayList(
                new IntegerComparisonExpression(loop, iterations, loopEiterations, IntegerComparisonOperator.EQUALS),
                new IntegerComparisonExpression(loop, iterations, loopLiterations, IntegerComparisonOperator.SMALLER),
                new IntegerComparisonExpression(loop, iterations, loopEiterations, IntegerComparisonOperator.EQUALS)
        ));

        DecisionNode decisionLoop = new DecisionNode("decisionLoop");
        MergeNode mergeFinal = new MergeNode("mergeFinal");
        OpaqueAction k = new OpaqueAction("k", Lists.newArrayList());
        OpaqueAction l = new OpaqueAction("l", Lists.newArrayList(
                new IntegerCalculationExpression(loop, value1, loop, IntegerCalculationOperator.ADD)
        ));

        ActivityFinalNode finalNode = new ActivityFinalNode("final");

        builder.setName("perf3-2")
                .setInitialNode(initNode)
                .addInputVariable(i_var)
                .addLocalVariable(value2)
                .addLocalVariable(iG2)
                .addLocalVariable(iE2)
                .addLocalVariable(iL2)
                .addLocalVariable(loop)
                .addLocalVariable(iterations)
                .addLocalVariable(loopEiterations)
                .addLocalVariable(loopLiterations)
                .addLocalVariable(value1)
                .createControlFlow("", initNode, a)
                .createControlFlow("", a, b)
                .createControlFlow("", b, c)
                .createControlFlow("", c, d)
                .createControlFlow("", d, mergeE)
                .createControlFlow("", mergeE, e)
                .createControlFlow("", e, decisionI)
                .createControlFlow("", decisionI, f, iG2)
                .createControlFlow("", decisionI, g, iE2)
                .createControlFlow("", decisionI, i, iL2)
                .createControlFlow("", f, mergeFinal)
                .createControlFlow("", mergeFinal, finalNode)
                .createControlFlow("", g, h)
                .createControlFlow("", h, mergeFinal)
                .createControlFlow("", i, j)
                .createControlFlow("", j, decisionLoop)
                .createControlFlow("", decisionLoop, k, loopEiterations)
                .createControlFlow("", decisionLoop, l, loopLiterations)
                .createControlFlow("", k, mergeFinal)
                .createControlFlow("", l, mergeE);

        this.checkGrooveGeneration(builder.build());
    }

    //    @Test
    void myPerf() throws IOException {
        // 36/37s on hvl laptop before mini
        // 32/33s after min
        IntegerVariable counter = new IntegerVariable("i", 1);
        IntegerVariable value1 = new IntegerVariable("value1", 1);
        IntegerVariable iterations = new IntegerVariable("iterations", 100000);
        BooleanVariable stop = new BooleanVariable("stop", false);
        BooleanVariable notStop = new BooleanVariable("dontStop", true);

        ActivityDiagramBuilder builder = new ActivityDiagramBuilder();
        InitialNode initNode = new InitialNode("initial");
        MergeNode merge = new MergeNode("merge");
        OpaqueAction a = new OpaqueAction("a", Lists.newArrayList(
                new IntegerCalculationExpression(counter, value1, counter, IntegerCalculationOperator.ADD),
                new IntegerComparisonExpression(counter, iterations, stop, IntegerComparisonOperator.EQUALS),
                new BooleanUnaryExpression(stop, notStop, BooleanUnaryOperator.NOT)
        ));
        DecisionNode decision = new DecisionNode("decision");
        ActivityFinalNode finalNode = new ActivityFinalNode("final");

        builder.setName("myPerf")
               .setInitialNode(initNode)
               .addInputVariable(counter)
               .addLocalVariable(value1)
               .addLocalVariable(iterations)
               .addLocalVariable(stop)
               .addLocalVariable(notStop)
               .createControlFlow("", initNode, merge)
               .createControlFlow("", merge, a)
               .createControlFlow("", a, decision)
               .createControlFlow("", decision, merge, notStop)
               .createControlFlow("", decision, finalNode, stop);

        this.checkGrooveGeneration(builder.build());
    }

    @Override
    public String getOutputPathSubFolderName() {
        return "activityDiagram";
    }
}