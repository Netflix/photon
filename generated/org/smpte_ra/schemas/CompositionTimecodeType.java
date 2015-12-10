
package org.smpte_ra.schemas;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CompositionTimecodeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CompositionTimecodeType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="TimecodeDropFrame" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="TimecodeRate" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
 *         &lt;element name="TimecodeStartAddress" type="{http://www.smpte-ra.org/schemas/2067-3/2013}TimecodeType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CompositionTimecodeType", namespace = "http://www.smpte-ra.org/schemas/2067-3/2013", propOrder = {
    "timecodeDropFrame",
    "timecodeRate",
    "timecodeStartAddress"
})
public class CompositionTimecodeType {

    @XmlElement(name = "TimecodeDropFrame")
    protected boolean timecodeDropFrame;
    @XmlElement(name = "TimecodeRate", required = true)
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger timecodeRate;
    @XmlElement(name = "TimecodeStartAddress", required = true)
    protected String timecodeStartAddress;

    /**
     * Gets the value of the timecodeDropFrame property.
     * 
     */
    public boolean isTimecodeDropFrame() {
        return timecodeDropFrame;
    }

    /**
     * Sets the value of the timecodeDropFrame property.
     * 
     */
    public void setTimecodeDropFrame(boolean value) {
        this.timecodeDropFrame = value;
    }

    /**
     * Gets the value of the timecodeRate property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTimecodeRate() {
        return timecodeRate;
    }

    /**
     * Sets the value of the timecodeRate property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTimecodeRate(BigInteger value) {
        this.timecodeRate = value;
    }

    /**
     * Gets the value of the timecodeStartAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTimecodeStartAddress() {
        return timecodeStartAddress;
    }

    /**
     * Sets the value of the timecodeStartAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTimecodeStartAddress(String value) {
        this.timecodeStartAddress = value;
    }

}
