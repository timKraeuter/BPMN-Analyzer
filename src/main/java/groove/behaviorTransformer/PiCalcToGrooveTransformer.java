package groove.behaviorTransformer;

import behavior.piCalculus.NamedPiProcess;
import groove.GrooveGxlHelper;
import groove.GxlToXMLConverter;
import groove.gxl.Graph;
import groove.gxl.Gxl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static groove.behaviorTransformer.BehaviorToGrooveTransformer.START_NODE_ID;

public class PiCalcToGrooveTransformer {
    void copyPiRules(File targetFolder) {
        // TODO: Copy the fixed set of rules from somewhere.
    }

    void generatePiStartGraph(NamedPiProcess piProcess, File targetFolder) {
        Gxl gxl = new Gxl();
        Graph graph = GrooveGxlHelper.createStandardGxlGraph(piProcess.getName(), gxl);
        GrooveGxlHelper.createNodeWithName(START_NODE_ID, piProcess.getName(), graph);

        // TODO: implement
        Map<String, String> nodeLabels = new HashMap<>();
        nodeLabels.put(START_NODE_ID, piProcess.getName());
        GrooveGxlHelper.layoutGraph(graph, nodeLabels);

        File startGraphFile = new File(targetFolder.getPath() + String.format("%s.gst", piProcess.getName()));
        GxlToXMLConverter.toXml(gxl, startGraphFile);
    }
}
