package no.hvl.tk.rulegenerator.server.endpoint.dtos;

public class ModelCheckingResponse {

  private boolean valid;
  private String error;

  public ModelCheckingResponse(boolean valid, String error) {
    this.valid = valid;
    this.error = error;
  }

  public boolean isValid() {
    return valid;
  }

  public void setValid(boolean valid) {
    this.valid = valid;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }
}
