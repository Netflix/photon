
package org.smpte_ra.schemas.st429_7_2006;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PictureTrackFileAssetType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PictureTrackFileAssetType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.smpte-ra.org/schemas/429-7/2006/CPL}TrackFileAssetType">
 *       &lt;sequence>
 *         &lt;element name="FrameRate" type="{http://www.smpte-ra.org/schemas/429-7/2006/CPL}Rational"/>
 *         &lt;element name="ScreenAspectRatio" type="{http://www.smpte-ra.org/schemas/429-7/2006/CPL}Rational"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PictureTrackFileAssetType", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL", propOrder = {
    "frameRate",
    "screenAspectRatio"
})
public class PictureTrackFileAssetType
    extends TrackFileAssetType
{

    @XmlList
    @XmlElement(name = "FrameRate", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL", type = Long.class)
    protected List<Long> frameRate;
    @XmlList
    @XmlElement(name = "ScreenAspectRatio", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL", type = Long.class)
    protected List<Long> screenAspectRatio;

    /**
     * Gets the value of the frameRate property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the frameRate property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFrameRate().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     * 
     * 
     */
    public List<Long> getFrameRate() {
        if (frameRate == null) {
            frameRate = new ArrayList<Long>();
        }
        return this.frameRate;
    }

    /**
     * Gets the value of the screenAspectRatio property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the screenAspectRatio property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getScreenAspectRatio().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     * 
     * 
     */
    public List<Long> getScreenAspectRatio() {
        if (screenAspectRatio == null) {
            screenAspectRatio = new ArrayList<Long>();
        }
        return this.screenAspectRatio;
    }

}
