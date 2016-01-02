
package org.smpte_ra.schemas.st2067_2_2013;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for LocaleType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LocaleType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Annotation" type="{http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/}UserTextType" minOccurs="0"/>
 *         &lt;element name="LanguageList" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="Language" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="RegionList" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="Region" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="ContentMaturityRatingList" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="ContentMaturityRating" type="{http://www.smpte-ra.org/schemas/2067-3/2013}ContentMaturityRatingType" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LocaleType", namespace = "http://www.smpte-ra.org/schemas/2067-3/2013", propOrder = {
    "annotation",
    "languageList",
    "regionList",
    "contentMaturityRatingList"
})
public class LocaleType {

    @XmlElement(name = "Annotation")
    protected UserTextType annotation;
    @XmlElement(name = "LanguageList")
    protected LocaleType.LanguageList languageList;
    @XmlElement(name = "RegionList")
    protected LocaleType.RegionList regionList;
    @XmlElement(name = "ContentMaturityRatingList")
    protected LocaleType.ContentMaturityRatingList contentMaturityRatingList;

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
     * Gets the value of the languageList property.
     * 
     * @return
     *     possible object is
     *     {@link LocaleType.LanguageList }
     *     
     */
    public LocaleType.LanguageList getLanguageList() {
        return languageList;
    }

    /**
     * Sets the value of the languageList property.
     * 
     * @param value
     *     allowed object is
     *     {@link LocaleType.LanguageList }
     *     
     */
    public void setLanguageList(LocaleType.LanguageList value) {
        this.languageList = value;
    }

    /**
     * Gets the value of the regionList property.
     * 
     * @return
     *     possible object is
     *     {@link LocaleType.RegionList }
     *     
     */
    public LocaleType.RegionList getRegionList() {
        return regionList;
    }

    /**
     * Sets the value of the regionList property.
     * 
     * @param value
     *     allowed object is
     *     {@link LocaleType.RegionList }
     *     
     */
    public void setRegionList(LocaleType.RegionList value) {
        this.regionList = value;
    }

    /**
     * Gets the value of the contentMaturityRatingList property.
     * 
     * @return
     *     possible object is
     *     {@link LocaleType.ContentMaturityRatingList }
     *     
     */
    public LocaleType.ContentMaturityRatingList getContentMaturityRatingList() {
        return contentMaturityRatingList;
    }

    /**
     * Sets the value of the contentMaturityRatingList property.
     * 
     * @param value
     *     allowed object is
     *     {@link LocaleType.ContentMaturityRatingList }
     *     
     */
    public void setContentMaturityRatingList(LocaleType.ContentMaturityRatingList value) {
        this.contentMaturityRatingList = value;
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
     *         &lt;element name="ContentMaturityRating" type="{http://www.smpte-ra.org/schemas/2067-3/2013}ContentMaturityRatingType" maxOccurs="unbounded"/>
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
        "contentMaturityRating"
    })
    public static class ContentMaturityRatingList {

        @XmlElement(name = "ContentMaturityRating", namespace = "http://www.smpte-ra.org/schemas/2067-3/2013", required = true)
        protected List<ContentMaturityRatingType> contentMaturityRating;

        /**
         * Gets the value of the contentMaturityRating property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the contentMaturityRating property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getContentMaturityRating().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link ContentMaturityRatingType }
         * 
         * 
         */
        public List<ContentMaturityRatingType> getContentMaturityRating() {
            if (contentMaturityRating == null) {
                contentMaturityRating = new ArrayList<ContentMaturityRatingType>();
            }
            return this.contentMaturityRating;
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
     *         &lt;element name="Language" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
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
        "language"
    })
    public static class LanguageList {

        @XmlElement(name = "Language", namespace = "http://www.smpte-ra.org/schemas/2067-3/2013", required = true)
        protected List<String> language;

        /**
         * Gets the value of the language property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the language property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getLanguage().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getLanguage() {
            if (language == null) {
                language = new ArrayList<String>();
            }
            return this.language;
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
     *         &lt;element name="Region" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
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
        "region"
    })
    public static class RegionList {

        @XmlElement(name = "Region", namespace = "http://www.smpte-ra.org/schemas/2067-3/2013", required = true)
        protected List<String> region;

        /**
         * Gets the value of the region property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the region property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getRegion().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getRegion() {
            if (region == null) {
                region = new ArrayList<String>();
            }
            return this.region;
        }

    }

}
