package no.hvl.tk.rulegenerator.server.endpoint.dtos;

public class ModelCheckingResponse {

  private boolean valid;

  public ModelCheckingResponse(boolean valid) {
    this.valid = valid;
  }

  public boolean isValid() {
    return valid;
  }

  public void setValid(boolean valid) {
    this.valid = valid;
  }
}
