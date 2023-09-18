package no.tk.groove.gxl;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "",
    propOrder = {"value"})
@XmlRootElement(name = "int")
public class Int {

  @XmlValue protected java.lang.String value;

  /**
   * Gets the value of the value property.
   *
   * @return possible object is {@link java.lang.String }
   */
  public java.lang.String getvalue() {
    return value;
  }

  /**
   * Sets the value of the value property.
   *
   * @param value allowed object is {@link java.lang.String }
   */
  public void setvalue(java.lang.String value) {
    this.value = value;
  }
}
