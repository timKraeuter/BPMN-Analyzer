package no.hvl.tk.rulegenerator.server.endpoint.verification.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ModelCheckingExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ModelCheckingException.class)
    public ResponseEntity<ModelCheckingErrorResponse> customHandleNotFound(Exception ex) {

        ModelCheckingErrorResponse errors = new ModelCheckingErrorResponse();
        errors.setMessage(ex.getMessage());

        return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}