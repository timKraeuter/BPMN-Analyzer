
package groove.gxl;

import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.util.ArrayList;
import java.util.List;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "type",
    "attr",
    "graph",
    "relend"
})
@XmlRootElement(name = "rel")
public class Rel {

    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected java.lang.String id;
    @XmlAttribute(name = "isdirected")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected java.lang.String isdirected;
    protected Type type;
    protected List<Attr> attr;
    protected List<Graph> graph;
    protected List<Relend> relend;

    /**
     * Gets the value of the id property.
     *
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *
     */
    public java.lang.String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *
     */
    public void setId(java.lang.String value) {
        this.id = value;
    }

    /**
     * Gets the value of the isdirected property.
     *
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *
     */
    public java.lang.String getIsdirected() {
        return isdirected;
    }

    /**
     * Sets the value of the isdirected property.
     *
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *
     */
    public void setIsdirected(java.lang.String value) {
        this.isdirected = value;
    }

    /**
     * Gets the value of the type property.
     *
     * @return
     *     possible object is
     *     {@link Type }
     *
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     *
     * @param value
     *     allowed object is
     *     {@link Type }
     *
     */
    public void setType(Type value) {
        this.type = value;
    }

    /**
     * Gets the value of the attr property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the attr property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAttr().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Attr }
     *
     *
     */
    public List<Attr> getAttr() {
        if (attr == null) {
            attr = new ArrayList<Attr>();
        }
        return this.attr;
    }

    /**
     * Gets the value of the graph property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the graph property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGraph().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Graph }
     *
     *
     */
    public List<Graph> getGraph() {
        if (graph == null) {
            graph = new ArrayList<Graph>();
        }
        return this.graph;
    }

    /**
     * Gets the value of the relend property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the relend property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRelend().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Relend }
     *
     *
     */
    public List<Relend> getRelend() {
        if (relend == null) {
            relend = new ArrayList<Relend>();
        }
        return this.relend;
    }

}
