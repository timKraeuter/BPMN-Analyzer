package no.tk.behavior.bpmn;

import no.tk.behavior.bpmn.auxiliary.visitors.AbstractProcessVisitor;
import no.tk.behavior.bpmn.auxiliary.visitors.DoNothingFlowNodeVisitor;
import no.tk.behavior.bpmn.events.StartEvent;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import no.tk.util.ValueWrapper;

public class BPMNEventSubprocess extends AbstractBPMNProcess {
  public BPMNEventSubprocess(
      String name,
      Set<SequenceFlow> sequenceFlows,
      Set<FlowNode> flowNodes,
      Set<BPMNEventSubprocess> eventSubprocesses) {
    super(name, sequenceFlows, flowNodes, eventSubprocesses);
  }

  @Override
  public void accept(AbstractProcessVisitor visitor) {
    visitor.handle(this);
  }

  @Override
  public boolean isEventSubprocess() {
    return true;
  }

  public Set<StartEvent> getStartEvents() {
    return this.getFlowNodes()
        .map(
            flowNode -> {
              ValueWrapper<StartEvent> valueWrapper = new ValueWrapper<>();
              flowNode.accept(
                  new DoNothingFlowNodeVisitor() {
                    @Override
                    public void handle(StartEvent startEvent) {
                      valueWrapper.setValue(startEvent);
                    }
                  });
              return valueWrapper.getValueIfExists();
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }
}
