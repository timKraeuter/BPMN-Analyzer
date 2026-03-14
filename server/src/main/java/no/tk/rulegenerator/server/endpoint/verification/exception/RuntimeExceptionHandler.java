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

  /** Handle known BPMN validation exceptions from the generator library. */
  @ExceptionHandler({
    BPMNRuntimeException.class,
    GrooveGenerationRuntimeException.class,
    ShouldNotHappenRuntimeException.class
  })
  public ResponseEntity<ModelCheckingErrorResponse> handleBPMNException(RuntimeException ex) {
    log.error("BPMN processing exception!", ex);
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
