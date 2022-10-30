package behavior.bpmn.auxiliary.exceptions;

public class ShouldNotHappenRuntimeException extends RuntimeException {
  public ShouldNotHappenRuntimeException(String message) {
    super(message);
  }

  public ShouldNotHappenRuntimeException(Exception e) {
    super(e);
  }

  public ShouldNotHappenRuntimeException() {}
}
