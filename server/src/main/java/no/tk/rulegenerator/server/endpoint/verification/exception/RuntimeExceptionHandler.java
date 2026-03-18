package no.tk.rulegenerator.server.endpoint.verification.exception;

import no.tk.behavior.bpmn.auxiliary.exceptions.BPMNRuntimeException;
import no.tk.behavior.bpmn.auxiliary.exceptions.GrooveGenerationRuntimeException;
import no.tk.behavior.bpmn.auxiliary.exceptions.ShouldNotHappenRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RuntimeExceptionHandler extends ResponseEntityExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(RuntimeExceptionHandler.class);

  /** Handle known model-checking exceptions with user-facing messages. */
  @ExceptionHandler(ModelCheckingException.class)
  public ResponseEntity<ModelCheckingErrorResponse> handleModelCheckingException(
      ModelCheckingException ex) {
    log.error("Model checking exception!", ex);
    return new ResponseEntity<>(
        new ModelCheckingErrorResponse(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /** Handle BPMN validation errors caused by invalid user input (bad BPMN models). */
  @ExceptionHandler({BPMNRuntimeException.class, GrooveGenerationRuntimeException.class})
  public ResponseEntity<ModelCheckingErrorResponse> handleBPMNValidationException(
      RuntimeException ex) {
    log.error("BPMN validation exception!", ex);
    return new ResponseEntity<>(
        new ModelCheckingErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
  }

  /** Handle internal generator errors that indicate a bug rather than invalid input. */
  @ExceptionHandler(ShouldNotHappenRuntimeException.class)
  public ResponseEntity<ModelCheckingErrorResponse> handleShouldNotHappenException(
      ShouldNotHappenRuntimeException ex) {
    log.error("Internal generator error!", ex);
    return new ResponseEntity<>(
        new ModelCheckingErrorResponse(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /** Catch-all for unexpected exceptions — does not leak internal details to the client. */
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ModelCheckingErrorResponse> handleUnexpectedException(RuntimeException ex) {
    log.error("Unexpected exception in controller!", ex);
    return new ResponseEntity<>(
        new ModelCheckingErrorResponse("An unexpected internal error occurred."),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
