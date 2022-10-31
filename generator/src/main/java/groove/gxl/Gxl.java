package groove.gxl;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "",
    propOrder = {"graph"})
@XmlRootElement(name = "gxl")
public class Gxl {

  @XmlAttribute(name = "xmlns:xlink")
  @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
  protected java.lang.String xmlnsXlink;

  protected List<Graph> graph;

  /**
   * Gets the value of the xmlnsXlink property.
   *
   * @return possible object is {@link java.lang.String }
   */
  public java.lang.String getXmlnsXlink() {
    if (xmlnsXlink == null) {
      return "http://www.w3.org/1999/xlink";
    } else {
      return xmlnsXlink;
    }
  }

  /**
   * Sets the value of the xmlnsXlink property.
   *
   * @param value allowed object is {@link java.lang.String }
   */
  public void setXmlnsXlink(java.lang.String value) {
    this.xmlnsXlink = value;
  }

  /**
   * Gets the value of the graph property.
   *
   * <p>This accessor method returns a reference to the live list, not a snapshot. Therefore any
   * modification you make to the returned list will be present inside the JAXB object. This is why
   * there is not a <CODE>set</CODE> method for the graph property.
   *
   * <p>For example, to add a new item, do as follows:
   *
   * <pre>
   *    getGraph().add(newItem);
   * </pre>
   *
   * <p>Objects of the following type(s) are allowed in the list {@link Graph }
   */
  public List<Graph> getGraph() {
    if (graph == null) {
      graph = new ArrayList<>();
    }
    return this.graph;
  }
}
