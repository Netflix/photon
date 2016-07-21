
package org.smpte_ra.schemas.st2067_2_2016;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for versionInfoListType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="versionInfoListType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/}strstrNameValueGroup" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "versionInfoListType", namespace = "http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/", propOrder = {
    "strstrNameValueGroup"
})
public class VersionInfoListType {

    @XmlElementRefs({
        @XmlElementRef(name = "Name", namespace = "http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/", type = JAXBElement.class),
        @XmlElementRef(name = "Value", namespace = "http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/", type = JAXBElement.class)
    })
    protected List<JAXBElement<String>> strstrNameValueGroup;

    /**
     * Gets the value of the strstrNameValueGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the strstrNameValueGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStrstrNameValueGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * 
     */
    public List<JAXBElement<String>> getStrstrNameValueGroup() {
        if (strstrNameValueGroup == null) {
            strstrNameValueGroup = new ArrayList<JAXBElement<String>>();
        }
        return this.strstrNameValueGroup;
    }

}
