package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor.*;
import com.netflix.imflibrary.st0377.header.UL;
import com.netflix.imflibrary.utils.Fraction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by svenkatrav on 10/31/16.
 */
public enum Colorimetry {
    Color1(ColorPrimaries.ITU470PAL,  TransferCharacteristic.ITU709,            CodingEquation.ITU601),
    Color2(ColorPrimaries.SMPTE170M,  TransferCharacteristic.ITU709,            CodingEquation.ITU601),
    Color3(ColorPrimaries.ITU709,     TransferCharacteristic.ITU709,            CodingEquation.ITU709),
    Color4(ColorPrimaries.ITU709,     TransferCharacteristic.IEC6196624xvYCC,   CodingEquation.ITU709),
    Color5(ColorPrimaries.ITU2020,    TransferCharacteristic.ITU2020,           CodingEquation.ITU2020NCL),
    Color6(ColorPrimaries.P3D65,      TransferCharacteristic.ITU2020,           CodingEquation.None),
    Color7(ColorPrimaries.ITU2020,    TransferCharacteristic.SMPTEST2084,       CodingEquation.ITU2020NCL),
    Unknown(ColorPrimaries.Unknown,   TransferCharacteristic.Unknown,           CodingEquation.Unknown);


    private final ColorPrimaries colorPrimary;
    private final TransferCharacteristic transferCharacteristic;
    private final CodingEquation codingEquation;

    Colorimetry(@Nonnull ColorPrimaries colorPrimary, @Nonnull TransferCharacteristic transferCharacteristic, @Nonnull CodingEquation codingEquation) {
        this.colorPrimary = colorPrimary;
        this.transferCharacteristic =  transferCharacteristic;
        this.codingEquation =  codingEquation;
    }

    public @Nonnull ColorPrimaries getColorPrimary() {
        return colorPrimary;
    }

    public @Nonnull TransferCharacteristic getTransferCharacteristic() {
        return transferCharacteristic;
    }

    public @Nonnull CodingEquation getCodingEquation() {
        return codingEquation;
    }

    public static @Nonnull Colorimetry valueOf(@Nonnull ColorPrimaries colorPrimary, @Nonnull TransferCharacteristic transferCharacteristic) {
        for(Colorimetry colorimetry: Colorimetry.values()) {
            if( colorimetry.getColorPrimary().equals(colorPrimary) && colorimetry.getTransferCharacteristic().equals(transferCharacteristic)) {
                return colorimetry;
            }
        }
        return Unknown;
    }

    public static enum CodingEquation {
        ITU601(UL.fromULAsURNStringToUL("urn:smpte:ul:06.0E.2B.34.04.01.01.01.04.01.01.01.02.01.00.00")),
        ITU709(UL.fromULAsURNStringToUL("urn:smpte:ul:06.0E.2B.34.04.01.01.01.04.01.01.01.02.02.00.00")),
        ITU2020NCL(UL.fromULAsURNStringToUL("urn:smpte:ul:06.0E.2B.34.04.01.01.0D.04.01.01.01.02.06.00.00")),
        None(null),
        Unknown(null);
        private final UL codingEquationUL;
        CodingEquation(@Nullable  UL codingEquationUL) {
            this.codingEquationUL = codingEquationUL;
        }

        public @Nullable UL getCodingEquationUL() {
            return codingEquationUL;
        }

        public static @Nonnull CodingEquation valueOf(@Nullable UL codingEquationUL) {
            for(CodingEquation codingEquation: CodingEquation.values()) {
                if( codingEquation.getCodingEquationUL() != null && codingEquation.getCodingEquationUL().equals(codingEquationUL)) {
                    return codingEquation;
                }
            }
            return Unknown;
        }
    }

    public static enum TransferCharacteristic {
        ITU709(UL.fromULAsURNStringToUL("urn:smpte:ul:06.0E.2B.34.04.01.01.01.04.01.01.01.01.02.00.00")),
        IEC6196624xvYCC(UL.fromULAsURNStringToUL("urn:smpte:ul:06.0E.2B.34.04.01.01.01.04.01.01.01.01.08.00.00")),
        ITU2020(UL.fromULAsURNStringToUL("urn:smpte:ul:06.0E.2B.34.04.01.01.01.04.01.01.01.01.09.00.00")),
        SMPTEST2084(UL.fromULAsURNStringToUL("urn:smpte:ul:06.0E.2B.34.04.01.01.0D.04.01.01.01.01.0A.00.00")),
        Unknown(null);

        private final UL transferCharacteristicUL;
        TransferCharacteristic(@Nullable UL transferCharacteristicUL) {
            this.transferCharacteristicUL = transferCharacteristicUL;
        }

        public @Nullable UL getTransferCharacteristicUL() {
            return this.transferCharacteristicUL;
        }

        public static @Nonnull TransferCharacteristic valueOf(@Nullable UL transferCharacteristicUL) {
            for(TransferCharacteristic transferCharacteristic: TransferCharacteristic.values()) {
                if( transferCharacteristic.getTransferCharacteristicUL() != null && transferCharacteristic.getTransferCharacteristicUL().equals(transferCharacteristicUL)) {
                    return transferCharacteristic;
                }
            }
            return Unknown;
        }
    }

