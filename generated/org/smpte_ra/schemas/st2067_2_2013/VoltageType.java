
package org.smpte_ra.schemas.st2067_2_2013;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


/**
 * <p>Java class for voltageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="voltageType">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>decimal">
 *       &lt;attribute name="units" use="required" type="{http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/}voltageUnitsToken" />
 *       &lt;attribute name="mode" use="required" type="{http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/}currentModeToken" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "voltageType", namespace = "http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/", propOrder = {
    "value"
})
public class VoltageType {

    @XmlValue
    protected BigDecimal value;
    @XmlAttribute(required = true)
    protected VoltageUnitsToken units;
    @XmlAttribute(required = true)
    protected CurrentModeToken mode;

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setValue(BigDecimal value) {
        this.value = value;
    }

    /**
     * Gets the value of the units property.
     * 
     * @return
     *     possible object is
     *     {@link VoltageUnitsToken }
     *     
     */
    public VoltageUnitsToken getUnits() {
        return units;
    }

    /**
     * Sets the value of the units property.
     * 
     * @param value
     *     allowed object is
     *     {@link VoltageUnitsToken }
     *     
     */
    public void setUnits(VoltageUnitsToken value) {
        this.units = value;
    }

    /**
     * Gets the value of the mode property.
     * 
     * @return
     *     possible object is
     *     {@link CurrentModeToken }
     *     
     */
    public CurrentModeToken getMode() {
        return mode;
    }

    /**
     * Sets the value of the mode property.
     * 
     * @param value
     *     allowed object is
     *     {@link CurrentModeToken }
     *     
     */
    public void setMode(CurrentModeToken value) {
        this.mode = value;
    }

}
