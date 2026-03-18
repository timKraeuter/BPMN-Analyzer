package no.tk.rulegenerator.server.endpoint.verification.exception;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.tk.behavior.bpmn.auxiliary.exceptions.BPMNRuntimeException;
import no.tk.behavior.bpmn.auxiliary.exceptions.GrooveGenerationRuntimeException;
import no.tk.behavior.bpmn.auxiliary.exceptions.ShouldNotHappenRuntimeException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class RuntimeExceptionHandlerTest {

  private final RuntimeExceptionHandler handler = new RuntimeExceptionHandler();

  // --- ModelCheckingException handler ---

  @Test
  void testModelCheckingExceptionReturnsMessage() {
    // Given
    ModelCheckingException ex = new ModelCheckingException("State space generation timed out.");

    // When
    ResponseEntity<ModelCheckingErrorResponse> response = handler.handleModelCheckingException(ex);

    // Then
    assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
    assertThat(response.getBody(), is(notNullValue()));
    assertThat(response.getBody().message(), is("State space generation timed out."));
  }

  // --- BPMN validation exception handler (400 Bad Request) ---

  @Test
  void testBPMNRuntimeExceptionReturnsBadRequest() {
    // Given
    BPMNRuntimeException ex =
        new BPMNRuntimeException(
            "Intermediate throw events should have exactly one incoming sequence flow!");

    // When
    ResponseEntity<ModelCheckingErrorResponse> response = handler.handleBPMNValidationException(ex);

    // Then
    assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    assertThat(response.getBody(), is(notNullValue()));
    assertThat(
        response.getBody().message(),
        is("Intermediate throw events should have exactly one incoming sequence flow!"));
  }

  @Test
  void testGrooveGenerationExceptionReturnsBadRequest() {
    // Given
    GrooveGenerationRuntimeException ex =
        new GrooveGenerationRuntimeException("Grammar generation failed.");

    // When
    ResponseEntity<ModelCheckingErrorResponse> response = handler.handleBPMNValidationException(ex);

    // Then
    assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    assertThat(response.getBody(), is(notNullValue()));
    assertThat(response.getBody().message(), is("Grammar generation failed."));
  }

  // --- ShouldNotHappenRuntimeException handler (500 Internal Server Error) ---

  @Test
  void testShouldNotHappenExceptionReturnsInternalServerError() {
    // Given
    ShouldNotHappenRuntimeException ex =
        new ShouldNotHappenRuntimeException("Only CTL model checking is currently supported!");

    // When
    ResponseEntity<ModelCheckingErrorResponse> response =
        handler.handleShouldNotHappenException(ex);

    // Then
    assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
    assertThat(response.getBody(), is(notNullValue()));
    assertThat(response.getBody().message(), is("Only CTL model checking is currently supported!"));
  }

  // --- Generic RuntimeException catch-all ---

  @Test
  void testGenericRuntimeExceptionReturnsGenericMessage() {
    // Given: An unexpected exception with internal details that should NOT leak
    RuntimeException ex = new RuntimeException("NullPointerException at com.internal.Foo:42");

    // When
    ResponseEntity<ModelCheckingErrorResponse> response = handler.handleUnexpectedException(ex);

    // Then: Generic message is returned, not the internal details
    assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
    assertThat(response.getBody(), is(notNullValue()));
    assertThat(response.getBody().message(), is("An unexpected internal error occurred."));
  }

  @Test
  void testGenericRuntimeExceptionDoesNotLeakMessage() {
    // Given
    RuntimeException ex = new RuntimeException("Connection to database at 10.0.0.5:5432 refused");

    // When
    ResponseEntity<ModelCheckingErrorResponse> response = handler.handleUnexpectedException(ex);

    // Then: Internal details are NOT present in the response
    assertThat(response.getBody(), is(notNullValue()));
    assertThat(response.getBody().message(), is("An unexpected internal error occurred."));
  }

  // --- ModelCheckingErrorResponse JSON serialization ---

  @Test
  void testErrorResponseSerializesToJsonWithMessageKey() throws Exception {
    // Given
    ModelCheckingErrorResponse errorResponse =
        new ModelCheckingErrorResponse("Something went wrong.");
    ObjectMapper mapper = new ObjectMapper();

    // When
    String json = mapper.writeValueAsString(errorResponse);

    // Then: JSON key must be "message" (matching existing test expectations)
    assertThat(json, is("{\"message\":\"Something went wrong.\"}"));
  }

  @Test
  void testErrorResponseDeserializesFromJson() throws Exception {
    // Given
    ObjectMapper mapper = new ObjectMapper();
    String json = "{\"message\":\"test error\"}";

    // When
    ModelCheckingErrorResponse response = mapper.readValue(json, ModelCheckingErrorResponse.class);

    // Then
    assertThat(response.message(), is("test error"));
  }
}
