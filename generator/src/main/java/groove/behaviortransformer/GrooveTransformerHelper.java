package groove.behaviortransformer;

import static groove.behaviortransformer.GrooveTransformer.STRING;

public class GrooveTransformerHelper {
    public static String createStringNodeLabel(String stringValue) {
        return String.format("%s\"%s\"", STRING, stringValue);
    }
}
