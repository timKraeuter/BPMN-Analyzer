package groove.behaviorTransformer;

import behavior.activity.ActivityDiagram;
import behavior.activity.ActivityDiagramBuilder;
import behavior.activity.expression.SetVariableExpression;
import behavior.activity.expression.integer.IntegerCalculationExpression;
import behavior.activity.expression.integer.IntegerCalculationOperator;
import behavior.activity.nodes.*;
import behavior.activity.values.IntegerValue;
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

        this.checkGrooveGeneration(activityDiagram);
    }

    /**
     * Tests an activity diagram with a decision and merge node.
     * See activity_diagrams/decision.
     */
    @Test
    void testAlternative() throws IOException {
        ActivityDiagramBuilder builder = new ActivityDiagramBuilder();
        InitialNode initNode = new InitialNode("initial");
        OpaqueAction action1 = new OpaqueAction("Action1", Collections.emptyList());

        DecisionNode decisionNode = new DecisionNode("decision");
        OpaqueAction action1_1 = new OpaqueAction("Action1.1", Collections.emptyList());
        OpaqueAction action1_2 = new OpaqueAction("Action1.2", Collections.emptyList());
        MergeNode mergeNode = new MergeNode("merge");

        OpaqueAction action2 = new OpaqueAction("Action2", Collections.emptyList());
        ActivityFinalNode finalNode = new ActivityFinalNode("final");

        ActivityDiagram activityDiagram = builder.setName("Decision")
                                                 .setInitialNode(initNode)
                                                 .createControlFlow("", initNode, decisionNode)
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

        ForkNode forkNode = new ForkNode("decision");
        OpaqueAction action1_1 = new OpaqueAction("Action1.1", Collections.emptyList());
        OpaqueAction action1_2 = new OpaqueAction("Action1.2", Collections.emptyList());
        JoinNode joinNode = new JoinNode("merge");

        OpaqueAction action2 = new OpaqueAction("Action2", Collections.emptyList());
        ActivityFinalNode finalNode = new ActivityFinalNode("final");

        ActivityDiagram activityDiagram = builder.setName("Fork")
                                                 .setInitialNode(initNode)
                                                 .createControlFlow("", initNode, forkNode)
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
        IntegerVariable y = new IntegerVariable("x", 0);
        SetVariableExpression<IntegerValue> setX = new SetVariableExpression<>(new IntegerValue(5), x);
        SetVariableExpression<IntegerValue> setY = new SetVariableExpression<>(new IntegerValue(5), y);
        IntegerCalculationExpression sum = new IntegerCalculationExpression(
                x,
                y,
                new IntegerVariable("sum", 0),
                IntegerCalculationOperator.ADD);
        OpaqueAction action1 = new OpaqueAction("Action1", Lists.newArrayList(setX, setY));
        OpaqueAction action2 = new OpaqueAction("Action2", Lists.newArrayList(sum));
        ActivityFinalNode finalNode = new ActivityFinalNode("final");

        ActivityDiagram activityDiagram = builder.setName("Vars")
                                                 .setInitialNode(initNode)
                                                 .createControlFlow("", initNode, action1)
                                                 .createControlFlow("", action1, action2)
                                                 .createControlFlow("", action2, finalNode)
                                                 .build();

        this.checkGrooveGeneration(activityDiagram);
    }

    /**
     * TODO: add picture and describe.
     */
    @Test
    void testTTCExample() throws IOException {
    }
}