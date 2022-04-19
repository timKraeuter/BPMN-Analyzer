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

public class UseCase extends BPMNToGrooveTestBase {


    /**
     * See test case <a href="https://cawemo.com/share/3da1f1ce-a0ca-4fa5-8caf-8bd04b879879">"Use Case Test"</a> in cawemo.
     */
    @Test
    void testUseCase() throws IOException {
        EventDefinition b_is_green = new EventDefinition("B_is_green");
        EventDefinition a_c_are_green = new EventDefinition("A_C_are_green");

        // Phase 1
        StartEvent phase1_start = new StartEvent("Phase1_start");
        EventBasedGateway evp1 = new EventBasedGateway("evp1");
        IntermediateCatchEvent tl_status_requested1 = new IntermediateCatchEvent("TL_status_requested1", IntermediateCatchEventType.MESSAGE);
        IntermediateThrowEvent a_c_green_t = new IntermediateThrowEvent("A_C_green_t", IntermediateThrowEventType.MESSAGE);
        IntermediateCatchEvent b_green_requested = new IntermediateCatchEvent("B_green_requested", IntermediateCatchEventType.MESSAGE);
        EndEvent phase1_end = new EndEvent("Phase1_end");

        String phase_1 = "Phase_1";
        Process phase1_process = new BPMNCollaborationBuilder()
                .name(phase_1)
                .processName(phase_1)
                .startEvent(phase1_start)
                .sequenceFlow(phase1_start, evp1)
                .sequenceFlow(evp1, tl_status_requested1)
                .sequenceFlow(tl_status_requested1, a_c_green_t)
                .sequenceFlow(a_c_green_t, evp1)
                .sequenceFlow(evp1, b_green_requested)
                .sequenceFlow(b_green_requested, phase1_end)
                .build()
                .getParticipants().iterator().next();

        // Phase 2
        StartEvent phase2_start = new StartEvent("Phase2_start");
        EventBasedGateway evp2 = new EventBasedGateway("evp2");
        IntermediateCatchEvent tl_status_requested2 = new IntermediateCatchEvent("TL_status_requested2", IntermediateCatchEventType.MESSAGE);
        IntermediateThrowEvent b_green_t = new IntermediateThrowEvent("B_green_t", IntermediateThrowEventType.MESSAGE);
        IntermediateCatchEvent a_c_green_requested = new IntermediateCatchEvent("A_C_green_requested", IntermediateCatchEventType.MESSAGE);
        EndEvent phase2_end = new EndEvent("Phase2_end");

        String phase_2 = "Phase_2";
        Process phase2_Process = new BPMNCollaborationBuilder()
                .name(phase_2)
                .processName(phase_2)
                .startEvent(phase2_start)
                .sequenceFlow(phase2_start, evp2)
                .sequenceFlow(evp2, tl_status_requested2)
                .sequenceFlow(tl_status_requested2, b_green_t)
                .sequenceFlow(b_green_t, evp2)
                .sequenceFlow(evp2, a_c_green_requested)
                .sequenceFlow(a_c_green_requested, phase2_end)
                .build()
                .getParticipants().iterator().next();

        // Junction-Controller
        final StartEvent controller_started = new StartEvent("controller_started");
        ExclusiveGateway e1 = new ExclusiveGateway("e1");

        CallActivity phase1 = new CallActivity(phase1_process);
        BoundaryEvent phase_1_over = new BoundaryEvent("Phase_1_over", BoundaryEventType.TIMER, true);
        phase1.attachBoundaryEvent(phase_1_over);
        Task switch_to_p2 = new Task("Switch_to_P2");
        IntermediateThrowEvent b_green_signal = new IntermediateThrowEvent("B_is_green_t", IntermediateThrowEventType.SIGNAL, b_is_green);

        CallActivity phase2 = new CallActivity(phase2_Process);
        BoundaryEvent phase_2_over = new BoundaryEvent("Phase_2_over", BoundaryEventType.TIMER, true);
        phase2.attachBoundaryEvent(phase_2_over);
        Task switch_to_p1 = new Task("Switch_to_P1");
        IntermediateThrowEvent ac_green_signal = new IntermediateThrowEvent("A_C_are_green_t", IntermediateThrowEventType.SIGNAL, a_c_are_green);

        ExclusiveGateway e2 = new ExclusiveGateway("e2");
        final EndEvent controller_stopped = new EndEvent("controller_stopped");

        // Bus controller (B)
        StartEvent approaching_junction_B = new StartEvent("Approaching_Junction_B");
        SendTask request_tl_status_B = new SendTask("Request_TL_status_B");
        EventBasedGateway ev1_B = new EventBasedGateway("ev1_B");
        IntermediateCatchEvent b_is_red_r = new IntermediateCatchEvent("B_is_red", IntermediateCatchEventType.MESSAGE);
        SendTask request_green_tl_B = new SendTask("Request_green_tl_B");
        IntermediateCatchEvent b_is_green_signal = new IntermediateCatchEvent("B_is_green_signal", IntermediateCatchEventType.SIGNAL, b_is_green);
        ExclusiveGateway e3_B = new ExclusiveGateway("e3_B");
        Task pass_junction_B = new Task("Pass_Junction_B");
        IntermediateCatchEvent b_is_green_r = new IntermediateCatchEvent("B_is_green_r", IntermediateCatchEventType.MESSAGE);
        EndEvent passed_junction_B = new EndEvent("Passed_junction_B");

        // Bus controller (A)
        StartEvent approaching_junction_A = new StartEvent("Approaching_Junction_A");
        SendTask request_tl_status_A = new SendTask("Request_TL_status_A");
        EventBasedGateway ev1_A = new EventBasedGateway("ev1_A");
        IntermediateCatchEvent a_is_red_r = new IntermediateCatchEvent("A_is_red", IntermediateCatchEventType.MESSAGE);
        SendTask request_green_tl_A = new SendTask("Request_green_tl_A");
        IntermediateCatchEvent a_is_green_signal = new IntermediateCatchEvent("A_is_green_signal", IntermediateCatchEventType.SIGNAL, b_is_green);
        ExclusiveGateway e3_A = new ExclusiveGateway("e3_A");
        Task pass_junction_A = new Task("Pass_Junction_A");
        IntermediateCatchEvent a_is_green_r = new IntermediateCatchEvent("A_is_green_r", IntermediateCatchEventType.MESSAGE);
        EndEvent passed_junction_A = new EndEvent("Passed_junction_A");


        final String modelName = "ecmfa_usecase";
        final BPMNCollaboration collaboration = new BPMNCollaborationBuilder()
                .name(modelName)
                .messageFlow(request_tl_status_B, tl_status_requested1)
                .messageFlow(request_tl_status_B, tl_status_requested2)
                .messageFlow(b_green_t, b_is_green_r)
                .messageFlow(a_c_green_t, b_is_red_r)
                .messageFlow(request_green_tl_B, b_green_requested)
                .messageFlow(request_tl_status_A, tl_status_requested1)
                .messageFlow(request_tl_status_A, tl_status_requested2)
                .messageFlow(b_green_t, a_is_red_r)
                .messageFlow(a_c_green_t, a_is_green_r)
                .messageFlow(request_green_tl_A, a_c_green_requested)
                .processName("T-Junction Controller")
                .startEvent(controller_started)
                .sequenceFlow(controller_started, e1)
                .sequenceFlow(e1, phase1)
                .sequenceFlow(phase1, switch_to_p2)
                .sequenceFlow(phase_1_over, switch_to_p2)
                .sequenceFlow(switch_to_p2, b_green_signal)
                .sequenceFlow(b_green_signal, phase2)
                .sequenceFlow(phase2, switch_to_p1)
                .sequenceFlow(phase_2_over, switch_to_p1)
                .sequenceFlow(switch_to_p1, ac_green_signal)
                .sequenceFlow(ac_green_signal, e2)
                .sequenceFlow(e2, e1)
                .sequenceFlow("stop", e2, controller_stopped)
                .buildProcess()
                .processName("Bus controller (B)")
                .startEvent(approaching_junction_B)
                .sequenceFlow(approaching_junction_B, request_tl_status_B)
                .sequenceFlow(request_tl_status_B, ev1_B)
                .sequenceFlow(ev1_B, b_is_red_r)
                .sequenceFlow(ev1_B, b_is_green_r)
                .sequenceFlow(b_is_red_r, request_green_tl_B)
                .sequenceFlow(request_green_tl_B, b_is_green_signal)
                .sequenceFlow(b_is_green_signal, e3_B)
                .sequenceFlow(e3_B, pass_junction_B)
                .sequenceFlow(pass_junction_B, passed_junction_B)
                .sequenceFlow(b_is_green_r, e3_B)
                .buildProcess()
                .processName("Bus controller (A)")
                .startEvent(approaching_junction_A)
                .sequenceFlow(approaching_junction_A, request_tl_status_A)
                .sequenceFlow(request_tl_status_A, ev1_A)
                .sequenceFlow(ev1_A, a_is_red_r)
                .sequenceFlow(ev1_A, a_is_green_r)
                .sequenceFlow(a_is_red_r, request_green_tl_A)
                .sequenceFlow(request_green_tl_A, a_is_green_signal)
                .sequenceFlow(a_is_green_signal, e3_A)
                .sequenceFlow(e3_A, pass_junction_A)
                .sequenceFlow(pass_junction_A, passed_junction_A)
                .sequenceFlow(a_is_green_r, e3_A)
                .build();

        this.checkGrooveGeneration(collaboration);
    }
}
