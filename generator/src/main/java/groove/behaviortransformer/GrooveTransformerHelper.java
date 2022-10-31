package groove.behaviortransformer;

import static groove.behaviortransformer.GrooveTransformer.STRING;

public class GrooveTransformerHelper {

  private GrooveTransformerHelper() {
    // Helper class.
  }

  public static String createStringNodeLabel(String stringValue) {
    return String.format("%s\"%s\"", STRING, stringValue);
  }
}
