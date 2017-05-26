//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.05.03 at 02:05:08 AM PDT 
//


package org.smpte_ra.schemas._2067_101._2014.lanczos;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import org.smpte_ra.schemas._2067_101._2014.scale_macro.ScalingAlgorithmType;


/**
 * <p>Java class for Lanczos complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Lanczos">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.smpte-ra.org/schemas/2067-101/2014/scale-macro}ScalingAlgorithmType">
 *       &lt;attribute name="parameterA" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LanczosType")
public class LanczosType
    extends ScalingAlgorithmType
{

    @XmlAttribute(name = "parameterA")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger parameterA;

    /**
     * Gets the value of the parameterA property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getParameterA() {
        return parameterA;
    }

    /**
     * Sets the value of the parameterA property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setParameterA(BigInteger value) {
        this.parameterA = value;
    }

}