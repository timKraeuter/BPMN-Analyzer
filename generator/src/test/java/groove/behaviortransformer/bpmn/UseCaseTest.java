package groove.behaviortransformer.bpmn;

import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.BPMNProcess;
import behavior.bpmn.activities.CallActivity;
import behavior.bpmn.activities.tasks.SendTask;
import behavior.bpmn.activities.tasks.Task;
import behavior.bpmn.auxiliary.BPMNCollaborationBuilder;
import behavior.bpmn.events.*;
import behavior.bpmn.gateways.EventBasedGateway;
import behavior.bpmn.gateways.ExclusiveGateway;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

class UseCaseTest extends BPMNToGrooveTestBase {
    private final AtomicInteger counter = new AtomicInteger(0);

    /**
     * See test case <a href="https://cawemo.com/share/3da1f1ce-a0ca-4fa5-8caf-8bd04b879879">"Use Case Test"</a>
     * in cawemo.
     */
    @Test
    void testUseCase() throws IOException {
        EventDefinition b_is_green = new EventDefinition("B_is_green");
        EventDefinition a_c_are_green = new EventDefinition("A_C_are_green");

        // Phase 1
        StartEvent phase1_start = new StartEvent(getId(), "Phase1_start");
        EventBasedGateway evp1 = new EventBasedGateway(getId(), "evp1");
        IntermediateCatchEvent tl_status_requested1 = new IntermediateCatchEvent(getId(),
                                                                                 "TL_status_requested1",
                                                                                 IntermediateCatchEventType.MESSAGE);
        IntermediateThrowEvent a_c_green_t = new IntermediateThrowEvent(getId(),
                                                                        "A_C_green_t",
                                                                        IntermediateThrowEventType.MESSAGE);
        IntermediateCatchEvent b_green_requested = new IntermediateCatchEvent(getId(),
                                                                              "B_green_requested",
                                                                              IntermediateCatchEventType.MESSAGE);
        EndEvent phase1_end = new EndEvent(getId(), "Phase1_end");

        String phase_1 = "Phase_1";
        BPMNProcess phase1_process = new BPMNCollaborationBuilder()
                .name(phase_1)
                .processName(phase_1)
                .startEvent(phase1_start)
                .sequenceFlow(getId(), phase1_start, evp1)
                .sequenceFlow(getId(), evp1, tl_status_requested1)
                .sequenceFlow(getId(), tl_status_requested1, a_c_green_t)
                .sequenceFlow(getId(), a_c_green_t, evp1)
                .sequenceFlow(getId(), evp1, b_green_requested)
                .sequenceFlow(getId(), b_green_requested, phase1_end)
                .build()
                .getParticipants().iterator().next();

        // Phase 2
        StartEvent phase2_start = new StartEvent(getId(), "Phase2_start");
        EventBasedGateway evp2 = new EventBasedGateway(getId(), "evp2");
        IntermediateCatchEvent tl_status_requested2 = new IntermediateCatchEvent(getId(),
                                                                                 "TL_status_requested2",
                                                                                 IntermediateCatchEventType.MESSAGE);
        IntermediateThrowEvent b_green_t = new IntermediateThrowEvent(getId(),
                                                                      "B_green_t",
                                                                      IntermediateThrowEventType.MESSAGE);
        IntermediateCatchEvent a_c_green_requested = new IntermediateCatchEvent(getId(),
                                                                                "A_C_green_requested",
                                                                                IntermediateCatchEventType.MESSAGE);
        EndEvent phase2_end = new EndEvent(getId(), "Phase2_end");

        String phase_2 = "Phase_2";
        BPMNProcess phase2_Process = new BPMNCollaborationBuilder()
                .name(phase_2)
                .processName(phase_2)
                .startEvent(phase2_start)
                .sequenceFlow(getId(), phase2_start, evp2)
                .sequenceFlow(getId(), evp2, tl_status_requested2)
                .sequenceFlow(getId(), tl_status_requested2, b_green_t)
                .sequenceFlow(getId(), b_green_t, evp2)
                .sequenceFlow(getId(), evp2, a_c_green_requested)
                .sequenceFlow(getId(), a_c_green_requested, phase2_end)
                .build()
                .getParticipants().iterator().next();

        // Junction-Controller
        final StartEvent controller_started = new StartEvent(getId(), "controller_started");
        ExclusiveGateway e1 = new ExclusiveGateway(getId(), "e1");

        CallActivity phase1 = new CallActivity(getId(), phase1_process);
        BoundaryEvent phase_1_over = new BoundaryEvent(getId(), "Phase_1_over", BoundaryEventType.TIMER, true);
        phase1.attachBoundaryEvent(phase_1_over);
        Task switch_to_p2 = new Task(getId(), "Switch_to_P2");
        IntermediateThrowEvent b_green_signal = new IntermediateThrowEvent(getId(),
                                                                           "B_is_green_t",
                                                                           IntermediateThrowEventType.SIGNAL,
                                                                           b_is_green);

        CallActivity phase2 = new CallActivity(getId(), phase2_Process);
        BoundaryEvent phase_2_over = new BoundaryEvent(getId(), "Phase_2_over", BoundaryEventType.TIMER, true);
        phase2.attachBoundaryEvent(phase_2_over);
        Task switch_to_p1 = new Task(getId(), "Switch_to_P1");
        IntermediateThrowEvent ac_green_signal = new IntermediateThrowEvent(getId(),
                                                                            "A_C_are_green_t",
                                                                            IntermediateThrowEventType.SIGNAL,
                                                                            a_c_are_green);

        ExclusiveGateway e2 = new ExclusiveGateway(getId(), "e2");
        final EndEvent controller_stopped = new EndEvent(getId(), "controller_stopped");

        // Bus controller (B)
        StartEvent approaching_junction_B = new StartEvent(getId(), "Approaching_Junction_B");
        SendTask request_tl_status_B = new SendTask(getId(), "Request_TL_status_B");
        EventBasedGateway ev1_B = new EventBasedGateway(getId(), "ev1_B");
        IntermediateCatchEvent b_is_red_r = new IntermediateCatchEvent(getId(),
                                                                       "B_is_red",
                                                                       IntermediateCatchEventType.MESSAGE);
        SendTask request_green_tl_B = new SendTask(getId(), "Request_green_tl_B");
        IntermediateCatchEvent b_is_green_signal = new IntermediateCatchEvent(getId(),
                                                                              "B_is_green_signal",
                                                                              IntermediateCatchEventType.SIGNAL,
                                                                              b_is_green);
        ExclusiveGateway e3_B = new ExclusiveGateway(getId(), "e3_B");
        Task pass_junction_B = new Task(getId(), "Pass_Junction_B");
        IntermediateCatchEvent b_is_green_r = new IntermediateCatchEvent(getId(),
                                                                         "B_is_green_r",
                                                                         IntermediateCatchEventType.MESSAGE);
        EndEvent passed_junction_B = new EndEvent(getId(), "Passed_junction_B");

        // Bus controller (A)
        StartEvent approaching_junction_A = new StartEvent(getId(), "Approaching_Junction_A");
        SendTask request_tl_status_A = new SendTask(getId(), "Request_TL_status_A");
        EventBasedGateway ev1_A = new EventBasedGateway(getId(), "ev1_A");
        IntermediateCatchEvent a_is_red_r = new IntermediateCatchEvent(getId(),
                                                                       "A_is_red",
                                                                       IntermediateCatchEventType.MESSAGE);
        SendTask request_green_tl_A = new SendTask(getId(), "Request_green_tl_A");
        IntermediateCatchEvent a_is_green_signal = new IntermediateCatchEvent(getId(),
                                                                              "A_is_green_signal",
                                                                              IntermediateCatchEventType.SIGNAL,
                                                                              b_is_green);
        ExclusiveGateway e3_A = new ExclusiveGateway(getId(), "e3_A");
        Task pass_junction_A = new Task(getId(), "Pass_Junction_A");
        IntermediateCatchEvent a_is_green_r = new IntermediateCatchEvent(getId(),
                                                                         "A_is_green_r",
                                                                         IntermediateCatchEventType.MESSAGE);
        EndEvent passed_junction_A = new EndEvent(getId(), "Passed_junction_A");


        final String modelName = "ecmfa_usecase";
        final BPMNCollaboration collaboration = new BPMNCollaborationBuilder()
                .name(modelName)
                .messageFlow(getId(), request_tl_status_B, tl_status_requested1)
                .messageFlow(getId(), request_tl_status_B, tl_status_requested2)
                .messageFlow(getId(), b_green_t, b_is_green_r)
                .messageFlow(getId(), a_c_green_t, b_is_red_r)
                .messageFlow(getId(), request_green_tl_B, b_green_requested)
                .messageFlow(getId(), request_tl_status_A, tl_status_requested1)
                .messageFlow(getId(), request_tl_status_A, tl_status_requested2)
                .messageFlow(getId(), b_green_t, a_is_red_r)
                .messageFlow(getId(), a_c_green_t, a_is_green_r)
                .messageFlow(getId(), request_green_tl_A, a_c_green_requested)
                .processName("T-Junction Controller")
                .startEvent(controller_started)
                .sequenceFlow(getId(),controller_started, e1)
                .sequenceFlow(getId(),e1, phase1)
                .sequenceFlow(getId(),phase1, switch_to_p2)
                .sequenceFlow(getId(),phase_1_over, switch_to_p2)
                .sequenceFlow(getId(),switch_to_p2, b_green_signal)
                .sequenceFlow(getId(),b_green_signal, phase2)
                .sequenceFlow(getId(),phase2, switch_to_p1)
                .sequenceFlow(getId(),phase_2_over, switch_to_p1)
                .sequenceFlow(getId(),switch_to_p1, ac_green_signal)
                .sequenceFlow(getId(),ac_green_signal, e2)
                .sequenceFlow(getId(),e2, e1)
                .sequenceFlow(getId(),"stop", e2, controller_stopped)
                .buildProcess()
                .processName("Bus controller (B)")
                .startEvent(approaching_junction_B)
                .sequenceFlow(getId(),approaching_junction_B, request_tl_status_B)
                .sequenceFlow(getId(),request_tl_status_B, ev1_B)
                .sequenceFlow(getId(),ev1_B, b_is_red_r)
                .sequenceFlow(getId(),ev1_B, b_is_green_r)
                .sequenceFlow(getId(),b_is_red_r, request_green_tl_B)
                .sequenceFlow(getId(),request_green_tl_B, b_is_green_signal)
                .sequenceFlow(getId(),b_is_green_signal, e3_B)
                .sequenceFlow(getId(),e3_B, pass_junction_B)
                .sequenceFlow(getId(),pass_junction_B, passed_junction_B)
                .sequenceFlow(getId(),b_is_green_r, e3_B)
                .buildProcess()
                .processName("Bus controller (A)")
                .startEvent(approaching_junction_A)
                .sequenceFlow(getId(),approaching_junction_A, request_tl_status_A)
                .sequenceFlow(getId(),request_tl_status_A, ev1_A)
                .sequenceFlow(getId(),ev1_A, a_is_red_r)
                .sequenceFlow(getId(),ev1_A, a_is_green_r)
                .sequenceFlow(getId(),a_is_red_r, request_green_tl_A)
                .sequenceFlow(getId(),request_green_tl_A, a_is_green_signal)
                .sequenceFlow(getId(),a_is_green_signal, e3_A)
                .sequenceFlow(getId(),e3_A, pass_junction_A)
                .sequenceFlow(getId(),pass_junction_A, passed_junction_A)
                .sequenceFlow(getId(),a_is_green_r, e3_A)
                .build();

        this.checkGrooveGeneration(collaboration);
    }

    // java -jar Generator.jar bpmn/ecmfa_usecase.gps
    // Expected output after state space generation:
    // Time (ms):          7449
    // Space (kB):         4050
    //
    // States:       7731
    // Transitions:  24020
    //
    // 9 result states found: Result [elements=[s1728, s4307, s4312, s5320, s5482, s6284, s6305, s6607, s7160]]

    private String getId() {
        return Integer.toString(counter.getAndIncrement());
    }
}
