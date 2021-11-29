package groove.behaviorTransformer;

import behavior.piCalculus.*;
import groove.GrooveGxlHelper;
import groove.GxlToXMLConverter;
import groove.gxl.Graph;
import groove.gxl.Gxl;
import groove.gxl.Node;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class PiCalcToGrooveTransformer {
    void copyPiRules(File targetFolder) {
        // TODO: Copy the fixed set of rules from somewhere.
    }

    void generatePiStartGraph(NamedPiProcess piProcess, File targetFolder) {
        Gxl gxl = new Gxl();
        Graph graph = GrooveGxlHelper.createStandardGxlGraph(piProcess.getName(), gxl);
//        GrooveGxlHelper.createNodeWithName(START_NODE_ID, piProcess.getName(), graph);

        Map<String, String> nodeIdToLabel = new HashMap<>();
        AtomicLong idCounter = new AtomicLong(-1);
        this.createGraph(graph, piProcess, nodeIdToLabel, idCounter);

        GrooveGxlHelper.layoutGraph(graph, nodeIdToLabel);

        File startGraphFile = new File(targetFolder.getPath() + String.format("/%s.gst", piProcess.getName()));
        GxlToXMLConverter.toXml(gxl, startGraphFile);
    }

    private void createGraph(
            Graph graph,
            NamedPiProcess piProcess,
            Map<String, String> nodeIdToLabel,
            AtomicLong idCounter) {
        piProcess.getProcess().accept(new PiProcessVisitor() {

            @Override
            public void handle(Parallelism parallelism) {

            }

            @Override
            public void handle(NameRestriction restriction) {

            }

            @Override
            public void handle(PrefixedProcess prefixedProcess) {

            }

            @Override
            public void handle(EmptySum emptySum) {
                String label = "type:Process";
                Node node = GrooveGxlHelper.createNodeWithName(
                        PiCalcToGrooveTransformer.this.getNodeId(idCounter),
                        label,
                        graph);
                nodeIdToLabel.put(node.getId(), label);
            }

            @Override
            public void handle(MultiarySum multiarySum) {

            }
        });
    }

    private String getNodeId(AtomicLong idCounter) {
        return "n" + idCounter.incrementAndGet();
    }
}
