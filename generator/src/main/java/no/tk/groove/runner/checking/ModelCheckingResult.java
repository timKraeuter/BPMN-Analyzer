package no.tk.groove.runner.checking;

/**
 * Result of a model checking operation.
 *
 * @param error Empty means no error. Non-empty means error with the given message.
 */
public record ModelCheckingResult(
    TemporalLogic usedLogic, String property, boolean valid, String error) {
  public boolean hasError() {
    return !error.isEmpty();
  }
}
