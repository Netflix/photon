package com.netflix.imflibrary.st2067_201;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.MXFUID;
import com.netflix.imflibrary.annotations.MXFProperty;
import com.netflix.imflibrary.st0377.header.GenericDescriptor;
import com.netflix.imflibrary.st0377.header.MCALabelSubDescriptor;
import com.netflix.imflibrary.st0377.header.StructuralMetadata;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.utils.ByteProvider;
import javax.annotation.concurrent.Immutable;

import java.io.IOException;
import java.util.Map;

/**
 * Object model corresponding to MultiChannelAudioLabelSubDescriptor structural metadata set defined in st20167-201:201x
 */
@Immutable
public final class IABSoundfieldLabelSubDescriptor extends MCALabelSubDescriptor {

    public static final String IAB_MCA_TAG_SYMBOL = "IAB";
    public static final String IAB_MCA_TAG_NAME = "IAB";
    public static final UL IAB_MCA_LABEL_DICTIONNARY_ID_UL = UL.fromULAsURNStringToUL("urn:smpte:ul:060E2B34.0401010D.03020221.00000000");
    private static final String ERROR_DESCRIPTION_PREFIX = "MXF Header Partition: " + IABSoundfieldLabelSubDescriptor.class.getSimpleName() + " : ";
    private final IABSoundfieldLabelSubDescriptorBO iabSoundfieldLabelSubDescriptorBO;

    /**
     * Constructor for an IABSoundfieldLabelSubDescriptor object
     * @param iabSoundfieldLabelSubDescriptorBO the parsed IAB Soundfield label sub-descriptor object
     */
    public IABSoundfieldLabelSubDescriptor(IABSoundfieldLabelSubDescriptorBO iabSoundfieldLabelSubDescriptorBO)
    {
        this.iabSoundfieldLabelSubDescriptorBO = iabSoundfieldLabelSubDescriptorBO;
    }

    /**
     * A getter for the spoken language in this SubDescriptor
     * @return string representing the spoken language as defined in RFC-5646
     */
    public String getRFC5646SpokenLanguage(){
        return this.iabSoundfieldLabelSubDescriptorBO.getRFC5646SpokenLanguage();
    }
    /**
     * A method that returns a string representation of an IABSoundfieldLabelSubDescriptor object.
     *
     * @return string representing the object
     */
    public String toString()
    {
        return this.iabSoundfieldLabelSubDescriptorBO.toString();
    }

    /**
     * Object corresponding to a parsed IABSoundfieldLabelSubDescriptor structural metadata set defined in st20167-201:201x
     */
    @Immutable
    @SuppressWarnings({"PMD.FinalFieldCouldBeStatic"})
    public static final class IABSoundfieldLabelSubDescriptorBO extends MCALabelSubDescriptor.MCALabelSubDescriptorBO
    {

        @MXFProperty(size=16) private final byte[] soundfield_group_link_id = null; //UUID

        /**
         * Instantiates a new parsed AudioChannelLabelSubDescriptor object by virtue of parsing the MXF file bitstream
         *
         * @param header the parsed header (K and L fields from the KLV packet)
         * @param byteProvider the input stream corresponding to the MXF file
         * @param localTagToUIDMap mapping from local tag to element UID as provided by the Primer Pack defined in st377-1:2011
         * @param imfErrorLogger logger for recording any parsing errors
         * @throws IOException - any I/O related error will be exposed through an IOException
         */
        public IABSoundfieldLabelSubDescriptorBO(KLVPacket.Header header, ByteProvider byteProvider, Map<Integer, MXFUID> localTagToUIDMap, IMFErrorLogger imfErrorLogger)
                throws IOException
        {
            super(header);
            long numBytesToRead = this.header.getVSize();

            StructuralMetadata.populate(this, byteProvider, numBytesToRead, localTagToUIDMap);

            if (this.instance_uid == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        IABSoundfieldLabelSubDescriptor.ERROR_DESCRIPTION_PREFIX + "instance_uid is null");
            }

            if (this.mca_label_dictionary_id == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        IABSoundfieldLabelSubDescriptor.ERROR_DESCRIPTION_PREFIX + "mca_label_dictionary_id is null");
            }

            if (this.mca_link_id == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        IABSoundfieldLabelSubDescriptor.ERROR_DESCRIPTION_PREFIX + "mca_link_id is null");
            }

            if (this.mca_tag_symbol == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        IABSoundfieldLabelSubDescriptor.ERROR_DESCRIPTION_PREFIX + "mca_tag_symbol is null");
            }
        }

        /**
         * A method that returns a string representation of the object.
         *
         * @return string representing the object
         */
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(super.toString());
            if (this.soundfield_group_link_id != null)
            {
                sb.append(String.format("soundfield_group_link_id = 0x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%n",
                        this.soundfield_group_link_id[0], this.soundfield_group_link_id[1], this.soundfield_group_link_id[2], this.soundfield_group_link_id[3],
                        this.soundfield_group_link_id[4], this.soundfield_group_link_id[5], this.soundfield_group_link_id[6], this.soundfield_group_link_id[7],
                        this.soundfield_group_link_id[8], this.soundfield_group_link_id[9], this.soundfield_group_link_id[10], this.soundfield_group_link_id[11],
                        this.soundfield_group_link_id[12], this.soundfield_group_link_id[13], this.soundfield_group_link_id[14], this.soundfield_group_link_id[15]));
            }

            return sb.toString();
        }
    }
}
