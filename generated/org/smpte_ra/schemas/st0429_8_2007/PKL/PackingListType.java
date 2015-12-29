
package org.smpte_ra.schemas.st0429_8_2007.PKL;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for PackingListType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PackingListType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Id" type="{http://www.smpte-ra.org/schemas/429-8/2007/PKL}UUID"/>
 *         &lt;element name="AnnotationText" type="{http://www.smpte-ra.org/schemas/429-8/2007/PKL}UserText" minOccurs="0"/>
 *         &lt;element name="IconId" type="{http://www.smpte-ra.org/schemas/429-8/2007/PKL}UUID" minOccurs="0"/>
 *         &lt;element name="IssueDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="Issuer" type="{http://www.smpte-ra.org/schemas/429-8/2007/PKL}UserText"/>
 *         &lt;element name="Creator" type="{http://www.smpte-ra.org/schemas/429-8/2007/PKL}UserText"/>
 *         &lt;element name="GroupId" type="{http://www.smpte-ra.org/schemas/429-8/2007/PKL}UUID" minOccurs="0"/>
 *         &lt;element name="AssetList">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="Asset" type="{http://www.smpte-ra.org/schemas/429-8/2007/PKL}AssetType" maxOccurs="unbounded"/>
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
@XmlType(name = "PackingListType", namespace = "http://www.smpte-ra.org/schemas/429-8/2007/PKL", propOrder = {
    "id",
    "annotationText",
    "iconId",
    "issueDate",
    "issuer",
    "creator",
    "groupId",
    "assetList",
    "signer",
    "signature"
})
public class PackingListType {

    @XmlElement(name = "Id", required = true)
    @XmlSchemaType(name = "anyURI")
    protected String id;
    @XmlElement(name = "AnnotationText")
    protected UserText annotationText;
    @XmlElement(name = "IconId")
    @XmlSchemaType(name = "anyURI")
    protected String iconId;
    @XmlElement(name = "IssueDate", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar issueDate;
    @XmlElement(name = "Issuer", required = true)
    protected UserText issuer;
    @XmlElement(name = "Creator", required = true)
    protected UserText creator;
    @XmlElement(name = "GroupId")
    @XmlSchemaType(name = "anyURI")
    protected String groupId;
    @XmlElement(name = "AssetList", required = true)
    protected PackingListType.AssetList assetList;
    @XmlElement(name = "Signer")
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
     * Gets the value of the groupId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Sets the value of the groupId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGroupId(String value) {
        this.groupId = value;
    }

    /**
     * Gets the value of the assetList property.
     * 
     * @return
     *     possible object is
     *     {@link PackingListType.AssetList }
     *     
     */
    public PackingListType.AssetList getAssetList() {
        return assetList;
    }

    /**
     * Sets the value of the assetList property.
     * 
     * @param value
     *     allowed object is
     *     {@link PackingListType.AssetList }
     *     
     */
    public void setAssetList(PackingListType.AssetList value) {
        this.assetList = value;
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
     *         &lt;element name="Asset" type="{http://www.smpte-ra.org/schemas/429-8/2007/PKL}AssetType" maxOccurs="unbounded"/>
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
        "asset"
    })
    public static class AssetList {

        @XmlElement(name = "Asset", namespace = "http://www.smpte-ra.org/schemas/429-8/2007/PKL", required = true)
        protected List<AssetType> asset;

        /**
         * Gets the value of the asset property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the asset property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getAsset().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link AssetType }
         * 
         * 
         */
        public List<AssetType> getAsset() {
            if (asset == null) {
                asset = new ArrayList<AssetType>();
            }
            return this.asset;
        }

    }

}
