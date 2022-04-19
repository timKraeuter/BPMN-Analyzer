package groove;

import groove.gxl.Graph;
import groove.gxl.Gxl;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class GxlToXMLConverterTest {
    @Test
    void test() {
        Gxl gxl = new Gxl();
        Graph graph = new Graph();
        gxl.getGraph().add(graph);
        graph.setRole("rule");
        graph.setEdgeids("false");
        graph.setEdgemode("directed");
        graph.setId("addNodesWithEdge");
        String gxlString = GxlToXMLConverter.toXml(gxl);
        assertThat(gxlString, is("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<gxl xmlns=\"http://www.gupro.de/GXL/gxl-1.0.dtd\">\n" +
                "    <graph id=\"addNodesWithEdge\" role=\"rule\" edgeids=\"false\" edgemode=\"directed\"/>\n" +
                "</gxl>\n"));
    }
}