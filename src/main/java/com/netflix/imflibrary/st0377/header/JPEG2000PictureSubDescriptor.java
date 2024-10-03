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
import com.netflix.imflibrary.annotations.MXFProperty;
import com.netflix.imflibrary.st0377.CompoundDataTypes;
import com.netflix.imflibrary.utils.ByteProvider;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.util.Map;

/**
 * Object model corresponding to JPEG2000PictureSubDescriptor as defined in st429-4:2006
 */
@Immutable
public final class JPEG2000PictureSubDescriptor extends SubDescriptor {
    private static final String ERROR_DESCRIPTION_PREFIX = "MXF Header Partition: " + JPEG2000PictureSubDescriptor.class.getSimpleName() + " : ";
    private final JPEG2000PictureSubDescriptorBO subDescriptorBO;

    /**
     * Constructor for a JPEG2000PictureSubDescriptor object
     * @param subDescriptorBO the parsed JPEG2000PictureSubDescriptor object
     */
    public JPEG2000PictureSubDescriptor(JPEG2000PictureSubDescriptorBO subDescriptorBO){
        this.subDescriptorBO = subDescriptorBO;
    }

    /**
     * A method that returns a string representation of a JPEG2000PictureSubDescriptor object
     *
     * @return string representing the object
     */
    public String toString()
    {
        return this.subDescriptorBO.toString();
    }

    /**
     * Object corresponding to a parsed JPEG2000PictureSubDescriptor as defined in st429-4-2006
     */
    @Immutable
    public static final class JPEG2000PictureSubDescriptorBO extends SubDescriptorBO {

        @MXFProperty(size=16) protected final byte[] generation_uid = null;
        @MXFProperty(size=2) protected final Short rSiz = null;
        @MXFProperty(size=4) protected final Integer xSiz = null;
        @MXFProperty(size=4) protected final Integer ySiz = null;
        @MXFProperty(size=4) protected final Integer xoSiz = null;
        @MXFProperty(size=4) protected final Integer yoSiz = null;
        @MXFProperty(size=4) protected final Integer xtSiz = null;
        @MXFProperty(size=4) protected final Integer ytSiz = null;
        @MXFProperty(size=4) protected final Integer xtoSiz = null;
        @MXFProperty(size=4) protected final Integer ytoSiz = null;
        @MXFProperty(size=2) protected final Short cSiz = null;
        @MXFProperty(size=0, depends=true) private final CompoundDataTypes.MXFCollections.MXFCollection<JPEG2000PictureComponent.JPEG2000PictureComponentBO> picture_component_sizing = null;
        @MXFProperty(size=0, depends=false) private final byte[] coding_style_default = null;
        @MXFProperty(size=0, depends=false) private final byte[] quantisation_default = null;
        @MXFProperty(size=0, depends=false) private final J2KExtendedCapabilities j2k_extended_capabilities = null;

        /**
         * Instantiates a new JPEG2000 picture sub descriptor ByteObject.
         *
         * @param header the header
         * @param byteProvider the mxf byte provider
         * @param localTagToUIDMap the local tag to uID map
         * @param imfErrorLogger the imf error logger
         * @throws IOException - any I/O related error will be exposed through an IOException
         */

        public JPEG2000PictureSubDescriptorBO(KLVPacket.Header header, ByteProvider byteProvider, Map<Integer, MXFUID> localTagToUIDMap, IMFErrorLogger imfErrorLogger)
                throws IOException {
            super(header);
            long numBytesToRead = this.header.getVSize();
            StructuralMetadata.populate(this, byteProvider, numBytesToRead, localTagToUIDMap);

            if (this.instance_uid == null) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_METADATA_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        JPEG2000PictureSubDescriptor.ERROR_DESCRIPTION_PREFIX + "instance_uid is null");
            }
        }

        public Short getRSiz() {
            return rSiz;
        }

        public Integer getXSiz() {
            return xSiz;
        }

        public Integer getYSiz() {
            return ySiz;
        }

        public Integer getXoSiz() {
            return xoSiz;
        }

        public Integer getYoSiz() {
            return yoSiz;
        }

        public Integer getXtSiz() {
            return xtSiz;
        }

        public Integer getYtSiz() {
            return ytSiz;
        }

        public Integer getXtoSiz() {
            return xtoSiz;
        }

        public Integer getYtoSiz() {
            return ytoSiz;
        }

        public Short getCSiz() {
            return cSiz;
        }

        public CompoundDataTypes.MXFCollections.MXFCollection<JPEG2000PictureComponent.JPEG2000PictureComponentBO> getPictureComponentSizing() {
            return picture_component_sizing;
        }

        public String getCodingStyleDefaultString() {
            return DatatypeConverter.printHexBinary(coding_style_default);
        }

        public String getQuantisationDefaultString() {
            return DatatypeConverter.printHexBinary(quantisation_default);
        }

        public J2KExtendedCapabilities getJ2kExtendedCapabilities() {
            return j2k_extended_capabilities;
        }

        /**
         * A method that returns a string representation of a JPEG2000PictureSubDescriptorBO object
         *
         * @return string representing the object
         */

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("================== JPEG2000PictureSubDescriptor ======================\n");
            sb.append(this.header.toString());
            sb.append(String.format("instance_uid = 0x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%n",
                    this.instance_uid[0], this.instance_uid[1], this.instance_uid[2], this.instance_uid[3],
                    this.instance_uid[4], this.instance_uid[5], this.instance_uid[6], this.instance_uid[7],
                    this.instance_uid[8], this.instance_uid[9], this.instance_uid[10], this.instance_uid[11],
                    this.instance_uid[12], this.instance_uid[13], this.instance_uid[14], this.instance_uid[15]));
            sb.append(String.format("rSiz = %d", this.rSiz));
            sb.append(String.format("xSiz = %d", this.xSiz));
            sb.append(String.format("ySiz = %d", this.ySiz));
            sb.append(String.format("xoSiz = %d", this.xoSiz));
            sb.append(String.format("yoSiz = %d", this.yoSiz));
            sb.append(String.format("xtSiz = %d", this.xtSiz));
            sb.append(String.format("ytSiz = %d", this.ytSiz));
            sb.append(String.format("xtoSiz = %d", this.xtoSiz));
            sb.append(String.format("ytoSiz = %d", this.ytoSiz));
            sb.append(String.format("cSiz = %d", this.cSiz));
            sb.append(this.picture_component_sizing.toString());
            String codingStyleDefaultString = "";
            for(byte b: coding_style_default){
                codingStyleDefaultString = codingStyleDefaultString.concat(String.format("%02x", b));
            }
            sb.append(String.format("coding_style_default = %s", codingStyleDefaultString));
            String quantisationDefaultString = "";
            for(byte b: coding_style_default){
                quantisationDefaultString = quantisationDefaultString.concat(String.format("%02x", b));
            }
            sb.append(String.format("quantisation_default = %s", quantisationDefaultString));
            sb.append(String.format("j2k_extended_capabilities = %s", j2k_extended_capabilities));
            return sb.toString();
        }
    }
}
