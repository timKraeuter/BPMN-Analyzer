package ecmfa;

import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.Process;
import behavior.bpmn.activities.CallActivity;
import behavior.bpmn.activities.tasks.SendTask;
import behavior.bpmn.activities.tasks.Task;
import behavior.bpmn.auxiliary.BPMNCollaborationBuilder;
import behavior.bpmn.events.*;
import behavior.bpmn.gateways.EventBasedGateway;
import behavior.bpmn.gateways.ExclusiveGateway;
import groove.behaviorTransformer.bpmn.BPMNToGrooveTestBase;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class Usecase extends BPMNToGrooveTestBase {


    /**
     * See test case <a href="https://cawemo.com/share/3da1f1ce-a0ca-4fa5-8caf-8bd04b879879">"Use Case Test"</a> in cawemo.
     */
    @Test
    void testUseCase() throws IOException {
        EventDefinition p1 = new EventDefinition("p1");
        EventDefinition p2 = new EventDefinition("p2");

        // Phase 1
        StartEvent phase1_start = new StartEvent("Phase1_start");
        EventBasedGateway evp1 = new EventBasedGateway("evp1");
        IntermediateCatchEvent tl_status_requested1 = new IntermediateCatchEvent("TL_status_requested1", IntermediateCatchEventType.MESSAGE);
        IntermediateThrowEvent phase1_t = new IntermediateThrowEvent("Phase1_t", IntermediateThrowEventType.MESSAGE);
        IntermediateCatchEvent p1_requested = new IntermediateCatchEvent("P1_requested", IntermediateCatchEventType.MESSAGE);
        EndEvent phase1_end = new EndEvent("Phase1_end");

        String phase_1 = "Phase_1";
        Process phase1_process = new BPMNCollaborationBuilder()
                .name(phase_1)
                .processName(phase_1)
                .startEvent(phase1_start)
                .sequenceFlow(phase1_start, evp1)
                .sequenceFlow(evp1, tl_status_requested1)
                .sequenceFlow(tl_status_requested1, phase1_t)
                .sequenceFlow(phase1_t, evp1)
                .sequenceFlow(evp1, p1_requested)
                .sequenceFlow(p1_requested, phase1_end)
                .build()
                .getParticipants().iterator().next();

        // Phase 2
        StartEvent phase2_start = new StartEvent("Phase2_start");
        EventBasedGateway evp2 = new EventBasedGateway("evp2");
        IntermediateCatchEvent tl_status_requested2 = new IntermediateCatchEvent("TL_status_requested2", IntermediateCatchEventType.MESSAGE);
        IntermediateThrowEvent phase2_t = new IntermediateThrowEvent("Phase2_t", IntermediateThrowEventType.MESSAGE);
        IntermediateCatchEvent p2_requested = new IntermediateCatchEvent("P2_requested", IntermediateCatchEventType.MESSAGE);
        EndEvent phase2_end = new EndEvent("Phase2_end");

        String phase_2 = "Phase_2";
        Process phase2_Process = new BPMNCollaborationBuilder()
                .name(phase_2)
                .processName(phase_2)
                .startEvent(phase2_start)
                .sequenceFlow(phase2_start, evp2)
                .sequenceFlow(evp2, tl_status_requested2)
                .sequenceFlow(tl_status_requested2, phase2_t)
                .sequenceFlow(phase2_t, evp2)
                .sequenceFlow(evp2, p2_requested)
                .sequenceFlow(p2_requested, phase2_end)
                .build()
                .getParticipants().iterator().next();

        // Junction-Controller
        final StartEvent start_c = new StartEvent("start_c");
        ExclusiveGateway e1 = new ExclusiveGateway("e1");
        CallActivity phase2 = new CallActivity(phase2_Process);
        Task switch_to_p1 = new Task("Switch_to_P1");
        IntermediateThrowEvent p1_signal = new IntermediateThrowEvent("P1_signal_t", IntermediateThrowEventType.SIGNAL, p1);
        IntermediateThrowEvent p2_signal = new IntermediateThrowEvent("P2_signal_t", IntermediateThrowEventType.SIGNAL, p2);
        Task switch_to_p2 = new Task("Switch_to_P2");
        CallActivity phase1 = new CallActivity(phase1_process);
        ExclusiveGateway e2 = new ExclusiveGateway("e2");
        final EndEvent end_c = new EndEvent("end_c");

        // Bus controller (P2)
        StartEvent approaching_junction = new StartEvent("Approaching_Junction");
        SendTask request_tl_status = new SendTask("Request_TL_status");
        EventBasedGateway ev1 = new EventBasedGateway("ev1");
        IntermediateCatchEvent phase1_message = new IntermediateCatchEvent("Phase_1", IntermediateCatchEventType.MESSAGE);
        SendTask request_p2 = new SendTask("Request_P2");
        IntermediateCatchEvent p2_signal_r = new IntermediateCatchEvent("P2_signal_r", IntermediateCatchEventType.SIGNAL, p2);
        ExclusiveGateway e3 = new ExclusiveGateway("e3");
        Task pass_junction = new Task("Pass_Junction");
        IntermediateCatchEvent phase2_message = new IntermediateCatchEvent("Phase_2", IntermediateCatchEventType.MESSAGE);
        EndEvent passed_junction = new EndEvent("Passed_junction");


        final String modelName = "ecmfa_usecase";
        final BPMNCollaboration collaboration = new BPMNCollaborationBuilder()
                .name(modelName)
                .messageFlow(request_tl_status, tl_status_requested1)
                .messageFlow(request_tl_status, tl_status_requested2)
                .messageFlow(phase2_t, phase2_message)
                .messageFlow(phase1_t, phase1_message)
                .messageFlow(request_p2, p2_requested)
                .processName("Junction_Controller")
                .startEvent(start_c)
                .sequenceFlow(start_c, e1)
                .sequenceFlow(e1, phase2)
                .sequenceFlow(phase2, switch_to_p1)
                .sequenceFlow(switch_to_p1, p1_signal)
                .sequenceFlow(p1_signal, phase1)
                .sequenceFlow(phase1, switch_to_p2)
                .sequenceFlow(switch_to_p2, p2_signal)
                .sequenceFlow(p2_signal, e2)
                .sequenceFlow(e2, e1)
                .sequenceFlow("stop", e2, end_c)
                .buildProcess()
                .processName("Bus controller")
                .startEvent(approaching_junction)
                .sequenceFlow(approaching_junction, request_tl_status)
                .sequenceFlow(request_tl_status, ev1)
                .sequenceFlow(ev1, phase1_message)
                .sequenceFlow(ev1, phase2_message)
                .sequenceFlow(phase1_message, request_p2)
                .sequenceFlow(request_p2, p2_signal_r)
                .sequenceFlow(p2_signal_r, e3)
                .sequenceFlow(e3, pass_junction)
                .sequenceFlow(pass_junction, passed_junction)
                .sequenceFlow(phase2_message, e3)
                .build();

        this.checkGrooveGeneration(collaboration);
    }
}
