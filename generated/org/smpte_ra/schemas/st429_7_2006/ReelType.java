
package org.smpte_ra.schemas.st429_7_2006;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import org.w3c.dom.Element;


/**
 * <p>Java class for ReelType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ReelType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Id" type="{http://www.smpte-ra.org/schemas/429-7/2006/CPL}UUID"/>
 *         &lt;element name="AnnotationText" type="{http://www.smpte-ra.org/schemas/429-7/2006/CPL}UserText" minOccurs="0"/>
 *         &lt;element name="AssetList">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="MainMarkers" type="{http://www.smpte-ra.org/schemas/429-7/2006/CPL}MarkerAssetType" minOccurs="0"/>
 *                   &lt;element name="MainPicture" type="{http://www.smpte-ra.org/schemas/429-7/2006/CPL}PictureTrackFileAssetType" minOccurs="0"/>
 *                   &lt;element name="MainSound" type="{http://www.smpte-ra.org/schemas/429-7/2006/CPL}SoundTrackFileAssetType" minOccurs="0"/>
 *                   &lt;element name="MainSubtitle" type="{http://www.smpte-ra.org/schemas/429-7/2006/CPL}SubtitleTrackFileAssetType" minOccurs="0"/>
 *                   &lt;any processContents='lax' namespace='##other' maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "ReelType", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL", propOrder = {
    "id",
    "annotationText",
    "assetList"
})
public class ReelType {

    @XmlElement(name = "Id", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL", required = true)
    @XmlSchemaType(name = "anyURI")
    protected String id;
    @XmlElement(name = "AnnotationText", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL")
    protected UserText annotationText;
    @XmlElement(name = "AssetList", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL", required = true)
    protected ReelType.AssetList assetList;

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
     * Gets the value of the assetList property.
     * 
     * @return
     *     possible object is
     *     {@link ReelType.AssetList }
     *     
     */
    public ReelType.AssetList getAssetList() {
        return assetList;
    }

    /**
     * Sets the value of the assetList property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReelType.AssetList }
     *     
     */
    public void setAssetList(ReelType.AssetList value) {
        this.assetList = value;
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
     *         &lt;element name="MainMarkers" type="{http://www.smpte-ra.org/schemas/429-7/2006/CPL}MarkerAssetType" minOccurs="0"/>
     *         &lt;element name="MainPicture" type="{http://www.smpte-ra.org/schemas/429-7/2006/CPL}PictureTrackFileAssetType" minOccurs="0"/>
     *         &lt;element name="MainSound" type="{http://www.smpte-ra.org/schemas/429-7/2006/CPL}SoundTrackFileAssetType" minOccurs="0"/>
     *         &lt;element name="MainSubtitle" type="{http://www.smpte-ra.org/schemas/429-7/2006/CPL}SubtitleTrackFileAssetType" minOccurs="0"/>
     *         &lt;any processContents='lax' namespace='##other' maxOccurs="unbounded" minOccurs="0"/>
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
        "mainMarkers",
        "mainPicture",
        "mainSound",
        "mainSubtitle",
        "any"
    })
    public static class AssetList {

        @XmlElement(name = "MainMarkers", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL")
        protected MarkerAssetType mainMarkers;
        @XmlElement(name = "MainPicture", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL")
        protected PictureTrackFileAssetType mainPicture;
        @XmlElement(name = "MainSound", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL")
        protected SoundTrackFileAssetType mainSound;
        @XmlElement(name = "MainSubtitle", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL")
        protected SubtitleTrackFileAssetType mainSubtitle;
        @XmlAnyElement(lax = true)
        protected List<Object> any;

        /**
         * Gets the value of the mainMarkers property.
         * 
         * @return
         *     possible object is
         *     {@link MarkerAssetType }
         *     
         */
        public MarkerAssetType getMainMarkers() {
            return mainMarkers;
        }

        /**
         * Sets the value of the mainMarkers property.
         * 
         * @param value
         *     allowed object is
         *     {@link MarkerAssetType }
         *     
         */
        public void setMainMarkers(MarkerAssetType value) {
            this.mainMarkers = value;
        }

        /**
         * Gets the value of the mainPicture property.
         * 
         * @return
         *     possible object is
         *     {@link PictureTrackFileAssetType }
         *     
         */
        public PictureTrackFileAssetType getMainPicture() {
            return mainPicture;
        }

        /**
         * Sets the value of the mainPicture property.
         * 
         * @param value
         *     allowed object is
         *     {@link PictureTrackFileAssetType }
         *     
         */
        public void setMainPicture(PictureTrackFileAssetType value) {
            this.mainPicture = value;
        }

        /**
         * Gets the value of the mainSound property.
         * 
         * @return
         *     possible object is
         *     {@link SoundTrackFileAssetType }
         *     
         */
        public SoundTrackFileAssetType getMainSound() {
            return mainSound;
        }

        /**
         * Sets the value of the mainSound property.
         * 
         * @param value
         *     allowed object is
         *     {@link SoundTrackFileAssetType }
         *     
         */
        public void setMainSound(SoundTrackFileAssetType value) {
            this.mainSound = value;
        }

        /**
         * Gets the value of the mainSubtitle property.
         * 
         * @return
         *     possible object is
         *     {@link SubtitleTrackFileAssetType }
         *     
         */
        public SubtitleTrackFileAssetType getMainSubtitle() {
            return mainSubtitle;
        }

        /**
         * Sets the value of the mainSubtitle property.
         * 
         * @param value
         *     allowed object is
         *     {@link SubtitleTrackFileAssetType }
         *     
         */
        public void setMainSubtitle(SubtitleTrackFileAssetType value) {
            this.mainSubtitle = value;
        }

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
         * {@link Element }
         * {@link Object }
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

}
