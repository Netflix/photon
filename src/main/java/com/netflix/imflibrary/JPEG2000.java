package com.netflix.imflibrary;

import com.netflix.imflibrary.st0377.header.UL;

public class JPEG2000 {

    public static final UL BROADCAST_PROFILE_NODE_UL = UL
            .fromULAsURNStringToUL("urn:smpte:ul:060e2b34.04010107.04010202.03010100");
    public static final UL J2K_NODE_UL = UL.fromULAsURNStringToUL("urn:smpte:ul:060e2b34.04010107.04010202.03010000");
    public static final UL HTJ2K_UL = UL.fromULAsURNStringToUL("urn:smpte:ul:060e2b34.04010107.04010202.03010801");

    public static boolean isBroadcastProfile(UL pictureEssenceCoding) {
        if (!pictureEssenceCoding.equalsWithMask(BROADCAST_PROFILE_NODE_UL, 0b1111111011111110))
            return false;

        switch (pictureEssenceCoding.getByte(15)) {
            case 0x11: /* JPEG2000BroadcastContributionSingleTileProfileLevel1 */
            case 0x12: /* JPEG2000BroadcastContributionSingleTileProfileLevel2 */
            case 0x13: /* JPEG2000BroadcastContributionSingleTileProfileLevel3 */
            case 0x14: /* JPEG2000BroadcastContributionSingleTileProfileLevel4 */
            case 0x15: /* JPEG2000BroadcastContributionSingleTileProfileLevel5 */
            case 0x16: /* JPEG2000BroadcastContributionMultiTileReversibleProfileLevel6 */
            case 0x17: /* JPEG2000BroadcastContributionMultiTileReversibleProfileLevel7 */
                return true;
        }

        return false;
    }

    public static boolean isIMF2KProfile(UL pictureEssenceCoding) {
        if (!pictureEssenceCoding.equalsWithMask(J2K_NODE_UL, 0b1111111011111100))
            return false;

        if (pictureEssenceCoding.getByte(14) == 0x02) {
            switch (pictureEssenceCoding.getByte(15)) {
                case 0x03: /* J2K_2KIMF_SingleTileLossyProfile_M1S1 */
                case 0x05: /* J2K_2KIMF_SingleTileLossyProfile_M2S1 */
                case 0x07: /* J2K_2KIMF_SingleTileLossyProfile_M3S1 */
                case 0x09: /* J2K_2KIMF_SingleTileLossyProfile_M4S1 */
                case 0x0a: /* J2K_2KIMF_SingleTileLossyProfile_M4S2 */
                case 0x0c: /* J2K_2KIMF_SingleTileLossyProfile_M5S1 */
                case 0x0d: /* J2K_2KIMF_SingleTileLossyProfile_M5S2 */
                case 0x0e: /* J2K_2KIMF_SingleTileLossyProfile_M5S3 */
                case 0x10: /* J2K_2KIMF_SingleTileLossyProfile_M6S1 */
                case 0x11: /* J2K_2KIMF_SingleTileLossyProfile_M6S2 */
                case 0x12: /* J2K_2KIMF_SingleTileLossyProfile_M6S3 */
                case 0x13: /* J2K_2KIMF_SingleTileLossyProfile_M6S4 */
                    return true;
            }
        }

        if (pictureEssenceCoding.getByte(14) == 0x05) {
            switch (pictureEssenceCoding.getByte(15)) {
                case 0x02: /* J2K_2KIMF_SingleMultiTileReversibleProfile_M1S0 */
                case 0x04: /* J2K_2KIMF_SingleMultiTileReversibleProfile_M2S0 */
                case 0x06: /* J2K_2KIMF_SingleMultiTileReversibleProfile_M3S0 */
                case 0x08: /* J2K_2KIMF_SingleMultiTileReversibleProfile_M4S0 */
                case 0x0b: /* J2K_2KIMF_SingleMultiTileReversibleProfile_M5S0 */
                case 0x0f: /* J2K_2KIMF_SingleMultiTileReversibleProfile_M6S0 */
                    return true;
            }
        }

        return false;
    }

