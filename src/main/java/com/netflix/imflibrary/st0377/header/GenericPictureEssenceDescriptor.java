/*
 *
 *  Copyright 2015 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.netflix.imflibrary.st0377.header;

import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.annotations.MXFProperty;
import com.netflix.imflibrary.st0377.CompoundDataTypes;

/**
 * Object model corresponding to GenericPictureEssenceDescriptor structural metadata set defined in st377-1:2011
 */
public abstract class GenericPictureEssenceDescriptor extends FileDescriptor {
    public static final String rgbaDescriptorUL = "urn:smpte:ul:060e2b34.027f0101.0d010101.01012900";
    public static final String cdciDescriptorUL = "urn:smpte:ul:060e2b34.027f0101.0d010101.01012800";
    public static final String frameLayoutUL = "urn:smpte:ul:060e2b34.01010101.04010301.04000000";
    public static final String frameLayoutTypeUL= "urn:smpte:ul:060e2b34.01040101.02010108.00000000";
    public static final String storedWidthUL = "urn:smpte:ul:060e2b34.01010101.04010502.02000000";
    public static final String storedHeightUL = "urn:smpte:ul:060e2b34.01010101.04010502.01000000";
    public static final String sampleRateUL = "urn:smpte:ul:060e2b34.01010101.04060101.00000000";
    public static final String storedF2OffsetUL = "urn:smpte:ul:060e2b34.01010105.04010302.08000000";
    public static final String sampledWidthUL = "urn:smpte:ul:060e2b34.01010101.04010501.08000000";
    public static final String sampledHeightUL = "urn:smpte:ul:060e2b34.01010101.04010501.07000000";
    public static final String subdescriptorsUL = "urn:smpte:ul:060e2b34.01010109.06010104.06100000";
    public static final String jpeg2000SubDescriptorUL = "urn:smpte:ul:060e2b34.027f0101.0d010101.01015a00";
    public static final String j2cLayoutUL = "urn:smpte:ul:060e2b34.0101010e.04010603.0e000000";
    public static final String rgbaComponentUL = "urn:smpte:ul:060e2b34.01040101.03010400.00000000";
    public static final String componentSizeUL = "urn:smpte:ul:060e2b34.01040101.01010100.00000000";
    public static final String codeUL = "urn:smpte:ul:060e2b34.01040101.0201010e.00000000";
    public static final String rgbaComponentKindUL = "urn:smpte:ul:060e2b34.01040101.0201010e.00000000";
    public static final String colorPrimariesUL = "urn:smpte:ul:060e2b34.01010109.04010201.01060100";
    public static final String transferCharacteristicUL = "urn:smpte:ul:060e2b34.01010102.04010201.01010200";
    public static final String codingEquationsUL = "urn:smpte:ul:060e2b34.01010102.04010201.01030100";
    public static final String componentMinRefUL = "urn:smpte:ul:060e2b34.01010105.04010503.0c000000";
    public static final String componentMaxRefUL = "urn:smpte:ul:060e2b34.01010105.04010503.0b000000";
    public static final String blackRefLevelUL = "urn:smpte:ul:060e2b34.01010101.04010503.03000000";
    public static final String whiteRefLevelUL = "urn:smpte:ul:060e2b34.01010101.04010503.04000000";
    public static final String horizontalSubSamplingUL = "urn:smpte:ul:060e2b34.01010101.04010501.05000000";
    public static final String verticalSubSamplingUL = "urn:smpte:ul:060e2b34.01010102.04010501.10000000";
    public static final String containerFormatUL = "urn:smpte:ul:060e2b34.01010102.06010104.01020000";


    public static abstract class GenericPictureEssenceDescriptorBO extends FileDescriptorBO {

        @MXFProperty(size=1) protected final Short frame_layout = null;
        @MXFProperty(size=4) protected final Long stored_width = null;
        @MXFProperty(size=4) protected final Long stored_height = null;
        @MXFProperty(size=0) protected final CompoundDataTypes.Rational aspect_ratio = null;
        @MXFProperty(size=0) protected final CompoundDataTypes.MXFCollections.MXFCollection<Integer> video_line_map = null;
        @MXFProperty(size=16) protected final UL picture_essence_coding = null;
        @MXFProperty(size=16) protected final UL color_primaries = null;
        @MXFProperty(size=16) protected final UL coding_equations = null;
        @MXFProperty(size=16) protected final UL transfer_characteristic = null;


        /**
         * Constructor for a File descriptor ByteObject.
         *
         * @param header the MXF KLV header (Key and Length field)
         */
        GenericPictureEssenceDescriptorBO(final KLVPacket.Header header) {
            super(header);
        }

        /**
         * Accessor for the ColorPrimaries UL
         * @return a UL representing the ColorPrimaries
         */
        public UL getColorPrimariesUL(){
            return this.color_primaries;
        }

        /**
         * Accessor for the CodingEquations UL
         * @return a UL representing the CodingEquations
         */
        public UL getCodingEquationsUL(){
            return this.coding_equations;
        }

        /**
         * Accessor for the TransferCharacteristic UL
         * @return a UL representing the TransferCharacteristic
         */
        public UL getTransferCharacteristicUL(){
            return this.transfer_characteristic;
        }

        public CompoundDataTypes.Rational getAspect_ratio() {
            return aspect_ratio;
        }
    }

    public static enum RGBAComponentType {
        Null(0),
        Red(0x52),
        Green(0x47),
        Blue(0x42),
        Luma(0x59),
        ChromaU(0x55),
        ChromaV(0x56),
        Unknown(-1);
        private final Integer code;

        RGBAComponentType(Integer code) {
            this.code = code;
        }

        public static RGBAComponentType valueOf(Integer code) {
            for(RGBAComponentType cur : RGBAComponentType.values())
            {
                if(cur.getCode().equals(code))
                {
                    return cur;
                }
            }
            return Unknown;
        }

        public Integer getCode() { return this.code;}
    }

    public static enum FrameLayoutType {
        FullFrame(0),
        SeparateFields(1),
        SingleField(2),
        MixedFields(3),
        SegmentedFrame(4),
        Unknown(-1);
        private final Integer value;

        FrameLayoutType(Integer value) {
            this.value = value;
        }

        public Integer getValue() { return this.value;}

        public static FrameLayoutType valueOf(Integer value) {
            for(FrameLayoutType frameLayout: FrameLayoutType.values()) {
                if(frameLayout.getValue().equals(value)) {
                    return frameLayout;
                }
            }
            return Unknown;
        }
    }
}
