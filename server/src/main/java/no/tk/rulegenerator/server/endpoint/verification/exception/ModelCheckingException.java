package no.tk.rulegenerator.server.endpoint.verification.exception;

public class ModelCheckingException extends RuntimeException {
  public ModelCheckingException(String message) {
    super(message);
  }

  public ModelCheckingException(String message, Throwable cause) {
    super(message, cause);
  }
}
