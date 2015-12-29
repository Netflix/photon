
package org.smpte_ra.schemas.st2067_2_2013;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for StereoImageTrackFileResourceType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="StereoImageTrackFileResourceType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.smpte-ra.org/schemas/2067-3/2013}BaseResourceType">
 *       &lt;sequence>
 *         &lt;element name="LeftEye" type="{http://www.smpte-ra.org/schemas/2067-3/2013}TrackFileResourceType"/>
 *         &lt;element name="RightEye" type="{http://www.smpte-ra.org/schemas/2067-3/2013}TrackFileResourceType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StereoImageTrackFileResourceType", namespace = "http://www.smpte-ra.org/schemas/2067-2/2013", propOrder = {
    "leftEye",
    "rightEye"
})
public class StereoImageTrackFileResourceType
    extends BaseResourceType
{

    @XmlElement(name = "LeftEye", required = true)
    protected TrackFileResourceType leftEye;
    @XmlElement(name = "RightEye", required = true)
    protected TrackFileResourceType rightEye;

    /**
     * Gets the value of the leftEye property.
     * 
     * @return
     *     possible object is
     *     {@link TrackFileResourceType }
     *     
     */
    public TrackFileResourceType getLeftEye() {
        return leftEye;
    }

    /**
     * Sets the value of the leftEye property.
     * 
     * @param value
     *     allowed object is
     *     {@link TrackFileResourceType }
     *     
     */
    public void setLeftEye(TrackFileResourceType value) {
        this.leftEye = value;
    }

    /**
     * Gets the value of the rightEye property.
     * 
     * @return
     *     possible object is
     *     {@link TrackFileResourceType }
     *     
     */
    public TrackFileResourceType getRightEye() {
        return rightEye;
    }

    /**
     * Sets the value of the rightEye property.
     * 
     * @param value
     *     allowed object is
     *     {@link TrackFileResourceType }
     *     
     */
    public void setRightEye(TrackFileResourceType value) {
        this.rightEye = value;
    }

}
