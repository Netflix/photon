
package org.smpte_ra.schemas.st2067_2_2013;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for rateType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="rateType">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/>RationalType">
 *       &lt;attribute name="eventscope" type="{http://www.w3.org/2001/XMLSchema}anyURI" default="http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/#rate-scope-tokens" />
 *       &lt;attribute name="event" type="{http://www.w3.org/2001/XMLSchema}token" />
 *       &lt;attribute name="period" use="required" type="{http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/}timeUnitToken" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "rateType", namespace = "http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/", propOrder = {
    "value"
})
public class RateType {

    @XmlValue
    protected List<Long> value;
    @XmlAttribute
    @XmlSchemaType(name = "anyURI")
    protected String eventscope;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String event;
    @XmlAttribute(required = true)
    protected TimeUnitToken period;

    /**
     * Gets the value of the value property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the value property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getValue().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     * 
     * 
     */
    public List<Long> getValue() {
        if (value == null) {
            value = new ArrayList<Long>();
        }
        return this.value;
    }

    /**
     * Gets the value of the eventscope property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEventscope() {
        if (eventscope == null) {
            return "http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/#rate-scope-tokens";
        } else {
            return eventscope;
        }
    }

    /**
     * Sets the value of the eventscope property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEventscope(String value) {
        this.eventscope = value;
    }

    /**
     * Gets the value of the event property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEvent() {
        return event;
    }

    /**
     * Sets the value of the event property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEvent(String value) {
        this.event = value;
    }

    /**
     * Gets the value of the period property.
     * 
     * @return
     *     possible object is
     *     {@link TimeUnitToken }
     *     
     */
    public TimeUnitToken getPeriod() {
        return period;
    }

    /**
     * Sets the value of the period property.
     * 
     * @param value
     *     allowed object is
     *     {@link TimeUnitToken }
     *     
     */
    public void setPeriod(TimeUnitToken value) {
        this.period = value;
    }

}
