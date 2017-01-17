
package org.smpte_ra.schemas.st429_7_2006;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for TrackFileAssetType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TrackFileAssetType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.smpte-ra.org/schemas/429-7/2006/CPL}GenericAssetType">
 *       &lt;sequence>
 *         &lt;element name="KeyId" type="{http://www.smpte-ra.org/schemas/429-7/2006/CPL}UUID" minOccurs="0"/>
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
@XmlType(name = "TrackFileAssetType", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL", propOrder = {
    "keyId",
    "hash"
})
@XmlSeeAlso({
    PictureTrackFileAssetType.class,
    SubtitleTrackFileAssetType.class,
    SoundTrackFileAssetType.class
})
public abstract class TrackFileAssetType
    extends GenericAssetType
{

    @XmlElement(name = "KeyId", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL")
    @XmlSchemaType(name = "anyURI")
    protected String keyId;
    @XmlElement(name = "Hash", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL")
    protected byte[] hash;

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
        this.hash = value;
    }

}
