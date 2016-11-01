package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.st0377.header.UL;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by svenkatrav on 10/31/16.
 */
public enum Colorimetry {
    Color1(ColorPrimaries.ITU470PAL,  TransferCharacteristic.ITU709,            CodingEquation.ITU601),
    Color2(ColorPrimaries.SMPTE170M,  TransferCharacteristic.ITU709,            CodingEquation.ITU601),
    Color3(ColorPrimaries.ITU709,     TransferCharacteristic.ITU709,            CodingEquation.ITU709),
    Color4(ColorPrimaries.ITU709,     TransferCharacteristic.IEC6196624xvYCC,   CodingEquation.ITU709),
    Color5(ColorPrimaries.ITU2020,    TransferCharacteristic.ITU2020,           CodingEquation.ITU2020NCL),
    Color6(ColorPrimaries.P3D65,      TransferCharacteristic.ITU2020,           null),
    Color7(ColorPrimaries.ITU2020,    TransferCharacteristic.SMPTEST2084,       CodingEquation.ITU2020NCL);


    private final ColorPrimaries colorPrimary;
    private final TransferCharacteristic transferCharacteristic;
    private final CodingEquation codingEquation;

    Colorimetry(@Nonnull ColorPrimaries colorPrimary, @Nonnull TransferCharacteristic transferCharacteristic, @Nullable CodingEquation codingEquation) {
        this.colorPrimary = colorPrimary;
        this.transferCharacteristic =  transferCharacteristic;
        this.codingEquation =  codingEquation;
    }

    public ColorPrimaries getColorPrimary() {
        return colorPrimary;
    }

    public TransferCharacteristic getTransferCharacteristic() {
        return transferCharacteristic;
    }

    public CodingEquation getCodingEquation() {
        return codingEquation;
    }

    public static Colorimetry valueOf(ColorPrimaries colorPrimary, TransferCharacteristic transferCharacteristic) {
        for(Colorimetry colorimetry: Colorimetry.values()) {
            if( colorimetry.getColorPrimary().equals(colorPrimary) && colorimetry.getTransferCharacteristic().equals(transferCharacteristic)) {
                return colorimetry;
            }
        }
        return null;
    }

    public static enum CodingEquation {
        ITU601(UL.fromULAsURNStringToUL("urn:smpte:ul:06.0E.2B.34.04.01.01.01.04.01.01.01.02.01.00.00")),
        ITU709(UL.fromULAsURNStringToUL("urn:smpte:ul:06.0E.2B.34.04.01.01.01.04.01.01.01.02.02.00.00")),
        ITU2020NCL(UL.fromULAsURNStringToUL("urn:smpte:ul:06.0E.2B.34.04.01.01.0D.04.01.01.01.02.06.00.00"));
        private UL codingEquationUL;
        CodingEquation(@Nonnull UL codingEquationUL) {
            this.codingEquationUL = codingEquationUL;
        }

        public UL getCodingEquationUL() {
            return codingEquationUL;
        }

        public static CodingEquation valueOf(UL codingEquationUL) {
            for(CodingEquation codingEquation: CodingEquation.values()) {
                if( codingEquation.getCodingEquationUL().equals(codingEquationUL)) {
                    return codingEquation;
                }
            }
            return null;
        }
    }

    public static enum TransferCharacteristic {
        ITU709(UL.fromULAsURNStringToUL("urn:smpte:ul:06.0E.2B.34.04.01.01.01.04.01.01.01.01.02.00.00")),
        IEC6196624xvYCC(UL.fromULAsURNStringToUL("urn:smpte:ul:06.0E.2B.34.04.01.01.01.04.01.01.01.01.08.00.00")),
        ITU2020(UL.fromULAsURNStringToUL("urn:smpte:ul:06.0E.2B.34.04.01.01.01.04.01.01.01.01.09.00.00")),
        SMPTEST2084(UL.fromULAsURNStringToUL("urn:smpte:ul:06.0E.2B.34.04.01.01.0D.04.01.01.01.01.0A.00.00"));

        private UL transferCharacteristicUL;
        TransferCharacteristic(@Nonnull UL transferCharacteristicUL) {
            this.transferCharacteristicUL = transferCharacteristicUL;
        }

        public UL getTransferCharacteristicUL() {
            return this.transferCharacteristicUL;
        }

        public static TransferCharacteristic valueOf(UL transferCharacteristicUL) {
            for(TransferCharacteristic transferCharacteristic: TransferCharacteristic.values()) {
                if( transferCharacteristic.getTransferCharacteristicUL().equals(transferCharacteristicUL)) {
                    return transferCharacteristic;
                }
            }
            return null;
        }
    }

    public static enum ColorPrimaries {
        ITU470PAL(UL.fromULAsURNStringToUL("urn:smpte:ul:06.0E.2B.34.04.01.01.06.04.01.01.01.03.02.00.00")),
        SMPTE170M(UL.fromULAsURNStringToUL("urn:smpte:ul:06.0E.2B.34.04.01.01.06.04.01.01.01.03.01.00.00")),
        ITU709(UL.fromULAsURNStringToUL("urn:smpte:ul:06.0E.2B.34.04.01.01.06.04.01.01.01.03.03.00.00")),
        ITU2020(UL.fromULAsURNStringToUL("urn:smpte:ul:06.0E.2B.34.04.01.01.0D.04.01.01.01.03.04.00.00")),
        P3D65(UL.fromULAsURNStringToUL("urn:smpte:ul:06.0E.2B.34.04.01.01.0D.04.01.01.01.03.06.00.00"));

        private UL colorPrimariesUL;
        ColorPrimaries(@Nonnull UL colorPrimariesUL) {
            this.colorPrimariesUL = colorPrimariesUL;
        }

        public UL getColorPrimariesUL() {
            return this.colorPrimariesUL;
        }

        public static ColorPrimaries valueOf(UL colorPrimariesUL) {
            for(ColorPrimaries colorPrimaries: ColorPrimaries.values()) {
                if( colorPrimaries.getColorPrimariesUL().equals(colorPrimariesUL)) {
                    return colorPrimaries;
                }
            }
            return null;
        }

    }
}
