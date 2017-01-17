
package org.smpte_ra.schemas.st429_7_2006;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ContentVersionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ContentVersionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Id" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
 *         &lt;element name="LabelText" type="{http://www.smpte-ra.org/schemas/429-7/2006/CPL}UserText"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ContentVersionType", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL", propOrder = {
    "id",
    "labelText"
})
public class ContentVersionType {

    @XmlElement(name = "Id", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL", required = true)
    @XmlSchemaType(name = "anyURI")
    protected String id;
    @XmlElement(name = "LabelText", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL", required = true)
    protected UserText labelText;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the labelText property.
     * 
     * @return
     *     possible object is
     *     {@link UserText }
     *     
     */
    public UserText getLabelText() {
        return labelText;
    }

    /**
     * Sets the value of the labelText property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserText }
     *     
     */
    public void setLabelText(UserText value) {
        this.labelText = value;
    }

}
