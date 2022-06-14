package groove;

import groove.gxl.Gxl;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

public class GxlToXMLConverter {

    private static JAXBContext jaxbContext;
    private static Marshaller jaxbMarshaller;

    private GxlToXMLConverter() {
    }

    public static void toXml(final Gxl gxl, File outputFile) {
        createJAXBObjectsIfNeeded();
        try {
            System.out.println(outputFile.getPath());
            outputFile.getParentFile().mkdirs();
            outputFile.createNewFile();
            jaxbMarshaller.marshal(gxl, outputFile);
        } catch (final JAXBException | IOException e) {
            throw new RuntimeException(e);
        }
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
