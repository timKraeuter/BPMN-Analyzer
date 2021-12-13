package groove.behaviorTransformer;

import behavior.Behavior;
import groove.GxlToXMLConverter;
import groove.graph.GrooveGraph;
import groove.graph.rule.GrooveGraphRule;
import groove.graph.rule.GrooveRuleWriter;
import groove.gxl.Gxl;

import java.io.File;
import java.util.stream.Stream;

import static groove.behaviorTransformer.BehaviorToGrooveTransformer.START_GST;

public interface GrooveTransformer<SOURCE extends Behavior> {

    GrooveGraph generateStartGraph(SOURCE source, boolean addPrefix);

    default void generateAndWriteStartGraph(SOURCE source, boolean addPrefix, File targetFolder) {
        GrooveGraph startGraph = this.generateStartGraph(source, addPrefix);
        writeStartGraph(targetFolder, startGraph);
    }

    static void writeStartGraph(File targetFolder, GrooveGraph startGraph) {
        Gxl gxl = BehaviorToGrooveTransformer.createGxlFromGrooveGraph(startGraph);
        File startGraphFile = new File(targetFolder.getPath() + START_GST);

        GxlToXMLConverter.toXml(gxl, startGraphFile);
    }

    Stream<GrooveGraphRule> generateRules(SOURCE source, boolean addPrefix);

    default void generateAndWriteRules(SOURCE source, boolean addPrefix, File targetFolder) {
        Stream<GrooveGraphRule> rules = this.generateRules(source, addPrefix);
        GrooveRuleWriter.writeRules(rules, targetFolder);
    }

}
