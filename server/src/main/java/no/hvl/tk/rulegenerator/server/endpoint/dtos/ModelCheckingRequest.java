package no.hvl.tk.rulegenerator.server.endpoint.dtos;

import no.tk.groove.runner.checking.TemporalLogic;
import org.springframework.web.multipart.MultipartFile;

public class ModelCheckingRequest {
  MultipartFile file;
  private String property;
  private TemporalLogic logic;

  public ModelCheckingRequest() {
    // DTO
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

  public MultipartFile getFile() {
    return file;
  }

  public void setFile(MultipartFile file) {
    this.file = file;
  }
}