    public static boolean isIMF4KProfile(UL pictureEssenceCoding) {
        if (!pictureEssenceCoding.equalsWithMask(J2K_NODE_UL, 0b1111111011111100))
            return false;

        if (pictureEssenceCoding.getByte(14) == 0x03) {
            switch (pictureEssenceCoding.getByte(15)) {
                case 0x03: /* J2K_4KIMF_SingleTileLossyProfile_M1S1 */
                case 0x05: /* J2K_4KIMF_SingleTileLossyProfile_M2S1 */
                case 0x07: /* J2K_4KIMF_SingleTileLossyProfile_M3S1 */
                case 0x09: /* J2K_4KIMF_SingleTileLossyProfile_M4S1 */
                case 0x0a: /* J2K_4KIMF_SingleTileLossyProfile_M4S2 */
                case 0x0c: /* J2K_4KIMF_SingleTileLossyProfile_M5S1 */
                case 0x0d: /* J2K_4KIMF_SingleTileLossyProfile_M5S2 */
                case 0x0e: /* J2K_4KIMF_SingleTileLossyProfile_M5S3 */
                case 0x10: /* J2K_4KIMF_SingleTileLossyProfile_M6S1 */
                case 0x11: /* J2K_4KIMF_SingleTileLossyProfile_M6S2 */
                case 0x12: /* J2K_4KIMF_SingleTileLossyProfile_M6S3 */
                case 0x13: /* J2K_4KIMF_SingleTileLossyProfile_M6S4 */
                case 0x15: /* J2K_4KIMF_SingleTileLossyProfile_M7S1 */
                case 0x16: /* J2K_4KIMF_SingleTileLossyProfile_M7S2 */
                case 0x17: /* J2K_4KIMF_SingleTileLossyProfile_M7S3 */
                case 0x18: /* J2K_4KIMF_SingleTileLossyProfile_M7S4 */
                case 0x19: /* J2K_4KIMF_SingleTileLossyProfile_M7S5 */
                case 0x1b: /* J2K_4KIMF_SingleTileLossyProfile_M8S1 */
                case 0x1c: /* J2K_4KIMF_SingleTileLossyProfile_M8S2 */
                case 0x1d: /* J2K_4KIMF_SingleTileLossyProfile_M8S3 */
                case 0x1e: /* J2K_4KIMF_SingleTileLossyProfile_M8S4 */
                case 0x1f: /* J2K_4KIMF_SingleTileLossyProfile_M8S5 */
                case 0x20: /* J2K_4KIMF_SingleTileLossyProfile_M8S6 */
                    return true;

            }
        }

        if (pictureEssenceCoding.getByte(14) == 0x06) {
            switch (pictureEssenceCoding.getByte(15)) {
                case 0x02: /* J2K_4KIMF_SingleMultiTileReversibleProfile_M1S0 */
                case 0x04: /* J2K_4KIMF_SingleMultiTileReversibleProfile_M2S0 */
                case 0x06: /* J2K_4KIMF_SingleMultiTileReversibleProfile_M3S0 */
                case 0x08: /* J2K_4KIMF_SingleMultiTileReversibleProfile_M4S0 */
                case 0x0b: /* J2K_4KIMF_SingleMultiTileReversibleProfile_M5S0 */
                case 0x0f: /* J2K_4KIMF_SingleMultiTileReversibleProfile_M6S0 */
                case 0x14: /* J2K_4KIMF_SingleMultiTileReversibleProfile_M7S0 */
                case 0x1a: /* J2K_4KIMF_SingleMultiTileReversibleProfile_M8S0 */
                    return true;
            }
        }

        return false;
    }

    public static boolean isAPP2HT(UL pictureEssenceCoding) {
        return pictureEssenceCoding.equalsWithMask(HTJ2K_UL, 0b1111111011111111);
    }

}
