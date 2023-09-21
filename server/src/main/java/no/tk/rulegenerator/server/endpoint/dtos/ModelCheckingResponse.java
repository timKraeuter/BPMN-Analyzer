package no.tk.rulegenerator.server.endpoint.dtos;

public class ModelCheckingResponse {

  private String property;

  private boolean valid;
  private String error;

  public ModelCheckingResponse(String property, boolean valid, String error) {
    this.property = property;
    this.valid = valid;
    this.error = error;
  }

  public String getProperty() {
    return property;
  }

  public void setProperty(String property) {
    this.property = property;
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
