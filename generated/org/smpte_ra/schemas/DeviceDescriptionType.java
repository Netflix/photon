
package org.smpte_ra.schemas;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for deviceDescriptionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="deviceDescriptionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DeviceIdentifier" type="{http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/}deviceIdentifierPolyType"/>
 *         &lt;element name="DeviceTypeID" type="{http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/}deviceTypeType"/>
 *         &lt;element name="DeviceSubsystemTypeID" type="{http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/}deviceTypeType" minOccurs="0"/>
 *         &lt;element name="AdditionalID" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/>
 *         &lt;element name="DeviceSerial" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ManufacturerID" type="{http://www.w3.org/2001/XMLSchema}anyURI" minOccurs="0"/>
 *         &lt;element name="ManufacturerCertID" type="{http://www.w3.org/2000/09/xmldsig#}DigestValueType" minOccurs="0"/>
 *         &lt;element name="DeviceCertID" type="{http://www.w3.org/2000/09/xmldsig#}DigestValueType" minOccurs="0"/>
 *         &lt;element name="ManufacturerName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DeviceName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ModelNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="VersionInfo" type="{http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/}versionInfoListType"/>
 *         &lt;element name="DeviceComment" type="{http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/}UserTextType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "deviceDescriptionType", namespace = "http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/", propOrder = {
    "deviceIdentifier",
    "deviceTypeID",
    "deviceSubsystemTypeID",
    "additionalID",
    "deviceSerial",
    "manufacturerID",
    "manufacturerCertID",
    "deviceCertID",
    "manufacturerName",
    "deviceName",
    "modelNumber",
    "versionInfo",
    "deviceComment"
})
public class DeviceDescriptionType {

    @XmlElement(name = "DeviceIdentifier", required = true)
    protected DeviceIdentifierPolyType deviceIdentifier;
    @XmlElement(name = "DeviceTypeID", required = true)
    protected DeviceTypeType deviceTypeID;
    @XmlElement(name = "DeviceSubsystemTypeID")
    protected DeviceTypeType deviceSubsystemTypeID;
    @XmlElement(name = "AdditionalID")
    protected Object additionalID;
    @XmlElement(name = "DeviceSerial", required = true)
    protected String deviceSerial;
    @XmlElement(name = "ManufacturerID")
    @XmlSchemaType(name = "anyURI")
    protected String manufacturerID;
    @XmlElement(name = "ManufacturerCertID")
    protected byte[] manufacturerCertID;
    @XmlElement(name = "DeviceCertID")
    protected byte[] deviceCertID;
    @XmlElement(name = "ManufacturerName")
    protected String manufacturerName;
    @XmlElement(name = "DeviceName")
    protected String deviceName;
    @XmlElement(name = "ModelNumber")
    protected String modelNumber;
    @XmlElement(name = "VersionInfo", required = true)
    protected VersionInfoListType versionInfo;
    @XmlElement(name = "DeviceComment")
    protected UserTextType deviceComment;

    /**
     * Gets the value of the deviceIdentifier property.
     * 
     * @return
     *     possible object is
     *     {@link DeviceIdentifierPolyType }
     *     
     */
    public DeviceIdentifierPolyType getDeviceIdentifier() {
        return deviceIdentifier;
    }

    /**
     * Sets the value of the deviceIdentifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link DeviceIdentifierPolyType }
     *     
     */
    public void setDeviceIdentifier(DeviceIdentifierPolyType value) {
        this.deviceIdentifier = value;
    }

    /**
     * Gets the value of the deviceTypeID property.
     * 
     * @return
     *     possible object is
     *     {@link DeviceTypeType }
     *     
     */
    public DeviceTypeType getDeviceTypeID() {
        return deviceTypeID;
    }

    /**
     * Sets the value of the deviceTypeID property.
     * 
     * @param value
     *     allowed object is
     *     {@link DeviceTypeType }
     *     
     */
    public void setDeviceTypeID(DeviceTypeType value) {
        this.deviceTypeID = value;
    }

    /**
     * Gets the value of the deviceSubsystemTypeID property.
     * 
     * @return
     *     possible object is
     *     {@link DeviceTypeType }
     *     
     */
    public DeviceTypeType getDeviceSubsystemTypeID() {
        return deviceSubsystemTypeID;
    }

    /**
     * Sets the value of the deviceSubsystemTypeID property.
     * 
     * @param value
     *     allowed object is
     *     {@link DeviceTypeType }
     *     
     */
    public void setDeviceSubsystemTypeID(DeviceTypeType value) {
        this.deviceSubsystemTypeID = value;
    }

    /**
     * Gets the value of the additionalID property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getAdditionalID() {
        return additionalID;
    }

    /**
     * Sets the value of the additionalID property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setAdditionalID(Object value) {
        this.additionalID = value;
    }

    /**
     * Gets the value of the deviceSerial property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDeviceSerial() {
        return deviceSerial;
    }

    /**
     * Sets the value of the deviceSerial property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDeviceSerial(String value) {
        this.deviceSerial = value;
    }

    /**
     * Gets the value of the manufacturerID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getManufacturerID() {
        return manufacturerID;
    }

    /**
     * Sets the value of the manufacturerID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setManufacturerID(String value) {
        this.manufacturerID = value;
    }

    /**
     * Gets the value of the manufacturerCertID property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getManufacturerCertID() {
        return manufacturerCertID;
    }

    /**
     * Sets the value of the manufacturerCertID property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setManufacturerCertID(byte[] value) {
        this.manufacturerCertID = ((byte[]) value);
    }

    /**
     * Gets the value of the deviceCertID property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getDeviceCertID() {
        return deviceCertID;
    }

    /**
     * Sets the value of the deviceCertID property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setDeviceCertID(byte[] value) {
        this.deviceCertID = ((byte[]) value);
    }

    /**
     * Gets the value of the manufacturerName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getManufacturerName() {
        return manufacturerName;
    }

    /**
     * Sets the value of the manufacturerName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setManufacturerName(String value) {
        this.manufacturerName = value;
    }

    /**
     * Gets the value of the deviceName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDeviceName() {
        return deviceName;
    }

    /**
     * Sets the value of the deviceName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDeviceName(String value) {
        this.deviceName = value;
    }

    /**
     * Gets the value of the modelNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getModelNumber() {
        return modelNumber;
    }

    /**
     * Sets the value of the modelNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setModelNumber(String value) {
        this.modelNumber = value;
    }

    /**
     * Gets the value of the versionInfo property.
     * 
     * @return
     *     possible object is
     *     {@link VersionInfoListType }
     *     
     */
    public VersionInfoListType getVersionInfo() {
        return versionInfo;
    }

    /**
     * Sets the value of the versionInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link VersionInfoListType }
     *     
     */
    public void setVersionInfo(VersionInfoListType value) {
        this.versionInfo = value;
    }

    /**
     * Gets the value of the deviceComment property.
     * 
     * @return
     *     possible object is
     *     {@link UserTextType }
     *     
     */
    public UserTextType getDeviceComment() {
        return deviceComment;
    }

    /**
     * Sets the value of the deviceComment property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserTextType }
     *     
     */
    public void setDeviceComment(UserTextType value) {
        this.deviceComment = value;
    }

}