    public static enum ColorPrimaries {
        ITU470PAL(UL.fromULAsURNStringToUL("urn:smpte:ul:06.0E.2B.34.04.01.01.06.04.01.01.01.03.02.00.00")),
        SMPTE170M(UL.fromULAsURNStringToUL("urn:smpte:ul:06.0E.2B.34.04.01.01.06.04.01.01.01.03.01.00.00")),
        ITU709(UL.fromULAsURNStringToUL("urn:smpte:ul:06.0E.2B.34.04.01.01.06.04.01.01.01.03.03.00.00")),
        ITU2020(UL.fromULAsURNStringToUL("urn:smpte:ul:06.0E.2B.34.04.01.01.0D.04.01.01.01.03.04.00.00")),
        P3D65(UL.fromULAsURNStringToUL("urn:smpte:ul:06.0E.2B.34.04.01.01.0D.04.01.01.01.03.06.00.00")),
        Unknown(null);

        private final UL colorPrimariesUL;
        ColorPrimaries(@Nullable UL colorPrimariesUL) {
            this.colorPrimariesUL = colorPrimariesUL;
        }

        public @Nullable UL getColorPrimariesUL() {
            return this.colorPrimariesUL;
        }

        public static @Nonnull ColorPrimaries valueOf(@Nullable UL colorPrimariesUL) {
            for(ColorPrimaries colorPrimaries: ColorPrimaries.values()) {
                if( colorPrimaries.getColorPrimariesUL() != null && colorPrimaries.getColorPrimariesUL().equals(colorPrimariesUL)) {
                    return colorPrimaries;
                }
            }
            return Unknown;
        }

    }

    static class ComponentLevel {
        private final Integer bitDepth;
        private final Integer minLevel;
        private final Integer maxLevel;

        public ComponentLevel(@Nonnull Integer bitDepth, @Nonnull Integer minLevel, @Nonnull Integer maxLevel) {
            this.bitDepth = bitDepth;
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
        }

        public @Nonnull Integer getBitDepth() {
            return bitDepth;
        }

        public @Nonnull Integer getMaxLevel() {
            return maxLevel;
        }

        public @Nonnull Integer getMinLevel() {
            return minLevel;
        }

        public boolean equals(Object other)
        {
            if (other == null || !(other instanceof ComponentLevel))
            {
                return false;
            }

            ComponentLevel otherObject = (ComponentLevel)other;

            return (this.bitDepth.equals(otherObject.getBitDepth()) &&
                    this.maxLevel.equals(otherObject.getMaxLevel()) &&
                    this.minLevel.equals(otherObject.getMinLevel()));
        }

        public int hashCode()
        {
            Integer hash = 1;
            hash = hash *31 + this.getBitDepth();
            hash = hash *31 + this.getMaxLevel();
            hash = hash *31 + this.getMinLevel();
            return hash;
        }
    }

    public static enum Quantization {
        QE1(new HashSet<ComponentLevel>() {{
            add(new ComponentLevel(8, 16, 235));
            add(new ComponentLevel(10, 64, 940));
            add(new ComponentLevel(12, 256, 3760));
            add(new ComponentLevel(16, 4096, 60160)); }} ),
        QE2(new HashSet<ComponentLevel>() {{
            add(new ComponentLevel(8, 0, 255));
            add(new ComponentLevel(10, 0, 1023));
            add(new ComponentLevel(12, 0, 4095));
            add(new ComponentLevel(16, 0, 65535)); }} ),
        Unknown(new HashSet<>());

        private final Set<ComponentLevel> componentLevels;
        Quantization(@Nonnull Set<ComponentLevel> componentLevels) {

            this.componentLevels = componentLevels;
        }

        public @Nonnull Set<ComponentLevel> getComponentLevels() {
            return this.componentLevels;
        }

        public static @Nonnull Quantization valueOf(@Nonnull Integer pixelBitDepth, @Nonnull Integer minLevel, @Nonnull Integer maxLevel) {
            ComponentLevel componentLevel = new ComponentLevel(pixelBitDepth, minLevel, maxLevel);
            for(Quantization quantization: Quantization.values()) {
                if(quantization.getComponentLevels().contains(componentLevel)) {
                    return quantization;
                }
            }
            return Unknown;
        }
    }

    public static enum Sampling {
        Sampling444(1, 1),
        Sampling422(2, 1),
        Unknown(0, 0);
        Integer horizontalSubSampling;
        Integer verticalSubSampling;
        Sampling(@Nonnull Integer horizontalSubSampling, @Nonnull Integer verticalSubSampling) {
            this.horizontalSubSampling = horizontalSubSampling;
            this.verticalSubSampling = verticalSubSampling;
        }

        public @Nonnull Integer getHorizontalSubSampling() {
            return horizontalSubSampling;
        }

        public @Nonnull Integer getVerticalSubSampling() {
            return verticalSubSampling;
        }

        public static @Nonnull Sampling valueOf(@Nonnull Integer horizontalSubSampling, @Nonnull Integer verticalSubSampling) {
            for(Sampling sampling: Sampling.values()) {
                if(sampling.getHorizontalSubSampling().equals(horizontalSubSampling) && sampling.getVerticalSubSampling().equals( verticalSubSampling)) {
                    return sampling;
                }
            }
            return Unknown;
        }
    }

    public static enum ColorSpace {
        RGB(new HashSet<RGBAComponentType>() {{ add(RGBAComponentType.Red); add(RGBAComponentType.Green); add(RGBAComponentType.Blue);}}),
        YUV(new HashSet<RGBAComponentType>() {{ add(RGBAComponentType.Luma); add(RGBAComponentType.ChromaU); add(RGBAComponentType.ChromaV);}}),
        Unknown(new HashSet<>());
        private final Set<RGBAComponentType> componentTypeSet;
        ColorSpace(@Nonnull Set<RGBAComponentType> componentTypeSet) {
            this.componentTypeSet = componentTypeSet;
        }

        public @Nonnull Set<RGBAComponentType> getComponentTypeSet() {
            return componentTypeSet;
        }
    }
}
