
package org.smpte_ra.schemas.st429_7_2006;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for CompositionPlaylistType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CompositionPlaylistType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Id" type="{http://www.smpte-ra.org/schemas/429-7/2006/CPL}UUID"/>
 *         &lt;element name="AnnotationText" type="{http://www.smpte-ra.org/schemas/429-7/2006/CPL}UserText" minOccurs="0"/>
 *         &lt;element name="IconId" type="{http://www.smpte-ra.org/schemas/429-7/2006/CPL}UUID" minOccurs="0"/>
 *         &lt;element name="IssueDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="Issuer" type="{http://www.smpte-ra.org/schemas/429-7/2006/CPL}UserText" minOccurs="0"/>
 *         &lt;element name="Creator" type="{http://www.smpte-ra.org/schemas/429-7/2006/CPL}UserText" minOccurs="0"/>
 *         &lt;element name="ContentTitleText" type="{http://www.smpte-ra.org/schemas/429-7/2006/CPL}UserText"/>
 *         &lt;element name="ContentKind" type="{http://www.smpte-ra.org/schemas/429-7/2006/CPL}ContentKindType"/>
 *         &lt;element name="ContentVersion" type="{http://www.smpte-ra.org/schemas/429-7/2006/CPL}ContentVersionType"/>
 *         &lt;element name="RatingList">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="Rating" type="{http://www.smpte-ra.org/schemas/429-7/2006/CPL}RatingType" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="ReelList">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="Reel" type="{http://www.smpte-ra.org/schemas/429-7/2006/CPL}ReelType" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="Signer" type="{http://www.w3.org/2000/09/xmldsig#}KeyInfoType" minOccurs="0"/>
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}Signature" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CompositionPlaylistType", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL", propOrder = {
    "id",
    "annotationText",
    "iconId",
    "issueDate",
    "issuer",
    "creator",
    "contentTitleText",
    "contentKind",
    "contentVersion",
    "ratingList",
    "reelList",
    "signer",
    "signature"
})
public class CompositionPlaylistType {

