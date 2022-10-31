package groove.gxl;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlIDREF;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "",
    propOrder = {"type", "attr", "graph"})
@XmlRootElement(name = "edge")
public class Edge {

  @XmlAttribute(name = "id")
  @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
  @XmlID
  protected java.lang.String id;

  @XmlAttribute(name = "from", required = true)
  @XmlIDREF
  protected Object from;

  @XmlAttribute(name = "to", required = true)
  @XmlIDREF
  protected Object to;

  @XmlAttribute(name = "fromorder")
  @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
  protected java.lang.String fromorder;

  @XmlAttribute(name = "toorder")
  @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
  protected java.lang.String toorder;

  @XmlAttribute(name = "isdirected")
  @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
  protected java.lang.String isdirected;

  protected Type type;
  protected List<Attr> attr;
  protected List<Graph> graph;

  /**
   * Gets the value of the id property.
   *
   * @return possible object is {@link java.lang.String }
   */
  public java.lang.String getId() {
    return id;
  }

  /**
   * Sets the value of the id property.
   *
   * @param value allowed object is {@link java.lang.String }
   */
  public void setId(java.lang.String value) {
    this.id = value;
  }

  /**
   * Gets the value of the from property.
   *
   * @return possible object is {@link Object }
   */
  public Object getFrom() {
    return from;
  }

  /**
   * Sets the value of the from property.
   *
   * @param value allowed object is {@link Object }
   */
  public void setFrom(Object value) {
    this.from = value;
  }

  /**
   * Gets the value of the to property.
   *
   * @return possible object is {@link Object }
   */
  public Object getTo() {
    return to;
  }

  /**
   * Sets the value of the to property.
   *
   * @param value allowed object is {@link Object }
   */
  public void setTo(Object value) {
    this.to = value;
  }

  /**
   * Gets the value of the fromorder property.
   *
   * @return possible object is {@link java.lang.String }
   */
  public java.lang.String getFromorder() {
    return fromorder;
  }

  /**
   * Sets the value of the fromorder property.
   *
   * @param value allowed object is {@link java.lang.String }
   */
  public void setFromorder(java.lang.String value) {
    this.fromorder = value;
  }

  /**
   * Gets the value of the toorder property.
   *
   * @return possible object is {@link java.lang.String }
   */
  public java.lang.String getToorder() {
    return toorder;
  }

  /**
   * Sets the value of the toorder property.
   *
   * @param value allowed object is {@link java.lang.String }
   */
  public void setToorder(java.lang.String value) {
    this.toorder = value;
  }

  /**
   * Gets the value of the isdirected property.
   *
   * @return possible object is {@link java.lang.String }
   */
  public java.lang.String getIsdirected() {
    return isdirected;
  }

  /**
   * Sets the value of the isdirected property.
   *
   * @param value allowed object is {@link java.lang.String }
   */
  public void setIsdirected(java.lang.String value) {
    this.isdirected = value;
  }

  /**
   * Gets the value of the type property.
   *
   * @return possible object is {@link Type }
   */
  public Type getType() {
    return type;
  }

  /**
   * Sets the value of the type property.
   *
   * @param value allowed object is {@link Type }
   */
  public void setType(Type value) {
    this.type = value;
  }

  /**
   * Gets the value of the attr property.
   *
   * <p>This accessor method returns a reference to the live list, not a snapshot. Therefore any
   * modification you make to the returned list will be present inside the JAXB object. This is why
   * there is not a <CODE>set</CODE> method for the attr property.
   *
   * <p>For example, to add a new item, do as follows:
   *
   * <pre>
   *    getAttr().add(newItem);
   * </pre>
   *
   * <p>Objects of the following type(s) are allowed in the list {@link Attr }
   */
  public List<Attr> getAttr() {
    if (attr == null) {
      attr = new ArrayList<>();
    }
    return this.attr;
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
