/*
 *
 * Copyright 2015 Netflix, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.netflix.imflibrary.st0377.header;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.MXFUID;
import com.netflix.imflibrary.utils.ByteProvider;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.util.Map;

/**
 * Object model corresponding to GroupOfSoundFieldGroupLabelSubDescriptor structural metadata set defined in st377-4:2012
 */
@Immutable
public final class GroupOfSoundFieldGroupLabelSubDescriptor extends SubDescriptor
{
    private static final String ERROR_DESCRIPTION_PREFIX = "MXF Header Partition: " + GroupOfSoundFieldGroupLabelSubDescriptor.class.getSimpleName() + " : ";
    private final GroupOfSoundFieldGroupLabelSubDescriptorBO groupOfSoundFieldGroupLabelSubDescriptorBO;

    /**
     * Constructor for a GroupOfSoundFieldGroupLabelSubDescriptor object
     * @param groupOfSoundFieldGroupLabelSubDescriptorBO the parsed GroupOfSoundFieldGroupLabelSubDescriptor object
     */
    public GroupOfSoundFieldGroupLabelSubDescriptor(GroupOfSoundFieldGroupLabelSubDescriptorBO groupOfSoundFieldGroupLabelSubDescriptorBO)
    {
        this.groupOfSoundFieldGroupLabelSubDescriptorBO = groupOfSoundFieldGroupLabelSubDescriptorBO;
    }

    /**
     * Getter for the MCALabelDictionary ID referred by this GroupOfSoundFieldGroupLabelSubDescriptor object
     * @return the MCALabelDictionary ID referred by this GroupOfSoundFieldGroupLabelSubDescriptor object
     */
    public MXFUID getMCALabelDictionaryId()
    {
        return this.groupOfSoundFieldGroupLabelSubDescriptorBO.mca_label_dictionary_id.getULAsMXFUid();
    }

    /**
     * Getter for the MCALink Id that links the instances of the MCALabelSubDescriptors
     * @return the MCALink Id that links the instances of the MCALabelSubDescriptors
     */
    public MXFUID getMCALinkId()
    {
        if(this.groupOfSoundFieldGroupLabelSubDescriptorBO.mca_link_id == null) {
            return null;
        }
        return new MXFUID(this.groupOfSoundFieldGroupLabelSubDescriptorBO.mca_link_id);
    }

    /**
     * A getter for the spoken language in this SubDescriptor
     * @return string representing the spoken language as defined in RFC-5646
     */
    public String getRFC5646SpokenLanguage(){
        return this.groupOfSoundFieldGroupLabelSubDescriptorBO.rfc_5646_spoken_language;
    }

    /**
     * A method that returns a string representation of a GroupOfSoundFieldGroupLabelSubDescriptor object
     *
     * @return string representing the object
     */
    public String toString()
    {
        return this.groupOfSoundFieldGroupLabelSubDescriptorBO.toString();
    }

    /**
     * Object corresponding to a parsed GroupOfSoundFieldGroupLabelSubDescriptor structural metadata set defined in st377-4:2012
     */
    @Immutable
    @SuppressWarnings({"PMD.FinalFieldCouldBeStatic"})
    public static final class GroupOfSoundFieldGroupLabelSubDescriptorBO extends MCALabelSubDescriptor.MCALabelSubDescriptorBO
    {
        //@MXFProperty(size=0, charset="UTF-16") protected final String group_of_soundfield_groups_linkID = null;
        /**
         * Instantiates a new parsed GroupOfSoundFieldGroupLabelSubDescriptor object by virtue of parsing the MXF file bitstream
         *
         * @param header the parsed header (K and L fields from the KLV packet)
         * @param byteProvider the input stream corresponding to the MXF file
         * @param localTagToUIDMap mapping from local tag to element UID as provided by the Primer Pack defined in st377-1:2011
         * @param imfErrorLogger logger for recording any parsing errors
         * @throws IOException - any I/O related error will be exposed through an IOException
         */
        public GroupOfSoundFieldGroupLabelSubDescriptorBO(KLVPacket.Header header, ByteProvider byteProvider, Map<Integer, MXFUID> localTagToUIDMap, IMFErrorLogger imfErrorLogger)
                throws IOException
        {
            super(header);
            long numBytesToRead = this.header.getVSize();

            StructuralMetadata.populate(this, byteProvider, numBytesToRead, localTagToUIDMap);

            if (this.instance_uid == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        GroupOfSoundFieldGroupLabelSubDescriptor.ERROR_DESCRIPTION_PREFIX + "instance_uid is null");
            }

            if (this.mca_label_dictionary_id == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        GroupOfSoundFieldGroupLabelSubDescriptor.ERROR_DESCRIPTION_PREFIX + "mca_label_dictionary_id is null");
            }

            if (this.mca_link_id == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        GroupOfSoundFieldGroupLabelSubDescriptor.ERROR_DESCRIPTION_PREFIX + "mca_link_id is null");
            }

            if (this.mca_tag_symbol == null)
            {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        GroupOfSoundFieldGroupLabelSubDescriptor.ERROR_DESCRIPTION_PREFIX + "mca_tag_symbol is null");
            }

        }

    }


}
