
package org.smpte_ra.schemas.st2067_2_2016;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.smpte-ra.org/schemas/2067-3/2016}SequenceType">
 *       &lt;sequence>
 *         &lt;element name="ParentTrackID" type="{http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/}UUIDType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "parentTrackID"
})
@XmlRootElement(name = "CDPSequence", namespace = "http://www.smpte-ra.org/schemas/2067-2/2016")
public class CDPSequence
    extends SequenceType
{

    @XmlElement(name = "ParentTrackID", namespace = "http://www.smpte-ra.org/schemas/2067-2/2016", required = true)
    @XmlSchemaType(name = "anyURI")
    protected String parentTrackID;

    /**
     * Gets the value of the parentTrackID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParentTrackID() {
        return parentTrackID;
    }

    /**
     * Sets the value of the parentTrackID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParentTrackID(String value) {
        this.parentTrackID = value;
    }

}
