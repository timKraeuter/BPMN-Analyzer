package no.tk.scalability;

import static no.tk.scalability.BPMNModelBuilder.ID_FORMAT;

import java.io.File;
import java.util.concurrent.atomic.AtomicLong;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.ParallelGatewayBuilder;
import org.camunda.bpm.model.bpmn.builder.ServiceTaskBuilder;

class ParallelBranchModelGenerator {
    private final AtomicLong idSequencer = new AtomicLong(0);

    private String getNextId() {
        return createID(idSequencer.incrementAndGet());
    }

    private String createID(long id) {
        return String.format(ID_FORMAT, id);
    }

    public void generateParallelBranchModel(int parallelBranches, int branchLength) {
        ParallelGatewayBuilder pg1 =
                Bpmn.createProcess().startEvent(getNextId()).parallelGateway(getNextId());

        ParallelGatewayBuilder pg2 = null;
        for (int i = 0; i < parallelBranches; i++) {
            String id = getNextId();
            ServiceTaskBuilder builder = pg1.serviceTask(id).name(id);
            for (int j = 1; j < branchLength; j++) {
                String taskId = getNextId();
                builder = builder.serviceTask(taskId).name(taskId);
            }
            if (i == 0) {
                pg2 = builder.parallelGateway(getNextId());
                pg2.endEvent(getNextId());
            } else {
                builder.connectTo(pg2.getElement().getId());
            }
        }

        BpmnModelInstance model = pg1.done();
        File file = new File(String.format("C:\\Source\\scalability/p%02dx%02d.bpmn", parallelBranches, branchLength));
        Bpmn.writeModelToFile(file, model);
        idSequencer.set(0);
    }
}
