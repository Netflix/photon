
package org.smpte_ra.schemas.st429_7_2006;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


/**
 * <p>Java class for MarkerType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MarkerType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Label">
 *           &lt;complexType>
 *             &lt;simpleContent>
 *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *                 &lt;attribute name="scope" type="{http://www.w3.org/2001/XMLSchema}anyURI" default="http://www.smpte-ra.org/schemas/429-7/2006/CPL#standard-markers" />
 *               &lt;/extension>
 *             &lt;/simpleContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="AnnotationText" type="{http://www.smpte-ra.org/schemas/429-7/2006/CPL}UserText" minOccurs="0"/>
 *         &lt;element name="Offset">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}long">
 *               &lt;minInclusive value="0"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
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
@XmlType(name = "MarkerType", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL", propOrder = {
    "label",
    "annotationText",
    "offset"
})
public class MarkerType {

    @XmlElement(name = "Label", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL", required = true)
    protected MarkerType.Label label;
    @XmlElement(name = "AnnotationText", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL")
    protected UserText annotationText;
    @XmlElement(name = "Offset", namespace = "http://www.smpte-ra.org/schemas/429-7/2006/CPL")
    protected long offset;

    /**
     * Gets the value of the label property.
     * 
     * @return
     *     possible object is
     *     {@link MarkerType.Label }
     *     
     */
    public MarkerType.Label getLabel() {
        return label;
    }

    /**
     * Sets the value of the label property.
     * 
     * @param value
     *     allowed object is
     *     {@link MarkerType.Label }
     *     
     */
    public void setLabel(MarkerType.Label value) {
        this.label = value;
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
     * Gets the value of the offset property.
     * 
     */
    public long getOffset() {
        return offset;
    }

    /**
     * Sets the value of the offset property.
     * 
     */
    public void setOffset(long value) {
        this.offset = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;simpleContent>
     *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
     *       &lt;attribute name="scope" type="{http://www.w3.org/2001/XMLSchema}anyURI" default="http://www.smpte-ra.org/schemas/429-7/2006/CPL#standard-markers" />
     *     &lt;/extension>
     *   &lt;/simpleContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class Label {

        @XmlValue
        protected String value;
        @XmlAttribute(name = "scope")
        @XmlSchemaType(name = "anyURI")
        protected String scope;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setValue(String value) {
            this.value = value;
        }

        /**
         * Gets the value of the scope property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getScope() {
            if (scope == null) {
                return "http://www.smpte-ra.org/schemas/429-7/2006/CPL#standard-markers";
            } else {
                return scope;
            }
        }

        /**
         * Sets the value of the scope property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setScope(String value) {
            this.scope = value;
        }

    }

}
