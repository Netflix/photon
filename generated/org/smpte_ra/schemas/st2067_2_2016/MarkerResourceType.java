
package org.smpte_ra.schemas.st2067_2_2016;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MarkerResourceType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MarkerResourceType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.smpte-ra.org/schemas/2067-3/2016}BaseResourceType">
 *       &lt;sequence>
 *         &lt;element name="Marker" type="{http://www.smpte-ra.org/schemas/2067-3/2016}MarkerType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MarkerResourceType", namespace = "http://www.smpte-ra.org/schemas/2067-3/2016", propOrder = {
    "marker"
})
public class MarkerResourceType
    extends BaseResourceType
{

    @XmlElement(name = "Marker")
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