    @XmlElement(name = "Id", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL", required = true)
    @XmlSchemaType(name = "anyURI")
    protected String id;
    @XmlElement(name = "AnnotationText", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL")
    protected UserText annotationText;
    @XmlElement(name = "IconId", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL")
    @XmlSchemaType(name = "anyURI")
    protected String iconId;
    @XmlElement(name = "IssueDate", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar issueDate;
    @XmlElement(name = "Issuer", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL")
    protected UserText issuer;
    @XmlElement(name = "Creator", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL")
    protected UserText creator;
    @XmlElement(name = "ContentTitleText", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL", required = true)
    protected UserText contentTitleText;
    @XmlElement(name = "ContentKind", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL", required = true)
    protected ContentKindType contentKind;
    @XmlElement(name = "ContentVersion", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL", required = true)
    protected ContentVersionType contentVersion;
    @XmlElement(name = "RatingList", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL", required = true)
    protected CompositionPlaylistType.RatingList ratingList;
    @XmlElement(name = "ReelList", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL", required = true)
    protected CompositionPlaylistType.ReelList reelList;
    @XmlElement(name = "Signer", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL")
    protected KeyInfoType signer;
    @XmlElement(name = "Signature", namespace = "http://www.w3.org/2000/09/xmldsig#")
    protected SignatureType signature;

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
     * Gets the value of the iconId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIconId() {
        return iconId;
    }

    /**
     * Sets the value of the iconId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIconId(String value) {
        this.iconId = value;
    }

    /**
     * Gets the value of the issueDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getIssueDate() {
        return issueDate;
    }

    /**
     * Sets the value of the issueDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setIssueDate(XMLGregorianCalendar value) {
        this.issueDate = value;
    }

    /**
     * Gets the value of the issuer property.
     * 
     * @return
     *     possible object is
     *     {@link UserText }
     *     
     */
    public UserText getIssuer() {
        return issuer;
    }

    /**
     * Sets the value of the issuer property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserText }
     *     
     */
    public void setIssuer(UserText value) {
        this.issuer = value;
    }

    /**
     * Gets the value of the creator property.
     * 
     * @return
     *     possible object is
     *     {@link UserText }
     *     
     */
    public UserText getCreator() {
        return creator;
    }

    /**
     * Sets the value of the creator property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserText }
     *     
     */
    public void setCreator(UserText value) {
        this.creator = value;
    }

    /**
     * Gets the value of the contentTitleText property.
     * 
     * @return
     *     possible object is
     *     {@link UserText }
     *     
     */
    public UserText getContentTitleText() {
        return contentTitleText;
    }

    /**
     * Sets the value of the contentTitleText property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserText }
     *     
     */
    public void setContentTitleText(UserText value) {
        this.contentTitleText = value;
    }

    /**
     * Gets the value of the contentKind property.
     * 
     * @return
     *     possible object is
     *     {@link ContentKindType }
     *     
     */
    public ContentKindType getContentKind() {
        return contentKind;
    }

    /**
     * Sets the value of the contentKind property.
     * 
     * @param value
     *     allowed object is
     *     {@link ContentKindType }
     *     
     */
    public void setContentKind(ContentKindType value) {
        this.contentKind = value;
    }

    /**
     * Gets the value of the contentVersion property.
     * 
     * @return
     *     possible object is
     *     {@link ContentVersionType }
     *     
     */
    public ContentVersionType getContentVersion() {
        return contentVersion;
    }

    /**
     * Sets the value of the contentVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link ContentVersionType }
     *     
     */
    public void setContentVersion(ContentVersionType value) {
        this.contentVersion = value;
    }

    /**
     * Gets the value of the ratingList property.
     * 
     * @return
     *     possible object is
     *     {@link CompositionPlaylistType.RatingList }
     *     
     */
    public CompositionPlaylistType.RatingList getRatingList() {
        return ratingList;
    }

    /**
     * Sets the value of the ratingList property.
     * 
     * @param value
     *     allowed object is
     *     {@link CompositionPlaylistType.RatingList }
     *     
     */
    public void setRatingList(CompositionPlaylistType.RatingList value) {
        this.ratingList = value;
    }

    /**
     * Gets the value of the reelList property.
     * 
     * @return
     *     possible object is
     *     {@link CompositionPlaylistType.ReelList }
     *     
     */
    public CompositionPlaylistType.ReelList getReelList() {
        return reelList;
    }

    /**
     * Sets the value of the reelList property.
     * 
     * @param value
     *     allowed object is
     *     {@link CompositionPlaylistType.ReelList }
     *     
     */
    public void setReelList(CompositionPlaylistType.ReelList value) {
        this.reelList = value;
    }

    /**
     * Gets the value of the signer property.
     * 
     * @return
     *     possible object is
     *     {@link KeyInfoType }
     *     
     */
    public KeyInfoType getSigner() {
        return signer;
    }

    /**
     * Sets the value of the signer property.
     * 
     * @param value
     *     allowed object is
     *     {@link KeyInfoType }
     *     
     */
    public void setSigner(KeyInfoType value) {
        this.signer = value;
    }

    /**
     * Gets the value of the signature property.
     * 
     * @return
     *     possible object is
     *     {@link SignatureType }
     *     
     */
    public SignatureType getSignature() {
        return signature;
    }

    /**
     * Sets the value of the signature property.
     * 
     * @param value
     *     allowed object is
     *     {@link SignatureType }
     *     
     */
    public void setSignature(SignatureType value) {
        this.signature = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="Rating" type="{http://www.smpte-ra.org/schemas/429-7/2006/CPL}RatingType" maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "rating"
    })
    public static class RatingList {

        @XmlElement(name = "Rating", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL")
        protected List<RatingType> rating;

        /**
         * Gets the value of the rating property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the rating property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getRating().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link RatingType }
         * 
         * 
         */
        public List<RatingType> getRating() {
            if (rating == null) {
                rating = new ArrayList<RatingType>();
            }
            return this.rating;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="Reel" type="{http://www.smpte-ra.org/schemas/429-7/2006/CPL}ReelType" maxOccurs="unbounded"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "reel"
    })
    public static class ReelList {

        @XmlElement(name = "Reel", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL", required = true)
        protected List<ReelType> reel;

        /**
         * Gets the value of the reel property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the reel property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getReel().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link ReelType }
         * 
         * 
         */
        public List<ReelType> getReel() {
            if (reel == null) {
                reel = new ArrayList<ReelType>();
            }
            return this.reel;
        }

    }

}
