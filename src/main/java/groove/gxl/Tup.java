
package groove.gxl;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "locatorOrBoolOrIntOrFloatOrStringOrEnumOrSeqOrSetOrBagOrTup"
})
@XmlRootElement(name = "tup")
public class Tup {

    @XmlElements({
        @XmlElement(name = "locator", type = Locator.class),
        @XmlElement(name = "bool", type = Bool.class),
        @XmlElement(name = "int", type = Int.class),
        @XmlElement(name = "float", type = Float.class),
        @XmlElement(name = "string", type = String.class),
        @XmlElement(name = "enum", type = Enum.class),
        @XmlElement(name = "seq", type = Seq.class),
        @XmlElement(name = "set", type = Set.class),
        @XmlElement(name = "bag", type = Bag.class),
        @XmlElement(name = "tup", type = Tup.class)
    })
    protected List<Object> locatorOrBoolOrIntOrFloatOrStringOrEnumOrSeqOrSetOrBagOrTup;

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
     * {@link String }
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
