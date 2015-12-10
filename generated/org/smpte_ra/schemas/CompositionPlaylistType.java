
package org.smpte_ra.schemas;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import org.w3c.dom.Element;


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
 *         &lt;element name="Id" type="{http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/}UUIDType"/>
 *         &lt;element name="Annotation" type="{http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/}UserTextType" minOccurs="0"/>
 *         &lt;element name="IssueDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="Issuer" type="{http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/}UserTextType" minOccurs="0"/>
 *         &lt;element name="Creator" type="{http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/}UserTextType" minOccurs="0"/>
 *         &lt;element name="ContentOriginator" type="{http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/}UserTextType" minOccurs="0"/>
 *         &lt;element name="ContentTitle" type="{http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/}UserTextType"/>
 *         &lt;element name="ContentKind" type="{http://www.smpte-ra.org/schemas/2067-3/2013}ContentKindType" minOccurs="0"/>
 *         &lt;element name="ContentVersionList" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="ContentVersion" type="{http://www.smpte-ra.org/schemas/2067-3/2013}ContentVersionType" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="EssenceDescriptorList" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="EssenceDescriptor" type="{http://www.smpte-ra.org/schemas/2067-3/2013}EssenceDescriptorBaseType" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="CompositionTimecode" type="{http://www.smpte-ra.org/schemas/2067-3/2013}CompositionTimecodeType" minOccurs="0"/>
 *         &lt;element name="EditRate" type="{http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/}RationalType"/>
 *         &lt;element name="TotalRunningTime" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;pattern value="[0-9][0-9]:[0-5][0-9]:[0-5][0-9]"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="LocaleList" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="Locale" type="{http://www.smpte-ra.org/schemas/2067-3/2013}LocaleType" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="ExtensionProperties" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;any processContents='lax' namespace='##other' maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="SegmentList">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="Segment" type="{http://www.smpte-ra.org/schemas/2067-3/2013}SegmentType" maxOccurs="unbounded"/>
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
@XmlType(name = "CompositionPlaylistType", namespace = "http://www.smpte-ra.org/schemas/2067-3/2013", propOrder = {
    "id",
    "annotation",
    "issueDate",
    "issuer",
    "creator",
    "contentOriginator",
    "contentTitle",
    "contentKind",
    "contentVersionList",
    "essenceDescriptorList",
    "compositionTimecode",
    "editRate",
    "totalRunningTime",
    "localeList",
    "extensionProperties",
    "segmentList",
    "signer",
    "signature"
})
public class CompositionPlaylistType {

