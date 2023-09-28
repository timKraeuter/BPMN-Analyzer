package no.tk.groove;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import no.tk.behavior.bpmn.auxiliary.exceptions.ShouldNotHappenRuntimeException;
import no.tk.groove.gxl.Gxl;

public class GxlToXMLConverter {

  private GxlToXMLConverter() {}

  public static void toXml(final Gxl gxl, Path outputFile) {
    try {
      Files.deleteIfExists(outputFile);
      Files.createFile(outputFile);

      Marshaller jaxbMarshaller = createJAXBMarshaller();
      jaxbMarshaller.marshal(gxl, Files.newOutputStream(outputFile));
    } catch (final JAXBException | IOException e) {
      throw new ShouldNotHappenRuntimeException(e);
    }
  }

  public static String toXml(final Gxl graph) {
    return marshallGraph(graph);
  }

  private static String marshallGraph(final Gxl graph) {
    final StringWriter sw = new StringWriter();
    try {
      Marshaller jaxbMarshaller = createJAXBMarshaller();
      jaxbMarshaller.marshal(graph, sw);
    } catch (final JAXBException e) {
      throw new ShouldNotHappenRuntimeException(e);
    }
    return sw.toString();
  }

  private static Marshaller createJAXBMarshaller() throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(Gxl.class);
    Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
    jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

    return jaxbMarshaller;
  }
}
