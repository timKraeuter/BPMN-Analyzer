
package groove.gxl;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "attr",
    "locatorOrBoolOrIntOrFloatOrStringOrEnumOrSeqOrSetOrBagOrTup"
})
@XmlRootElement(name = "attr")
public class Attr {

    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected java.lang.String id;
    @XmlAttribute(name = "name", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected java.lang.String name;
    @XmlAttribute(name = "kind")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected java.lang.String kind;
    protected List<Attr> attr;
    @XmlElements({
        @XmlElement(name = "locator", required = true, type = Locator.class),
        @XmlElement(name = "bool", required = true, type = Bool.class),
        @XmlElement(name = "int", required = true, type = Int.class),
        @XmlElement(name = "float", required = true, type = Float.class),
        @XmlElement(name = "string", required = true, type = groove.gxl.String.class),
        @XmlElement(name = "enum", required = true, type = Enum.class),
        @XmlElement(name = "seq", required = true, type = Seq.class),
        @XmlElement(name = "set", required = true, type = Set.class),
        @XmlElement(name = "bag", required = true, type = Bag.class),
        @XmlElement(name = "tup", required = true, type = Tup.class)
    })
    protected List<Object> locatorOrBoolOrIntOrFloatOrStringOrEnumOrSeqOrSetOrBagOrTup;

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
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setName(java.lang.String value) {
        this.name = value;
    }

    /**
     * Gets the value of the kind property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getKind() {
        return kind;
    }

    /**
     * Sets the value of the kind property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setKind(java.lang.String value) {
        this.kind = value;
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
     * Gets the value of the locatorOrBoolOrIntOrFloatOrStringOrEnumOrSeqOrSetOrBagOrTup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the locatorOrBoolOrIntOrFloatOrStringOrEnumOrSeqOrSetOrBagOrTup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLocatorOrBoolOrIntOrFloatOrStringOrEnumOrSeqOrSetOrBagOrTup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Locator }
     * {@link Bool }
     * {@link Int }
     * {@link Float }
     * {@link groove.gxl.String }
     * {@link Enum }
     * {@link Seq }
     * {@link Set }
     * {@link Bag }
     * {@link Tup }
     * 
     * 
     */
    public List<Object> getLocatorOrBoolOrIntOrFloatOrStringOrEnumOrSeqOrSetOrBagOrTup() {
        if (locatorOrBoolOrIntOrFloatOrStringOrEnumOrSeqOrSetOrBagOrTup == null) {
            locatorOrBoolOrIntOrFloatOrStringOrEnumOrSeqOrSetOrBagOrTup = new ArrayList<Object>();
        }
        return this.locatorOrBoolOrIntOrFloatOrStringOrEnumOrSeqOrSetOrBagOrTup;
    }

}
