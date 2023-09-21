package no.tk.rulegenerator.server.endpoint.verification.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RuntimeExceptionHandler extends ResponseEntityExceptionHandler {
  private final Logger log = LoggerFactory.getLogger(RuntimeExceptionHandler.class);

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ModelCheckingErrorResponse> customHandleNotFound(Exception ex) {
    log.error("Unexpected exception in controller!", ex);

    ModelCheckingErrorResponse errors = new ModelCheckingErrorResponse();
    errors.setMessage(ex.getMessage());

    return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
