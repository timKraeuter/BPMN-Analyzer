package groove;

import groove.gxl.Graph;
import groove.gxl.Gxl;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

import java.io.StringWriter;

public class GraphToXMLConverter {

    private static JAXBContext jaxbContext;
    private static Marshaller jaxbMarshaller;

    private GraphToXMLConverter() {
    }

    public static String toXml(final Gxl graph) {
        createJAXBObjectsIfNeeded();
        return marshallGraph(graph);
    }

    private static String marshallGraph(final Gxl graph) {
        final StringWriter sw = new StringWriter();
        try {
            jaxbMarshaller.marshal(graph, sw);
        } catch (final JAXBException e) {
            throw new RuntimeException(e);
        }
        return sw.toString();
    }

    private static void createJAXBObjectsIfNeeded() {
        if (jaxbContext == null) {
            try {
                jaxbContext = JAXBContext.newInstance(Gxl.class);
                jaxbMarshaller = jaxbContext.createMarshaller();
                jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            } catch (final JAXBException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
