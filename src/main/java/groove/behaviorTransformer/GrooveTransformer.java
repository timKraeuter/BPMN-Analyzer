package groove.behaviorTransformer;

import behavior.Behavior;
import groove.GxlToXMLConverter;
import groove.graph.GrooveGraph;
import groove.graph.GrooveRuleBuilder;
import groove.graph.GrooveRuleWriter;
import groove.gxl.Gxl;

import java.io.File;

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

    GrooveRuleBuilder generateRules(SOURCE source, boolean addPrefix);

    default void generateAndWriteRules(SOURCE source, boolean addPrefix, File targetFolder) {
        GrooveRuleBuilder ruleBuilder = this.generateRules(source, addPrefix);
        GrooveRuleWriter.writeRules(ruleBuilder.getRules(), targetFolder);
    }

}