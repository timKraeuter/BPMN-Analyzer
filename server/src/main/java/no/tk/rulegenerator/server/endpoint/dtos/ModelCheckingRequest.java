package no.tk.rulegenerator.server.endpoint.dtos;

import java.util.HashSet;
import java.util.Set;
import no.tk.groove.runner.checking.TemporalLogic;
import no.tk.rulegenerator.server.endpoint.converter.RequiresConversion;
import org.springframework.web.multipart.MultipartFile;

public class ModelCheckingRequest {

  private MultipartFile file;
  private String property;
  private TemporalLogic logic;
  @RequiresConversion private Set<BPMNProposition> propositions;

  public ModelCheckingRequest() {
    propositions = new HashSet<>();
  }

  public MultipartFile getFile() {
    return file;
  }

  public void setFile(MultipartFile file) {
    this.file = file;
  }

  public String getProperty() {
    return property;
  }

  public void setProperty(String property) {
    this.property = property;
  }

  public TemporalLogic getLogic() {
    return logic;
  }

  public void setLogic(TemporalLogic logic) {
    this.logic = logic;
  }

  public Set<BPMNProposition> getPropositions() {
    return propositions;
  }

  public void setPropositions(Set<BPMNProposition> propositions) {
    this.propositions = propositions;
  }
}
