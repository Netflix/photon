
package org.smpte_ra.schemas;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BaseResourceType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BaseResourceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Id" type="{http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/}UUIDType"/>
 *         &lt;element name="Annotation" type="{http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/}UserTextType" minOccurs="0"/>
 *         &lt;element name="EditRate" type="{http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/}RationalType" minOccurs="0"/>
 *         &lt;element name="IntrinsicDuration" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger"/>
 *         &lt;element name="EntryPoint" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" minOccurs="0"/>
 *         &lt;element name="SourceDuration" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" minOccurs="0"/>
 *         &lt;element name="RepeatCount" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BaseResourceType", namespace = "http://www.smpte-ra.org/schemas/2067-3/2013", propOrder = {
    "id",
    "annotation",
    "editRate",
    "intrinsicDuration",
    "entryPoint",
    "sourceDuration",
    "repeatCount"
})
@XmlSeeAlso({
    StereoImageTrackFileResourceType.class,
    MarkerResourceType.class,
    TrackFileResourceType.class
})
public abstract class BaseResourceType {

    @XmlElement(name = "Id", required = true)
    protected String id;
    @XmlElement(name = "Annotation")
    protected UserTextType annotation;
    @XmlList
    @XmlElement(name = "EditRate", type = Long.class)
    protected List<Long> editRate;
    @XmlElement(name = "IntrinsicDuration", required = true)
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger intrinsicDuration;
    @XmlElement(name = "EntryPoint")
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger entryPoint;
    @XmlElement(name = "SourceDuration")
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger sourceDuration;
    @XmlElement(name = "RepeatCount")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger repeatCount;

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
     * Gets the value of the annotation property.
     * 
     * @return
     *     possible object is
     *     {@link UserTextType }
     *     
     */
    public UserTextType getAnnotation() {
        return annotation;
    }

    /**
     * Sets the value of the annotation property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserTextType }
     *     
     */
    public void setAnnotation(UserTextType value) {
        this.annotation = value;
    }

    /**
     * Gets the value of the editRate property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the editRate property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEditRate().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     * 
     * 
     */
    public List<Long> getEditRate() {
        if (editRate == null) {
            editRate = new ArrayList<Long>();
        }
        return this.editRate;
    }

    /**
     * Gets the value of the intrinsicDuration property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getIntrinsicDuration() {
        return intrinsicDuration;
    }

    /**
     * Sets the value of the intrinsicDuration property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setIntrinsicDuration(BigInteger value) {
        this.intrinsicDuration = value;
    }

    /**
     * Gets the value of the entryPoint property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getEntryPoint() {
        return entryPoint;
    }

    /**
     * Sets the value of the entryPoint property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setEntryPoint(BigInteger value) {
        this.entryPoint = value;
    }

    /**
     * Gets the value of the sourceDuration property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getSourceDuration() {
        return sourceDuration;
    }

    /**
     * Sets the value of the sourceDuration property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setSourceDuration(BigInteger value) {
        this.sourceDuration = value;
    }

    /**
     * Gets the value of the repeatCount property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getRepeatCount() {
        return repeatCount;
    }

    /**
     * Sets the value of the repeatCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setRepeatCount(BigInteger value) {
        this.repeatCount = value;
    }

}