    @XmlElement(name = "Id", required = true)
    protected String id;
    @XmlElement(name = "Annotation")
    protected UserTextType annotation;
    @XmlElement(name = "IssueDate", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar issueDate;
    @XmlElement(name = "Issuer")
    protected UserTextType issuer;
    @XmlElement(name = "Creator")
    protected UserTextType creator;
    @XmlElement(name = "ContentOriginator")
    protected UserTextType contentOriginator;
    @XmlElement(name = "ContentTitle", required = true)
    protected UserTextType contentTitle;
    @XmlElement(name = "ContentKind")
    protected ContentKindType contentKind;
    @XmlElement(name = "ContentVersionList")
    protected CompositionPlaylistType.ContentVersionList contentVersionList;
    @XmlElement(name = "EssenceDescriptorList")
    protected CompositionPlaylistType.EssenceDescriptorList essenceDescriptorList;
    @XmlElement(name = "CompositionTimecode")
    protected CompositionTimecodeType compositionTimecode;
    @XmlList
    @XmlElement(name = "EditRate", type = Long.class)
    protected List<Long> editRate;
    @XmlElement(name = "TotalRunningTime")
    protected String totalRunningTime;
    @XmlElement(name = "LocaleList")
    protected CompositionPlaylistType.LocaleList localeList;
    @XmlElement(name = "ExtensionProperties")
    protected CompositionPlaylistType.ExtensionProperties extensionProperties;
    @XmlElement(name = "SegmentList", required = true)
    protected CompositionPlaylistType.SegmentList segmentList;
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
     *     {@link UserTextType }
     *     
     */
    public UserTextType getIssuer() {
        return issuer;
    }

    /**
     * Sets the value of the issuer property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserTextType }
     *     
     */
    public void setIssuer(UserTextType value) {
        this.issuer = value;
    }

    /**
     * Gets the value of the creator property.
     * 
     * @return
     *     possible object is
     *     {@link UserTextType }
     *     
     */
    public UserTextType getCreator() {
        return creator;
    }

    /**
     * Sets the value of the creator property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserTextType }
     *     
     */
    public void setCreator(UserTextType value) {
        this.creator = value;
    }

    /**
     * Gets the value of the contentOriginator property.
     * 
     * @return
     *     possible object is
     *     {@link UserTextType }
     *     
     */
    public UserTextType getContentOriginator() {
        return contentOriginator;
    }

    /**
     * Sets the value of the contentOriginator property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserTextType }
     *     
     */
    public void setContentOriginator(UserTextType value) {
        this.contentOriginator = value;
    }

    /**
     * Gets the value of the contentTitle property.
     * 
     * @return
     *     possible object is
     *     {@link UserTextType }
     *     
     */
    public UserTextType getContentTitle() {
        return contentTitle;
    }

    /**
     * Sets the value of the contentTitle property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserTextType }
     *     
     */
    public void setContentTitle(UserTextType value) {
        this.contentTitle = value;
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
     * Gets the value of the contentVersionList property.
     * 
     * @return
     *     possible object is
     *     {@link CompositionPlaylistType.ContentVersionList }
     *     
     */
    public CompositionPlaylistType.ContentVersionList getContentVersionList() {
        return contentVersionList;
    }

    /**
     * Sets the value of the contentVersionList property.
     * 
     * @param value
     *     allowed object is
     *     {@link CompositionPlaylistType.ContentVersionList }
     *     
     */
    public void setContentVersionList(CompositionPlaylistType.ContentVersionList value) {
        this.contentVersionList = value;
    }

    /**
     * Gets the value of the essenceDescriptorList property.
     * 
     * @return
     *     possible object is
     *     {@link CompositionPlaylistType.EssenceDescriptorList }
     *     
     */
    public CompositionPlaylistType.EssenceDescriptorList getEssenceDescriptorList() {
        return essenceDescriptorList;
    }

    /**
     * Sets the value of the essenceDescriptorList property.
     * 
     * @param value
     *     allowed object is
     *     {@link CompositionPlaylistType.EssenceDescriptorList }
     *     
     */
    public void setEssenceDescriptorList(CompositionPlaylistType.EssenceDescriptorList value) {
        this.essenceDescriptorList = value;
    }

    /**
     * Gets the value of the compositionTimecode property.
     * 
     * @return
     *     possible object is
     *     {@link CompositionTimecodeType }
     *     
     */
    public CompositionTimecodeType getCompositionTimecode() {
        return compositionTimecode;
    }

    /**
     * Sets the value of the compositionTimecode property.
     * 
     * @param value
     *     allowed object is
     *     {@link CompositionTimecodeType }
     *     
     */
    public void setCompositionTimecode(CompositionTimecodeType value) {
        this.compositionTimecode = value;
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
     * Gets the value of the totalRunningTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTotalRunningTime() {
        return totalRunningTime;
    }

    /**
     * Sets the value of the totalRunningTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTotalRunningTime(String value) {
        this.totalRunningTime = value;
    }

    /**
     * Gets the value of the localeList property.
     * 
     * @return
     *     possible object is
     *     {@link CompositionPlaylistType.LocaleList }
     *     
     */
    public CompositionPlaylistType.LocaleList getLocaleList() {
        return localeList;
    }

    /**
     * Sets the value of the localeList property.
     * 
     * @param value
     *     allowed object is
     *     {@link CompositionPlaylistType.LocaleList }
     *     
     */
    public void setLocaleList(CompositionPlaylistType.LocaleList value) {
        this.localeList = value;
    }

    /**
     * Gets the value of the extensionProperties property.
     * 
     * @return
     *     possible object is
     *     {@link CompositionPlaylistType.ExtensionProperties }
     *     
     */
    public CompositionPlaylistType.ExtensionProperties getExtensionProperties() {
        return extensionProperties;
    }

    /**
     * Sets the value of the extensionProperties property.
     * 
     * @param value
     *     allowed object is
     *     {@link CompositionPlaylistType.ExtensionProperties }
     *     
     */
    public void setExtensionProperties(CompositionPlaylistType.ExtensionProperties value) {
        this.extensionProperties = value;
    }

    /**
     * Gets the value of the segmentList property.
     * 
     * @return
     *     possible object is
     *     {@link CompositionPlaylistType.SegmentList }
     *     
     */
    public CompositionPlaylistType.SegmentList getSegmentList() {
        return segmentList;
    }

    /**
     * Sets the value of the segmentList property.
     * 
     * @param value
     *     allowed object is
     *     {@link CompositionPlaylistType.SegmentList }
     *     
     */
    public void setSegmentList(CompositionPlaylistType.SegmentList value) {
        this.segmentList = value;
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
     *         &lt;element name="ContentVersion" type="{http://www.smpte-ra.org/schemas/2067-3/2013}ContentVersionType" maxOccurs="unbounded"/>
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
        "contentVersion"
    })
    public static class ContentVersionList {

        @XmlElement(name = "ContentVersion", namespace = "http://www.smpte-ra.org/schemas/2067-3/2013", required = true)
        protected List<ContentVersionType> contentVersion;

        /**
         * Gets the value of the contentVersion property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the contentVersion property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getContentVersion().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link ContentVersionType }
         * 
         * 
         */
        public List<ContentVersionType> getContentVersion() {
            if (contentVersion == null) {
                contentVersion = new ArrayList<ContentVersionType>();
            }
            return this.contentVersion;
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
     *         &lt;element name="EssenceDescriptor" type="{http://www.smpte-ra.org/schemas/2067-3/2013}EssenceDescriptorBaseType" maxOccurs="unbounded"/>
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
        "essenceDescriptor"
    })
    public static class EssenceDescriptorList {

        @XmlElement(name = "EssenceDescriptor", namespace = "http://www.smpte-ra.org/schemas/2067-3/2013", required = true)
        protected List<EssenceDescriptorBaseType> essenceDescriptor;

        /**
         * Gets the value of the essenceDescriptor property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the essenceDescriptor property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getEssenceDescriptor().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link EssenceDescriptorBaseType }
         * 
         * 
         */
        public List<EssenceDescriptorBaseType> getEssenceDescriptor() {
            if (essenceDescriptor == null) {
                essenceDescriptor = new ArrayList<EssenceDescriptorBaseType>();
            }
            return this.essenceDescriptor;
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
     *         &lt;any processContents='lax' namespace='##other' maxOccurs="unbounded"/>
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
        "any"
    })
    public static class ExtensionProperties {

        @XmlAnyElement(lax = true)
        protected List<Object> any;

        /**
         * Gets the value of the any property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the any property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getAny().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Object }
         * {@link Element }
         * 
         * 
         */
        public List<Object> getAny() {
            if (any == null) {
                any = new ArrayList<Object>();
            }
            return this.any;
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
     *         &lt;element name="Locale" type="{http://www.smpte-ra.org/schemas/2067-3/2013}LocaleType" maxOccurs="unbounded"/>
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
        "locale"
    })
    public static class LocaleList {

        @XmlElement(name = "Locale", namespace = "http://www.smpte-ra.org/schemas/2067-3/2013", required = true)
        protected List<LocaleType> locale;

        /**
         * Gets the value of the locale property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the locale property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getLocale().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link LocaleType }
         * 
         * 
         */
        public List<LocaleType> getLocale() {
            if (locale == null) {
                locale = new ArrayList<LocaleType>();
            }
            return this.locale;
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
     *         &lt;element name="Segment" type="{http://www.smpte-ra.org/schemas/2067-3/2013}SegmentType" maxOccurs="unbounded"/>
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
        "segment"
    })
    public static class SegmentList {

        @XmlElement(name = "Segment", namespace = "http://www.smpte-ra.org/schemas/2067-3/2013", required = true)
        protected List<SegmentType> segment;

        /**
         * Gets the value of the segment property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the segment property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getSegment().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link SegmentType }
         * 
         * 
         */
        public List<SegmentType> getSegment() {
            if (segment == null) {
                segment = new ArrayList<SegmentType>();
            }
            return this.segment;
        }

    }

}
