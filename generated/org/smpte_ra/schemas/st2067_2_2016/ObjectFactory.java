
package org.smpte_ra.schemas.st2067_2_2016;

import java.math.BigInteger;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.smpte_ra.schemas.st2067_2_2016 package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _VisuallyImpairedTextSequence_QNAME = new QName("http://www.smpte-ra.org/schemas/2067-2/2016", "VisuallyImpairedTextSequence");
    private final static QName _SPKIData_QNAME = new QName("http://www.w3.org/2000/09/xmldsig#", "SPKIData");
    private final static QName _TimedTextResourceID_QNAME = new QName("http://www.smpte-ra.org/schemas/2067-2/2016", "TimedTextResourceID");
    private final static QName _DeviceIdentifierList_QNAME = new QName("http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/", "DeviceIdentifierList");
    private final static QName _KeyInfo_QNAME = new QName("http://www.w3.org/2000/09/xmldsig#", "KeyInfo");
    private final static QName _SignatureValue_QNAME = new QName("http://www.w3.org/2000/09/xmldsig#", "SignatureValue");
    private final static QName _SubtitlesSequence_QNAME = new QName("http://www.smpte-ra.org/schemas/2067-2/2016", "SubtitlesSequence");
    private final static QName _CompositionPlaylist_QNAME = new QName("http://www.smpte-ra.org/schemas/2067-3/2016", "CompositionPlaylist");
    private final static QName _KeyValue_QNAME = new QName("http://www.w3.org/2000/09/xmldsig#", "KeyValue");
    private final static QName _Transforms_QNAME = new QName("http://www.w3.org/2000/09/xmldsig#", "Transforms");
    private final static QName _DigestMethod_QNAME = new QName("http://www.w3.org/2000/09/xmldsig#", "DigestMethod");
    private final static QName _ApplicationIdentification_QNAME = new QName("http://www.smpte-ra.org/schemas/2067-2/2016", "ApplicationIdentification");
    private final static QName _DeviceDescription_QNAME = new QName("http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/", "DeviceDescription");
    private final static QName _X509Data_QNAME = new QName("http://www.w3.org/2000/09/xmldsig#", "X509Data");
    private final static QName _AncillaryDataSequence_QNAME = new QName("http://www.smpte-ra.org/schemas/2067-2/2016", "AncillaryDataSequence");
    private final static QName _SignatureProperty_QNAME = new QName("http://www.w3.org/2000/09/xmldsig#", "SignatureProperty");
    private final static QName _KaraokeSequence_QNAME = new QName("http://www.smpte-ra.org/schemas/2067-2/2016", "KaraokeSequence");
    private final static QName _MainImageSequence_QNAME = new QName("http://www.smpte-ra.org/schemas/2067-2/2016", "MainImageSequence");
    private final static QName _KeyName_QNAME = new QName("http://www.w3.org/2000/09/xmldsig#", "KeyName");
    private final static QName _RSAKeyValue_QNAME = new QName("http://www.w3.org/2000/09/xmldsig#", "RSAKeyValue");
    private final static QName _Signature_QNAME = new QName("http://www.w3.org/2000/09/xmldsig#", "Signature");
    private final static QName _CommentarySequence_QNAME = new QName("http://www.smpte-ra.org/schemas/2067-2/2016", "CommentarySequence");
    private final static QName _MgmtData_QNAME = new QName("http://www.w3.org/2000/09/xmldsig#", "MgmtData");
    private final static QName _SignatureMethod_QNAME = new QName("http://www.w3.org/2000/09/xmldsig#", "SignatureMethod");
    private final static QName _Object_QNAME = new QName("http://www.w3.org/2000/09/xmldsig#", "Object");
    private final static QName _SignatureProperties_QNAME = new QName("http://www.w3.org/2000/09/xmldsig#", "SignatureProperties");
    private final static QName _Transform_QNAME = new QName("http://www.w3.org/2000/09/xmldsig#", "Transform");
    private final static QName _MainAudioSequence_QNAME = new QName("http://www.smpte-ra.org/schemas/2067-2/2016", "MainAudioSequence");
    private final static QName _HearingImpairedCaptionsSequence_QNAME = new QName("http://www.smpte-ra.org/schemas/2067-2/2016", "HearingImpairedCaptionsSequence");
    private final static QName _PGPData_QNAME = new QName("http://www.w3.org/2000/09/xmldsig#", "PGPData");
    private final static QName _Reference_QNAME = new QName("http://www.w3.org/2000/09/xmldsig#", "Reference");
    private final static QName _RetrievalMethod_QNAME = new QName("http://www.w3.org/2000/09/xmldsig#", "RetrievalMethod");
    private final static QName _DSAKeyValue_QNAME = new QName("http://www.w3.org/2000/09/xmldsig#", "DSAKeyValue");
    private final static QName _DigestValue_QNAME = new QName("http://www.w3.org/2000/09/xmldsig#", "DigestValue");
    private final static QName _CanonicalizationMethod_QNAME = new QName("http://www.w3.org/2000/09/xmldsig#", "CanonicalizationMethod");
    private final static QName _SignedInfo_QNAME = new QName("http://www.w3.org/2000/09/xmldsig#", "SignedInfo");
    private final static QName _Manifest_QNAME = new QName("http://www.w3.org/2000/09/xmldsig#", "Manifest");
    private final static QName _SPKIDataTypeSPKISexp_QNAME = new QName("http://www.w3.org/2000/09/xmldsig#", "SPKISexp");
    private final static QName _SignatureMethodTypeHMACOutputLength_QNAME = new QName("http://www.w3.org/2000/09/xmldsig#", "HMACOutputLength");
    private final static QName _PGPDataTypePGPKeyID_QNAME = new QName("http://www.w3.org/2000/09/xmldsig#", "PGPKeyID");
    private final static QName _PGPDataTypePGPKeyPacket_QNAME = new QName("http://www.w3.org/2000/09/xmldsig#", "PGPKeyPacket");
    private final static QName _X509DataTypeX509IssuerSerial_QNAME = new QName("http://www.w3.org/2000/09/xmldsig#", "X509IssuerSerial");
    private final static QName _X509DataTypeX509CRL_QNAME = new QName("http://www.w3.org/2000/09/xmldsig#", "X509CRL");
    private final static QName _X509DataTypeX509SubjectName_QNAME = new QName("http://www.w3.org/2000/09/xmldsig#", "X509SubjectName");
    private final static QName _X509DataTypeX509SKI_QNAME = new QName("http://www.w3.org/2000/09/xmldsig#", "X509SKI");
    private final static QName _X509DataTypeX509Certificate_QNAME = new QName("http://www.w3.org/2000/09/xmldsig#", "X509Certificate");
    private final static QName _VersionInfoListTypeName_QNAME = new QName("http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/", "Name");
    private final static QName _VersionInfoListTypeValue_QNAME = new QName("http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/", "Value");
    private final static QName _TransformTypeXPath_QNAME = new QName("http://www.w3.org/2000/09/xmldsig#", "XPath");
    private final static QName _IABSequence_QNAME = new QName("http://www.smpte-ra.org/ns/2067-201/2019", "IABSequence");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.smpte_ra.schemas.st2067_2_2016
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link SequenceType }
     * 
     */
    public SequenceType createSequenceType() {
        return new SequenceType();
    }

    /**
     * Create an instance of {@link ContentMaturityRatingType }
     * 
     */
    public ContentMaturityRatingType createContentMaturityRatingType() {
        return new ContentMaturityRatingType();
    }

    /**
     * Create an instance of {@link MarkerType }
     * 
     */
    public MarkerType createMarkerType() {
        return new MarkerType();
    }

    /**
     * Create an instance of {@link LocaleType }
     * 
     */
    public LocaleType createLocaleType() {
        return new LocaleType();
    }

    /**
     * Create an instance of {@link SegmentType }
     * 
     */
    public SegmentType createSegmentType() {
        return new SegmentType();
    }

    /**
     * Create an instance of {@link CompositionPlaylistType }
     * 
     */
    public CompositionPlaylistType createCompositionPlaylistType() {
        return new CompositionPlaylistType();
    }

    /**
     * Create an instance of {@link CDPSequence }
     * 
     */
    public CDPSequence createCDPSequence() {
        return new CDPSequence();
    }

    /**
     * Create an instance of {@link SequenceType.ResourceList }
     * 
     */
    public SequenceType.ResourceList createSequenceTypeResourceList() {
        return new SequenceType.ResourceList();
    }

    /**
     * Create an instance of {@link StereoImageTrackFileResourceType }
     * 
     */
    public StereoImageTrackFileResourceType createStereoImageTrackFileResourceType() {
        return new StereoImageTrackFileResourceType();
    }

    /**
     * Create an instance of {@link MarkerResourceType }
     * 
     */
    public MarkerResourceType createMarkerResourceType() {
        return new MarkerResourceType();
    }

    /**
     * Create an instance of {@link ContentVersionType }
     * 
     */
    public ContentVersionType createContentVersionType() {
        return new ContentVersionType();
    }

    /**
     * Create an instance of {@link TrackFileResourceType }
     * 
     */
    public TrackFileResourceType createTrackFileResourceType() {
        return new TrackFileResourceType();
    }

    /**
     * Create an instance of {@link EssenceDescriptorBaseType }
     * 
     */
    public EssenceDescriptorBaseType createEssenceDescriptorBaseType() {
        return new EssenceDescriptorBaseType();
    }

    /**
     * Create an instance of {@link ContentKindType }
     * 
     */
    public ContentKindType createContentKindType() {
        return new ContentKindType();
    }

    /**
     * Create an instance of {@link CompositionTimecodeType }
     * 
     */
    public CompositionTimecodeType createCompositionTimecodeType() {
        return new CompositionTimecodeType();
    }

    /**
     * Create an instance of {@link DeviceDescriptionType }
     * 
     */
    public DeviceDescriptionType createDeviceDescriptionType() {
        return new DeviceDescriptionType();
    }

    /**
     * Create an instance of {@link DeviceIdentifierListType }
     * 
     */
    public DeviceIdentifierListType createDeviceIdentifierListType() {
        return new DeviceIdentifierListType();
    }

    /**
     * Create an instance of {@link UserTextType }
     * 
     */
    public UserTextType createUserTextType() {
        return new UserTextType();
    }

    /**
     * Create an instance of {@link TemperatureType }
     * 
     */
    public TemperatureType createTemperatureType() {
        return new TemperatureType();
    }

    /**
     * Create an instance of {@link NamedParmType }
     * 
     */
    public NamedParmType createNamedParmType() {
        return new NamedParmType();
    }

    /**
     * Create an instance of {@link CurrentType }
     * 
     */
    public CurrentType createCurrentType() {
        return new CurrentType();
    }

    /**
     * Create an instance of {@link ScopedTokenType }
     * 
     */
    public ScopedTokenType createScopedTokenType() {
        return new ScopedTokenType();
    }

    /**
     * Create an instance of {@link DurationType }
     * 
     */
    public DurationType createDurationType() {
        return new DurationType();
    }

    /**
     * Create an instance of {@link RateType }
     * 
     */
    public RateType createRateType() {
        return new RateType();
    }

    /**
     * Create an instance of {@link VoltageType }
     * 
     */
    public VoltageType createVoltageType() {
        return new VoltageType();
    }

    /**
     * Create an instance of {@link ParameterListType }
     * 
     */
    public ParameterListType createParameterListType() {
        return new ParameterListType();
    }

    /**
     * Create an instance of {@link VersionInfoListType }
     * 
     */
    public VersionInfoListType createVersionInfoListType() {
        return new VersionInfoListType();
    }

    /**
     * Create an instance of {@link DeviceIdentifierPolyType }
     * 
     */
    public DeviceIdentifierPolyType createDeviceIdentifierPolyType() {
        return new DeviceIdentifierPolyType();
    }

    /**
     * Create an instance of {@link DeviceTypeType }
     * 
     */
    public DeviceTypeType createDeviceTypeType() {
        return new DeviceTypeType();
    }

    /**
     * Create an instance of {@link PGPDataType }
     * 
     */
    public PGPDataType createPGPDataType() {
        return new PGPDataType();
    }

    /**
     * Create an instance of {@link KeyValueType }
     * 
     */
    public KeyValueType createKeyValueType() {
        return new KeyValueType();
    }

    /**
     * Create an instance of {@link DSAKeyValueType }
     * 
     */
    public DSAKeyValueType createDSAKeyValueType() {
        return new DSAKeyValueType();
    }

    /**
     * Create an instance of {@link ReferenceType }
     * 
     */
    public ReferenceType createReferenceType() {
        return new ReferenceType();
    }

    /**
     * Create an instance of {@link RetrievalMethodType }
     * 
     */
    public RetrievalMethodType createRetrievalMethodType() {
        return new RetrievalMethodType();
    }

    /**
     * Create an instance of {@link TransformsType }
     * 
     */
    public TransformsType createTransformsType() {
        return new TransformsType();
    }

    /**
     * Create an instance of {@link CanonicalizationMethodType }
     * 
     */
    public CanonicalizationMethodType createCanonicalizationMethodType() {
        return new CanonicalizationMethodType();
    }

    /**
     * Create an instance of {@link DigestMethodType }
     * 
     */
    public DigestMethodType createDigestMethodType() {
        return new DigestMethodType();
    }

    /**
     * Create an instance of {@link ManifestType }
     * 
     */
    public ManifestType createManifestType() {
        return new ManifestType();
    }

    /**
     * Create an instance of {@link SignaturePropertyType }
     * 
     */
    public SignaturePropertyType createSignaturePropertyType() {
        return new SignaturePropertyType();
    }

    /**
     * Create an instance of {@link X509DataType }
     * 
     */
    public X509DataType createX509DataType() {
        return new X509DataType();
    }

    /**
     * Create an instance of {@link SignedInfoType }
     * 
     */
    public SignedInfoType createSignedInfoType() {
        return new SignedInfoType();
    }

    /**
     * Create an instance of {@link RSAKeyValueType }
     * 
     */
    public RSAKeyValueType createRSAKeyValueType() {
        return new RSAKeyValueType();
    }

    /**
     * Create an instance of {@link SPKIDataType }
     * 
     */
    public SPKIDataType createSPKIDataType() {
        return new SPKIDataType();
    }

    /**
     * Create an instance of {@link SignatureValueType }
     * 
     */
    public SignatureValueType createSignatureValueType() {
        return new SignatureValueType();
    }

    /**
     * Create an instance of {@link KeyInfoType }
     * 
     */
    public KeyInfoType createKeyInfoType() {
        return new KeyInfoType();
    }

    /**
     * Create an instance of {@link SignatureType }
     * 
     */
    public SignatureType createSignatureType() {
        return new SignatureType();
    }

    /**
     * Create an instance of {@link SignaturePropertiesType }
     * 
     */
    public SignaturePropertiesType createSignaturePropertiesType() {
        return new SignaturePropertiesType();
    }

    /**
     * Create an instance of {@link SignatureMethodType }
     * 
     */
    public SignatureMethodType createSignatureMethodType() {
        return new SignatureMethodType();
    }

    /**
     * Create an instance of {@link ObjectType }
     * 
     */
    public ObjectType createObjectType() {
        return new ObjectType();
    }

    /**
     * Create an instance of {@link TransformType }
     * 
     */
    public TransformType createTransformType() {
        return new TransformType();
    }

    /**
     * Create an instance of {@link X509IssuerSerialType }
     * 
     */
    public X509IssuerSerialType createX509IssuerSerialType() {
        return new X509IssuerSerialType();
    }

    /**
     * Create an instance of {@link ContentMaturityRatingType.Audience }
     * 
     */
    public ContentMaturityRatingType.Audience createContentMaturityRatingTypeAudience() {
        return new ContentMaturityRatingType.Audience();
    }

    /**
     * Create an instance of {@link MarkerType.Label }
     * 
     */
    public MarkerType.Label createMarkerTypeLabel() {
        return new MarkerType.Label();
    }

    /**
     * Create an instance of {@link LocaleType.LanguageList }
     * 
     */
    public LocaleType.LanguageList createLocaleTypeLanguageList() {
        return new LocaleType.LanguageList();
    }

    /**
     * Create an instance of {@link LocaleType.RegionList }
     * 
     */
    public LocaleType.RegionList createLocaleTypeRegionList() {
        return new LocaleType.RegionList();
    }

    /**
     * Create an instance of {@link LocaleType.ContentMaturityRatingList }
     * 
     */
    public LocaleType.ContentMaturityRatingList createLocaleTypeContentMaturityRatingList() {
        return new LocaleType.ContentMaturityRatingList();
    }

    /**
     * Create an instance of {@link SegmentType.SequenceList }
     * 
     */
    public SegmentType.SequenceList createSegmentTypeSequenceList() {
        return new SegmentType.SequenceList();
    }

    /**
     * Create an instance of {@link CompositionPlaylistType.ContentVersionList }
     * 
     */
    public CompositionPlaylistType.ContentVersionList createCompositionPlaylistTypeContentVersionList() {
        return new CompositionPlaylistType.ContentVersionList();
    }

    /**
     * Create an instance of {@link CompositionPlaylistType.EssenceDescriptorList }
     * 
     */
    public CompositionPlaylistType.EssenceDescriptorList createCompositionPlaylistTypeEssenceDescriptorList() {
        return new CompositionPlaylistType.EssenceDescriptorList();
    }

    /**
     * Create an instance of {@link CompositionPlaylistType.LocaleList }
     * 
     */
    public CompositionPlaylistType.LocaleList createCompositionPlaylistTypeLocaleList() {
        return new CompositionPlaylistType.LocaleList();
    }

    /**
     * Create an instance of {@link CompositionPlaylistType.ExtensionProperties }
     * 
     */
    public CompositionPlaylistType.ExtensionProperties createCompositionPlaylistTypeExtensionProperties() {
        return new CompositionPlaylistType.ExtensionProperties();
    }

    /**
     * Create an instance of {@link CompositionPlaylistType.SegmentList }
     * 
     */
    public CompositionPlaylistType.SegmentList createCompositionPlaylistTypeSegmentList() {
        return new CompositionPlaylistType.SegmentList();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SequenceType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.smpte-ra.org/schemas/2067-2/2016", name = "VisuallyImpairedTextSequence")
    public JAXBElement<SequenceType> createVisuallyImpairedTextSequence(SequenceType value) {
        return new JAXBElement<SequenceType>(_VisuallyImpairedTextSequence_QNAME, SequenceType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SPKIDataType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2000/09/xmldsig#", name = "SPKIData")
    public JAXBElement<SPKIDataType> createSPKIData(SPKIDataType value) {
        return new JAXBElement<SPKIDataType>(_SPKIData_QNAME, SPKIDataType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.smpte-ra.org/schemas/2067-2/2016", name = "TimedTextResourceID")
    public JAXBElement<String> createTimedTextResourceID(String value) {
        return new JAXBElement<String>(_TimedTextResourceID_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeviceIdentifierListType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/", name = "DeviceIdentifierList")
    public JAXBElement<DeviceIdentifierListType> createDeviceIdentifierList(DeviceIdentifierListType value) {
        return new JAXBElement<DeviceIdentifierListType>(_DeviceIdentifierList_QNAME, DeviceIdentifierListType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link KeyInfoType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2000/09/xmldsig#", name = "KeyInfo")
    public JAXBElement<KeyInfoType> createKeyInfo(KeyInfoType value) {
        return new JAXBElement<KeyInfoType>(_KeyInfo_QNAME, KeyInfoType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SignatureValueType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2000/09/xmldsig#", name = "SignatureValue")
    public JAXBElement<SignatureValueType> createSignatureValue(SignatureValueType value) {
        return new JAXBElement<SignatureValueType>(_SignatureValue_QNAME, SignatureValueType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SequenceType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.smpte-ra.org/schemas/2067-2/2016", name = "SubtitlesSequence")
    public JAXBElement<SequenceType> createSubtitlesSequence(SequenceType value) {
        return new JAXBElement<SequenceType>(_SubtitlesSequence_QNAME, SequenceType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CompositionPlaylistType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.smpte-ra.org/schemas/2067-3/2016", name = "CompositionPlaylist")
    public JAXBElement<CompositionPlaylistType> createCompositionPlaylist(CompositionPlaylistType value) {
        return new JAXBElement<CompositionPlaylistType>(_CompositionPlaylist_QNAME, CompositionPlaylistType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link KeyValueType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2000/09/xmldsig#", name = "KeyValue")
    public JAXBElement<KeyValueType> createKeyValue(KeyValueType value) {
        return new JAXBElement<KeyValueType>(_KeyValue_QNAME, KeyValueType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TransformsType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2000/09/xmldsig#", name = "Transforms")
    public JAXBElement<TransformsType> createTransforms(TransformsType value) {
        return new JAXBElement<TransformsType>(_Transforms_QNAME, TransformsType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DigestMethodType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2000/09/xmldsig#", name = "DigestMethod")
    public JAXBElement<DigestMethodType> createDigestMethod(DigestMethodType value) {
        return new JAXBElement<DigestMethodType>(_DigestMethod_QNAME, DigestMethodType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link List }{@code <}{@link String }{@code >}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.smpte-ra.org/schemas/2067-2/2016", name = "ApplicationIdentification")
    public JAXBElement<List<String>> createApplicationIdentification(List<String> value) {
        return new JAXBElement<List<String>>(_ApplicationIdentification_QNAME, ((Class) List.class), null, ((List<String> ) value));
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeviceDescriptionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/", name = "DeviceDescription")
    public JAXBElement<DeviceDescriptionType> createDeviceDescription(DeviceDescriptionType value) {
        return new JAXBElement<DeviceDescriptionType>(_DeviceDescription_QNAME, DeviceDescriptionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link X509DataType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2000/09/xmldsig#", name = "X509Data")
    public JAXBElement<X509DataType> createX509Data(X509DataType value) {
        return new JAXBElement<X509DataType>(_X509Data_QNAME, X509DataType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SequenceType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.smpte-ra.org/schemas/2067-2/2016", name = "AncillaryDataSequence")
    public JAXBElement<SequenceType> createAncillaryDataSequence(SequenceType value) {
        return new JAXBElement<SequenceType>(_AncillaryDataSequence_QNAME, SequenceType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SignaturePropertyType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2000/09/xmldsig#", name = "SignatureProperty")
    public JAXBElement<SignaturePropertyType> createSignatureProperty(SignaturePropertyType value) {
        return new JAXBElement<SignaturePropertyType>(_SignatureProperty_QNAME, SignaturePropertyType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SequenceType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.smpte-ra.org/schemas/2067-2/2016", name = "KaraokeSequence")
    public JAXBElement<SequenceType> createKaraokeSequence(SequenceType value) {
        return new JAXBElement<SequenceType>(_KaraokeSequence_QNAME, SequenceType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SequenceType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.smpte-ra.org/schemas/2067-2/2016", name = "MainImageSequence")
    public JAXBElement<SequenceType> createMainImageSequence(SequenceType value) {
        return new JAXBElement<SequenceType>(_MainImageSequence_QNAME, SequenceType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2000/09/xmldsig#", name = "KeyName")
    public JAXBElement<String> createKeyName(String value) {
        return new JAXBElement<String>(_KeyName_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RSAKeyValueType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2000/09/xmldsig#", name = "RSAKeyValue")
    public JAXBElement<RSAKeyValueType> createRSAKeyValue(RSAKeyValueType value) {
        return new JAXBElement<RSAKeyValueType>(_RSAKeyValue_QNAME, RSAKeyValueType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SignatureType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2000/09/xmldsig#", name = "Signature")
    public JAXBElement<SignatureType> createSignature(SignatureType value) {
        return new JAXBElement<SignatureType>(_Signature_QNAME, SignatureType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SequenceType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.smpte-ra.org/schemas/2067-2/2016", name = "CommentarySequence")
    public JAXBElement<SequenceType> createCommentarySequence(SequenceType value) {
        return new JAXBElement<SequenceType>(_CommentarySequence_QNAME, SequenceType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2000/09/xmldsig#", name = "MgmtData")
    public JAXBElement<String> createMgmtData(String value) {
        return new JAXBElement<String>(_MgmtData_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SignatureMethodType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2000/09/xmldsig#", name = "SignatureMethod")
    public JAXBElement<SignatureMethodType> createSignatureMethod(SignatureMethodType value) {
        return new JAXBElement<SignatureMethodType>(_SignatureMethod_QNAME, SignatureMethodType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ObjectType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2000/09/xmldsig#", name = "Object")
    public JAXBElement<ObjectType> createObject(ObjectType value) {
        return new JAXBElement<ObjectType>(_Object_QNAME, ObjectType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SignaturePropertiesType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2000/09/xmldsig#", name = "SignatureProperties")
    public JAXBElement<SignaturePropertiesType> createSignatureProperties(SignaturePropertiesType value) {
        return new JAXBElement<SignaturePropertiesType>(_SignatureProperties_QNAME, SignaturePropertiesType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TransformType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2000/09/xmldsig#", name = "Transform")
    public JAXBElement<TransformType> createTransform(TransformType value) {
        return new JAXBElement<TransformType>(_Transform_QNAME, TransformType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SequenceType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.smpte-ra.org/schemas/2067-2/2016", name = "MainAudioSequence")
    public JAXBElement<SequenceType> createMainAudioSequence(SequenceType value) {
        return new JAXBElement<SequenceType>(_MainAudioSequence_QNAME, SequenceType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SequenceType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.smpte-ra.org/schemas/2067-2/2016", name = "HearingImpairedCaptionsSequence")
    public JAXBElement<SequenceType> createHearingImpairedCaptionsSequence(SequenceType value) {
        return new JAXBElement<SequenceType>(_HearingImpairedCaptionsSequence_QNAME, SequenceType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PGPDataType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2000/09/xmldsig#", name = "PGPData")
    public JAXBElement<PGPDataType> createPGPData(PGPDataType value) {
        return new JAXBElement<PGPDataType>(_PGPData_QNAME, PGPDataType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2000/09/xmldsig#", name = "Reference")
    public JAXBElement<ReferenceType> createReference(ReferenceType value) {
        return new JAXBElement<ReferenceType>(_Reference_QNAME, ReferenceType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RetrievalMethodType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2000/09/xmldsig#", name = "RetrievalMethod")
    public JAXBElement<RetrievalMethodType> createRetrievalMethod(RetrievalMethodType value) {
        return new JAXBElement<RetrievalMethodType>(_RetrievalMethod_QNAME, RetrievalMethodType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DSAKeyValueType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2000/09/xmldsig#", name = "DSAKeyValue")
    public JAXBElement<DSAKeyValueType> createDSAKeyValue(DSAKeyValueType value) {
        return new JAXBElement<DSAKeyValueType>(_DSAKeyValue_QNAME, DSAKeyValueType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link byte[]}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2000/09/xmldsig#", name = "DigestValue")
    public JAXBElement<byte[]> createDigestValue(byte[] value) {
        return new JAXBElement<byte[]>(_DigestValue_QNAME, byte[].class, null, ((byte[]) value));
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CanonicalizationMethodType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2000/09/xmldsig#", name = "CanonicalizationMethod")
    public JAXBElement<CanonicalizationMethodType> createCanonicalizationMethod(CanonicalizationMethodType value) {
        return new JAXBElement<CanonicalizationMethodType>(_CanonicalizationMethod_QNAME, CanonicalizationMethodType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SignedInfoType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2000/09/xmldsig#", name = "SignedInfo")
    public JAXBElement<SignedInfoType> createSignedInfo(SignedInfoType value) {
        return new JAXBElement<SignedInfoType>(_SignedInfo_QNAME, SignedInfoType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ManifestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2000/09/xmldsig#", name = "Manifest")
    public JAXBElement<ManifestType> createManifest(ManifestType value) {
        return new JAXBElement<ManifestType>(_Manifest_QNAME, ManifestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link byte[]}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2000/09/xmldsig#", name = "SPKISexp", scope = SPKIDataType.class)
    public JAXBElement<byte[]> createSPKIDataTypeSPKISexp(byte[] value) {
        return new JAXBElement<byte[]>(_SPKIDataTypeSPKISexp_QNAME, byte[].class, SPKIDataType.class, ((byte[]) value));
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2000/09/xmldsig#", name = "HMACOutputLength", scope = SignatureMethodType.class)
    public JAXBElement<BigInteger> createSignatureMethodTypeHMACOutputLength(BigInteger value) {
        return new JAXBElement<BigInteger>(_SignatureMethodTypeHMACOutputLength_QNAME, BigInteger.class, SignatureMethodType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link byte[]}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2000/09/xmldsig#", name = "PGPKeyID", scope = PGPDataType.class)
    public JAXBElement<byte[]> createPGPDataTypePGPKeyID(byte[] value) {
        return new JAXBElement<byte[]>(_PGPDataTypePGPKeyID_QNAME, byte[].class, PGPDataType.class, ((byte[]) value));
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link byte[]}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2000/09/xmldsig#", name = "PGPKeyPacket", scope = PGPDataType.class)
    public JAXBElement<byte[]> createPGPDataTypePGPKeyPacket(byte[] value) {
        return new JAXBElement<byte[]>(_PGPDataTypePGPKeyPacket_QNAME, byte[].class, PGPDataType.class, ((byte[]) value));
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link X509IssuerSerialType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2000/09/xmldsig#", name = "X509IssuerSerial", scope = X509DataType.class)
    public JAXBElement<X509IssuerSerialType> createX509DataTypeX509IssuerSerial(X509IssuerSerialType value) {
        return new JAXBElement<X509IssuerSerialType>(_X509DataTypeX509IssuerSerial_QNAME, X509IssuerSerialType.class, X509DataType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link byte[]}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2000/09/xmldsig#", name = "X509CRL", scope = X509DataType.class)
    public JAXBElement<byte[]> createX509DataTypeX509CRL(byte[] value) {
        return new JAXBElement<byte[]>(_X509DataTypeX509CRL_QNAME, byte[].class, X509DataType.class, ((byte[]) value));
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2000/09/xmldsig#", name = "X509SubjectName", scope = X509DataType.class)
    public JAXBElement<String> createX509DataTypeX509SubjectName(String value) {
        return new JAXBElement<String>(_X509DataTypeX509SubjectName_QNAME, String.class, X509DataType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link byte[]}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2000/09/xmldsig#", name = "X509SKI", scope = X509DataType.class)
    public JAXBElement<byte[]> createX509DataTypeX509SKI(byte[] value) {
        return new JAXBElement<byte[]>(_X509DataTypeX509SKI_QNAME, byte[].class, X509DataType.class, ((byte[]) value));
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link byte[]}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2000/09/xmldsig#", name = "X509Certificate", scope = X509DataType.class)
    public JAXBElement<byte[]> createX509DataTypeX509Certificate(byte[] value) {
        return new JAXBElement<byte[]>(_X509DataTypeX509Certificate_QNAME, byte[].class, X509DataType.class, ((byte[]) value));
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/", name = "Name", scope = VersionInfoListType.class)
    public JAXBElement<String> createVersionInfoListTypeName(String value) {
        return new JAXBElement<String>(_VersionInfoListTypeName_QNAME, String.class, VersionInfoListType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.smpte-ra.org/schemas/433/2008/dcmlTypes/", name = "Value", scope = VersionInfoListType.class)
    public JAXBElement<String> createVersionInfoListTypeValue(String value) {
        return new JAXBElement<String>(_VersionInfoListTypeValue_QNAME, String.class, VersionInfoListType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2000/09/xmldsig#", name = "XPath", scope = TransformType.class)
    public JAXBElement<String> createTransformTypeXPath(String value) {
        return new JAXBElement<String>(_TransformTypeXPath_QNAME, String.class, TransformType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SequenceType }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://www.smpte-ra.org/ns/2067-201/2019", name = "IABSequence")
    public JAXBElement<SequenceType> createIABSequence(SequenceType value) {
        return new JAXBElement<SequenceType>(_IABSequence_QNAME, SequenceType.class, null, value);
    }
}
