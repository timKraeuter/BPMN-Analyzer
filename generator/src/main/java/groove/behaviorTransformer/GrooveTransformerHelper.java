package groove.behaviorTransformer;

import static groove.behaviorTransformer.GrooveTransformer.STRING;

public class GrooveTransformerHelper {
    public static String createStringNodeLabel(String stringValue) {
        return String.format("%s\"%s\"", STRING, stringValue);
    }
}
