package groove.runner.checking;

public class ModelCheckingResult {

  private final TemporalLogic usedLogic;
  private final String property;
  private final boolean valid;

  public ModelCheckingResult(TemporalLogic usedLogic, String property, boolean valid) {
    this.usedLogic = usedLogic;
    this.property = property;
    this.valid = valid;
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
}
