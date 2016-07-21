
package org.smpte_ra.schemas.st2067_2_2016;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for deviceIdentifierListType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="deviceIdentifierListType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PrimaryID" type="{http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/}deviceIdentifierPolyType"/>
 *         &lt;element name="SecondaryID" type="{http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/}deviceIdentifierPolyType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "deviceIdentifierListType", namespace = "http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/", propOrder = {
    "primaryID",
    "secondaryID"
})
public class DeviceIdentifierListType {

    @XmlElement(name = "PrimaryID", required = true)
    protected DeviceIdentifierPolyType primaryID;
    @XmlElement(name = "SecondaryID")
    protected DeviceIdentifierPolyType secondaryID;

    /**
     * Gets the value of the primaryID property.
     * 
     * @return
     *     possible object is
     *     {@link DeviceIdentifierPolyType }
     *     
     */
    public DeviceIdentifierPolyType getPrimaryID() {
        return primaryID;
    }

    /**
     * Sets the value of the primaryID property.
     * 
     * @param value
     *     allowed object is
     *     {@link DeviceIdentifierPolyType }
     *     
     */
    public void setPrimaryID(DeviceIdentifierPolyType value) {
        this.primaryID = value;
    }

    /**
     * Gets the value of the secondaryID property.
     * 
     * @return
     *     possible object is
     *     {@link DeviceIdentifierPolyType }
     *     
     */
    public DeviceIdentifierPolyType getSecondaryID() {
        return secondaryID;
    }

    /**
     * Sets the value of the secondaryID property.
     * 
     * @param value
     *     allowed object is
     *     {@link DeviceIdentifierPolyType }
     *     
     */
    public void setSecondaryID(DeviceIdentifierPolyType value) {
        this.secondaryID = value;
    }

}
