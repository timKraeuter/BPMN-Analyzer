
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
    "nodeOrEdgeOrRel"
})
@XmlRootElement(name = "graph")
public class Graph {

    @XmlAttribute(name = "id", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected java.lang.String id;
    @XmlAttribute(name = "role")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected java.lang.String role;
    @XmlAttribute(name = "edgeids")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected java.lang.String edgeids;
    @XmlAttribute(name = "hypergraph")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected java.lang.String hypergraph;
    @XmlAttribute(name = "edgemode")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected java.lang.String edgemode;
    protected Type type;
    protected List<Attr> attr;
    @XmlElements({
        @XmlElement(name = "node", type = Node.class),
        @XmlElement(name = "edge", type = Edge.class),
        @XmlElement(name = "rel", type = Rel.class)
    })
    protected List<Object> nodeOrEdgeOrRel;

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
     * Gets the value of the role property.
     *
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *
     */
    public java.lang.String getRole() {
        return role;
    }

    /**
     * Sets the value of the role property.
     *
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *
     */
    public void setRole(java.lang.String value) {
        this.role = value;
    }

    /**
     * Gets the value of the edgeids property.
     *
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *
     */
    public java.lang.String getEdgeids() {
        if (edgeids == null) {
            return "false";
        } else {
            return edgeids;
        }
    }

    /**
     * Sets the value of the edgeids property.
     *
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *
     */
    public void setEdgeids(java.lang.String value) {
        this.edgeids = value;
    }

    /**
     * Gets the value of the hypergraph property.
     *
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *
     */
    public java.lang.String getHypergraph() {
        if (hypergraph == null) {
            return "false";
        } else {
            return hypergraph;
        }
    }

    /**
     * Sets the value of the hypergraph property.
     *
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *
     */
    public void setHypergraph(java.lang.String value) {
        this.hypergraph = value;
    }

    /**
     * Gets the value of the edgemode property.
     *
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *
     */
    public java.lang.String getEdgemode() {
        if (edgemode == null) {
            return "directed";
        } else {
            return edgemode;
        }
    }

    /**
     * Sets the value of the edgemode property.
     *
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *
     */
    public void setEdgemode(java.lang.String value) {
        this.edgemode = value;
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
     * Gets the value of the nodeOrEdgeOrRel property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the nodeOrEdgeOrRel property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNodeOrEdgeOrRel().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Node }
     * {@link Edge }
     * {@link Rel }
     *
     *
     */
    public List<Object> getNodeOrEdgeOrRel() {
        if (nodeOrEdgeOrRel == null) {
            nodeOrEdgeOrRel = new ArrayList<Object>();
        }
        return this.nodeOrEdgeOrRel;
    }

}
