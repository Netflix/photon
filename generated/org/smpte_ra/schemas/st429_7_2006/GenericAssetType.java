
package org.smpte_ra.schemas.st429_7_2006;

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
 * <p>Java class for GenericAssetType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GenericAssetType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Id" type="{http://www.smpte-ra.org/schemas/429-7/2006/CPL}UUID"/>
 *         &lt;element name="AnnotationText" type="{http://www.smpte-ra.org/schemas/429-7/2006/CPL}UserText" minOccurs="0"/>
 *         &lt;element name="EditRate" type="{http://www.smpte-ra.org/schemas/429-7/2006/CPL}Rational"/>
 *         &lt;element name="IntrinsicDuration" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="EntryPoint" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="Duration" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GenericAssetType", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL", propOrder = {
    "id",
    "annotationText",
    "editRate",
    "intrinsicDuration",
    "entryPoint",
    "duration"
})
@XmlSeeAlso({
    TrackFileAssetType.class,
    MarkerAssetType.class
})
public abstract class GenericAssetType {

    @XmlElement(name = "Id", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL", required = true)
    @XmlSchemaType(name = "anyURI")
    protected String id;
    @XmlElement(name = "AnnotationText", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL")
    protected UserText annotationText;
    @XmlList
    @XmlElement(name = "EditRate", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL", type = Long.class)
    protected List<Long> editRate;
    @XmlElement(name = "IntrinsicDuration", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL")
    protected long intrinsicDuration;
    @XmlElement(name = "EntryPoint", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL")
    protected Long entryPoint;
    @XmlElement(name = "Duration", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL")
    protected Long duration;

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
     * Gets the value of the annotationText property.
     * 
     * @return
     *     possible object is
     *     {@link UserText }
     *     
     */
    public UserText getAnnotationText() {
        return annotationText;
    }

    /**
     * Sets the value of the annotationText property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserText }
     *     
     */
    public void setAnnotationText(UserText value) {
        this.annotationText = value;
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
     */
    public long getIntrinsicDuration() {
        return intrinsicDuration;
    }

    /**
     * Sets the value of the intrinsicDuration property.
     * 
     */
    public void setIntrinsicDuration(long value) {
        this.intrinsicDuration = value;
    }

    /**
     * Gets the value of the entryPoint property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getEntryPoint() {
        return entryPoint;
    }

    /**
     * Sets the value of the entryPoint property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setEntryPoint(Long value) {
        this.entryPoint = value;
    }

    /**
     * Gets the value of the duration property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getDuration() {
        return duration;
    }

    /**
     * Sets the value of the duration property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setDuration(Long value) {
        this.duration = value;
    }

}
