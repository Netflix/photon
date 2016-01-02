
package org.smpte_ra.schemas.st2067_2_2013;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for TrackFileResourceType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TrackFileResourceType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.smpte-ra.org/schemas/2067-3/2013}BaseResourceType">
 *       &lt;sequence>
 *         &lt;element name="SourceEncoding" type="{http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/}UUIDType"/>
 *         &lt;element name="TrackFileId" type="{http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/}UUIDType"/>
 *         &lt;element name="KeyId" type="{http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/}UUIDType" minOccurs="0"/>
 *         &lt;element name="Hash" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TrackFileResourceType", namespace = "http://www.smpte-ra.org/schemas/2067-3/2013", propOrder = {
    "sourceEncoding",
    "trackFileId",
    "keyId",
    "hash"
})
public class TrackFileResourceType
    extends BaseResourceType
{

    @XmlElement(name = "SourceEncoding", required = true)
    protected String sourceEncoding;
    @XmlElement(name = "TrackFileId", required = true)
    protected String trackFileId;
    @XmlElement(name = "KeyId")
    protected String keyId;
    @XmlElement(name = "Hash")
    protected byte[] hash;

    /**
     * Gets the value of the sourceEncoding property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceEncoding() {
        return sourceEncoding;
    }

    /**
     * Sets the value of the sourceEncoding property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceEncoding(String value) {
        this.sourceEncoding = value;
    }

    /**
     * Gets the value of the trackFileId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTrackFileId() {
        return trackFileId;
    }

    /**
     * Sets the value of the trackFileId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTrackFileId(String value) {
        this.trackFileId = value;
    }

    /**
     * Gets the value of the keyId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKeyId() {
        return keyId;
    }

    /**
     * Sets the value of the keyId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKeyId(String value) {
        this.keyId = value;
    }

    /**
     * Gets the value of the hash property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getHash() {
        return hash;
    }

    /**
     * Sets the value of the hash property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setHash(byte[] value) {
        this.hash = ((byte[]) value);
    }

}
