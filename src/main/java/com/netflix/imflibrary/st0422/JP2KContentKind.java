package com.netflix.imflibrary.st0422;

public enum JP2KContentKind {
    FU((byte)0x01, "Frame-based wrapping – \"FU\" Undefined"),
    Cn((byte)0x02, "\"Cn\" Clip-based wrapping"),
    I1((byte)0x03, "\"I1\" Interlaced Frame Wrapping, 1 field per KLV Element"),
    I2((byte)0x04, "\"I2\" Interlaced Frame Wrapping, 2 fields per KLV Element"),
    F1((byte)0x05, "\"F1\" Field Wrapping, 1 field per KLV Element"),
    P1((byte)0x06, "Frame-based wrapping – \"P1\" Progressive"),
    Unknown((byte)0, "Unknown wrapping");

    private final byte contentKind;
    private final String description;

    JP2KContentKind (byte contentKind, String description){
        this.contentKind = contentKind;
        this.description = description;
    }

    public byte getContentKind() {
        return contentKind;
    }

    public static JP2KContentKind valueOf(byte value)
    {
        for (JP2KContentKind kind: JP2KContentKind.values()) {
            if (kind.getContentKind() == value) {
                return kind;
            }
        }
        return Unknown;
    }

    @Override
    public String toString() {
        return this.description;
    }
}