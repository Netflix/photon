package com.netflix.imflibrary.st2067_204;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.MXFUID;
import com.netflix.imflibrary.annotations.MXFProperty;
import com.netflix.imflibrary.st0377.header.SoundFieldGroupLabelSubDescriptor;
import com.netflix.imflibrary.st0377.header.StructuralMetadata;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.utils.ByteProvider;
import javax.annotation.concurrent.Immutable;

import java.io.IOException;
import java.util.Map;

/**
 * Object model corresponding to ADMSoundfieldGroupLabelSubDescriptor structural metadata set defined in ST 2131
 */
@Immutable
public final class ADMSoundfieldGroupLabelSubDescriptor extends SoundFieldGroupLabelSubDescriptor {

    public static final String ADM_MCA_TAG_SYMBOL = "ADM";
    public static final String ADM_MCA_TAG_NAME = "ADM";
    public static final UL ADM_MCA_LABEL_DICTIONNARY_ID_UL = UL.fromULAsURNStringToUL("urn:smpte:ul:060e2b34.0401010d.03020223.00000000");
    private static final String ERROR_DESCRIPTION_PREFIX = "MXF Header Partition: " + ADMSoundfieldGroupLabelSubDescriptor.class.getSimpleName() + " : ";
    private final ADMSoundfieldGroupLabelSubDescriptorBO admSoundfieldLabelSubDescriptorBO;

    /**
     * Constructor for an ADMSoundfieldGroupLabelSubDescriptor object
     * @param admSoundfieldLabelSubDescriptorBO the parsed ADM Soundfield label sub-descriptor object
     */
    public ADMSoundfieldGroupLabelSubDescriptor(ADMSoundfieldGroupLabelSubDescriptorBO admSoundfieldLabelSubDescriptorBO)
    {
        super(admSoundfieldLabelSubDescriptorBO);
        this.admSoundfieldLabelSubDescriptorBO = admSoundfieldLabelSubDescriptorBO;
    }

    /**
     * A getter for the spoken language in this SubDescriptor
     * @return string representing the spoken language as defined in RFC-5646
     */
    public String getRFC5646SpokenLanguage(){
        return this.admSoundfieldLabelSubDescriptorBO.getRFC5646SpokenLanguage();
    }
    /**
     * A method that returns a string representation of an ADMSoundfieldGroupLabelSubDescriptor object.
     *
     * @return string representing the object
     */
    public String toString()
    {
        return this.admSoundfieldLabelSubDescriptorBO.toString();
    }

    /**
     * Object corresponding to a parsed ADMSoundfieldGroupLabelSubDescriptor structural metadata set defined in ST 2131
     */
    @Immutable
    @SuppressWarnings({"PMD.FinalFieldCouldBeStatic"})
    public static final class ADMSoundfieldGroupLabelSubDescriptorBO extends SoundFieldGroupLabelSubDescriptor.SoundFieldGroupLabelSubDescriptorBO
    {

        @MXFProperty(size=4) protected final Long riff_chunk_stream_id_link2 = null; //UINT32
        @MXFProperty(size=0, charset = "UTF-16") protected final String adm_audio_programme_id_st2131 = null; //UTF-16 String
        @MXFProperty(size=0, charset = "UTF-16") protected final String adm_audio_content_id_st2131 = null; //UTF-16 String
        @MXFProperty(size=0, charset = "UTF-16") protected final String adm_audio_object_id_st2131 = null; //UTF-16 String

        /**
         * Instantiates a new parsed ADMSoundfieldGroupLabelSubDescriptor object by virtue of parsing the MXF file bitstream
         *
         * @param header the parsed header (K and L fields from the KLV packet)
         * @param byteProvider the input stream corresponding to the MXF file
         * @param localTagToUIDMap mapping from local tag to element UID as provided by the Primer Pack defined in st377-1:2011
         * @param imfErrorLogger logger for recording any parsing errors
         * @throws IOException - any I/O related error will be exposed through an IOException
         */
        public ADMSoundfieldGroupLabelSubDescriptorBO(KLVPacket.Header header, ByteProvider byteProvider, Map<Integer, MXFUID> localTagToUIDMap, IMFErrorLogger imfErrorLogger)
                throws IOException
        {
            super(header); 
            long numBytesToRead = this.header.getVSize();

            StructuralMetadata.populate(this, byteProvider, numBytesToRead, localTagToUIDMap);
            if (this.riff_chunk_stream_id_link2 == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        ADMSoundfieldGroupLabelSubDescriptor.ERROR_DESCRIPTION_PREFIX + "RIFFChunkStreamID_link2 is not present");
            }

        }

        /**
         * Accessor for the adm_audio_programm_id of this ADMSoundfieldGroupLabelSubDescriptor
         * @return a byte array representing the adm_audio_programm_id for the MGASoundfieldGroupLabelSubDescriptor
         */
        public String getADMAudioProgrammeId(){
            return this.adm_audio_programme_id_st2131;
        }

        /**
         * Accessor for the adm_audio_content_id of this ADMSoundfieldGroupLabelSubDescriptor
         * @return a byte array representing the adm_audio_content_id for the MGASoundfieldGroupLabelSubDescriptor
         */
        public String getADMAudioContentId(){
            return this.adm_audio_content_id_st2131;
        }

        /**
         * Accessor for the adm_audio_object_id of this ADMSoundfieldGroupLabelSubDescriptor
         * @return a byte array representing the adm_audio_object_id for the MGASoundfieldGroupLabelSubDescriptor
         */
        public String getADMAudioObjectId(){
            return this.adm_audio_object_id_st2131;
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
            if (this.riff_chunk_stream_id_link2 != null)
            {
                sb.append(String.format("RIFFChunkStreamID_link2 = x%02x%n",
                		riff_chunk_stream_id_link2));
            }

            return sb.toString();
        }
    }
}
