package maude.behaviortransformer;

import behavior.Behavior;
import behavior.BehaviorVisitor;
import behavior.activity.ActivityDiagram;
import behavior.bpmn.BPMNCollaboration;
import behavior.fsm.FiniteStateMachine;
import behavior.petrinet.PetriNet;
import behavior.picalculus.NamedPiProcess;
import util.ValueWrapper;

public class BehaviorToMaudeTransformer {

    public String generateMaudeModule(Behavior behavior) {
        ValueWrapper<String> resultWrapper = new ValueWrapper<>();
        behavior.accept(new BehaviorVisitor() {
            @Override
            public void handle(FiniteStateMachine finiteStateMachine) {
                FSMToMaudeTransformer fsmToMaudeTransformer = new FSMToMaudeTransformer(finiteStateMachine);
                resultWrapper.setValue(fsmToMaudeTransformer.generate());
            }

            @Override
            public void handle(PetriNet petriNet) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void handle(BPMNCollaboration bpmnProcess) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void handle(NamedPiProcess piProcess) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void handle(ActivityDiagram activityDiagram) {
                throw new UnsupportedOperationException();
            }
        });
        return resultWrapper.getValueIfExists();
    }
}
