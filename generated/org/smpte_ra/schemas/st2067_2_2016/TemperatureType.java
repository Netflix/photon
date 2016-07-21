
package org.smpte_ra.schemas.st2067_2_2016;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


/**
 * <p>Java class for temperatureType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="temperatureType">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>decimal">
 *       &lt;attribute name="units" use="required" type="{http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/}temperatureUnitsToken" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "temperatureType", namespace = "http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/", propOrder = {
    "value"
})
public class TemperatureType {

    @XmlValue
    protected BigDecimal value;
    @XmlAttribute(name = "units", required = true)
    protected TemperatureUnitsToken units;

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
     *     {@link TemperatureUnitsToken }
     *     
     */
    public TemperatureUnitsToken getUnits() {
        return units;
    }

    /**
     * Sets the value of the units property.
     * 
     * @param value
     *     allowed object is
     *     {@link TemperatureUnitsToken }
     *     
     */
    public void setUnits(TemperatureUnitsToken value) {
        this.units = value;
    }

}
