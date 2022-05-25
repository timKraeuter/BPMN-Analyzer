package no.hvl.tk.ruleGenerator.server.endpoint.dtos;

public class BPMNPropertyCheckingResult {
    public ModelCheckingProperty name;
    public boolean holds;
    public String additionalInfo;

    public BPMNPropertyCheckingResult(ModelCheckingProperty name, boolean holds, String additionalInfo) {
        this.name = name;
        this.holds = holds;
        this.additionalInfo = additionalInfo;
    }
}
