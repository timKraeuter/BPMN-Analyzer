package groove.runner.checking;

public class ModelCheckingResult {

  private final TemporalLogic usedLogic;
  private final String property;
  private final boolean valid;

  /** Empty means no error. Non-empty means error with the given message. */
  private final String error;

  public ModelCheckingResult(
      TemporalLogic usedLogic, String property, boolean valid, String error) {
    this.usedLogic = usedLogic;
    this.property = property;
    this.valid = valid;
    this.error = error;
  }

  public TemporalLogic getUsedLogic() {
    return usedLogic;
  }

  public String getProperty() {
    return property;
  }

  public boolean isValid() {
    return valid;
  }

  public boolean hasError() {
    return !error.isEmpty();
  }

  public String getError() {
    return error;
  }
}
