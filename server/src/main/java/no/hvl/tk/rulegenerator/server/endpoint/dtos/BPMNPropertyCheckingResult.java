package no.hvl.tk.rulegenerator.server.endpoint.dtos;

public class BPMNPropertyCheckingResult {
    private ModelCheckingProperty name;
    private boolean holds;
    private String additionalInfo;

    public BPMNPropertyCheckingResult(ModelCheckingProperty name, boolean holds, String additionalInfo) {
        this.name = name;
        this.holds = holds;
        this.additionalInfo = additionalInfo;
    }

    public ModelCheckingProperty getName() {
        return name;
    }

    public void setName(ModelCheckingProperty name) {
        this.name = name;
    }

    public boolean isHolds() {
        return holds;
    }

    public void setHolds(boolean holds) {
        this.holds = holds;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}
