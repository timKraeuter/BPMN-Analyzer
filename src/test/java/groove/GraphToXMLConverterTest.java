package groove;

import groove.gxl.Graph;
import groove.gxl.Gxl;
import org.junit.jupiter.api.Test;

class GraphToXMLConverterTest {
    @Test
    void test() {
        Gxl gxl = new Gxl();
        Graph graph = new Graph();
        gxl.getGraph().add(graph);
        graph.setRole("rule");
        graph.setEdgeids("false");
        graph.setEdgemode("directed");
        graph.setId("addNodesWithEdge");
        String s = GraphToXMLConverter.toXml(gxl);
        System.out.println(s);
    }
}