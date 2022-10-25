package behavior.bpmn.auxiliary.visitors;


import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.BPMNProcess;
import behavior.bpmn.activities.Activity;
import behavior.bpmn.activities.CallActivity;
import behavior.bpmn.activities.tasks.ReceiveTask;
import behavior.bpmn.activities.tasks.SendTask;
import behavior.bpmn.activities.tasks.Task;
import behavior.bpmn.events.*;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Set;

/**
 * FlowNodeVisitor used to collect all signal catch events with a certain event definition.
 */
public class SignalCatchEventCollectorFlowNodeVisitor extends DoNothingFlowNodeVisitor {
    private final BPMNCollaboration collaboration;
    private final EventDefinition eventDefinition;
    private final Set<Event> signalCatchEvents;
    private final Set<BoundaryEvent> signalBoundaryCatchEvents;
    private final Set<BPMNProcess> seenProcesses;

    public SignalCatchEventCollectorFlowNodeVisitor(BPMNCollaboration collaboration,
                                                    EventDefinition eventDefinition,
                                                    Set<Event> signalCatchEvents,
                                                    Set<BoundaryEvent> signalBoundaryCatchEvents,
                                                    Set<BPMNProcess> seenProcesses) {
        this.collaboration = collaboration;
        this.eventDefinition = eventDefinition;
        this.signalCatchEvents = signalCatchEvents;
        this.signalBoundaryCatchEvents = signalBoundaryCatchEvents;
        this.seenProcesses = seenProcesses;
    }

    @Override
    public void handle(Task task) {
        addPotentialBoundaryEvents(task);
    }

    private void addPotentialBoundaryEvents(Activity task) {
        task.getBoundaryEvents().stream().filter(boundaryEvent -> boundaryEvent.getType() ==
                                                                  BoundaryEventType.SIGNAL).forEach(
                signalBoundaryCatchEvents::add);
    }

    @Override
    public void handle(SendTask task) {
        addPotentialBoundaryEvents(task);
    }

    @Override
    public void handle(ReceiveTask task) {
        addPotentialBoundaryEvents(task);

    }

    @Override
    public void handle(CallActivity callActivity) {
        Pair<Set<Event>, Set<BoundaryEvent>> signalAndBoundarySignalEvents =
                collaboration.findAllCorrespondingSignalCatchEvents(
                callActivity.getSubProcessModel(),
                eventDefinition,
                seenProcesses);
        signalCatchEvents.addAll(signalAndBoundarySignalEvents.getLeft());
        signalBoundaryCatchEvents.addAll(signalAndBoundarySignalEvents.getRight());

        addPotentialBoundaryEvents(callActivity);
    }

    @Override
    public void handle(StartEvent startEvent) {
        if ((startEvent.getType() == StartEventType.SIGNAL ||
             startEvent.getType() == StartEventType.SIGNAL_NON_INTERRUPTING) &&
            startEvent.getEventDefinition().getGlobalSignalName().equals(eventDefinition.getGlobalSignalName())) {
            signalCatchEvents.add(startEvent);
        }
    }

    @Override
    public void handle(IntermediateCatchEvent intermediateCatchEvent) {
        if (intermediateCatchEvent.getType() == IntermediateCatchEventType.SIGNAL &&
            intermediateCatchEvent.getEventDefinition().getGlobalSignalName().equals(eventDefinition.getGlobalSignalName())) {
            signalCatchEvents.add(intermediateCatchEvent);
        }
    }
}
