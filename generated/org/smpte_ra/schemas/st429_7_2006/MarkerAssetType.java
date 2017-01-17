
package org.smpte_ra.schemas.st429_7_2006;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MarkerAssetType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MarkerAssetType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.smpte-ra.org/schemas/429-7/2006/CPL}GenericAssetType">
 *       &lt;sequence>
 *         &lt;element name="MarkerList">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="Marker" type="{http://www.smpte-ra.org/schemas/429-7/2006/CPL}MarkerType" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MarkerAssetType", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL", propOrder = {
    "markerList"
})
public class MarkerAssetType
    extends GenericAssetType
{

    @XmlElement(name = "MarkerList", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL", required = true)
    protected MarkerAssetType.MarkerList markerList;

    /**
     * Gets the value of the markerList property.
     * 
     * @return
     *     possible object is
     *     {@link MarkerAssetType.MarkerList }
     *     
     */
    public MarkerAssetType.MarkerList getMarkerList() {
        return markerList;
    }

    /**
     * Sets the value of the markerList property.
     * 
     * @param value
     *     allowed object is
     *     {@link MarkerAssetType.MarkerList }
     *     
     */
    public void setMarkerList(MarkerAssetType.MarkerList value) {
        this.markerList = value;
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
     *         &lt;element name="Marker" type="{http://www.smpte-ra.org/schemas/429-7/2006/CPL}MarkerType" maxOccurs="unbounded" minOccurs="0"/>
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
        "marker"
    })
    public static class MarkerList {

        @XmlElement(name = "Marker", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL")
        protected List<MarkerType> marker;

        /**
         * Gets the value of the marker property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the marker property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getMarker().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link MarkerType }
         * 
         * 
         */
        public List<MarkerType> getMarker() {
            if (marker == null) {
                marker = new ArrayList<MarkerType>();
            }
            return this.marker;
        }

    }

}
