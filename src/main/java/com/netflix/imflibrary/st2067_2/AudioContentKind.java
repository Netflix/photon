package com.netflix.imflibrary.st2067_2;

/**
 * This enum lists all audio content kinds defined in the following draft specification:
 * "Interoperable Master Format –Specifying Audio Element and Content Kind in Application #2E Compositions"
 */
public enum AudioContentKind {
    PRM("Primary"),
    DXC("Dialog Composite"),
    MXC("Music Composite"),
    FXC("Effects Composite"),
    NAR("Narration"),
    DDXC("Dialog Composite (dubbed)"),
    MEC("Music and Effects Composite"),
    VO("Voice Over"),
    HI("Hearing Impaired"),
    VI("Visually Impaired"),
    DVS("Descriptive Video Service"),
    DCM("Director Commentary"),
    TCM("Technical Commentary"),
    WCM("Writer's Commentary"),
    MCM("Composer's Commentary"),
    CCM("Cast Commentary"),
    DXS("Dialog Split Track"),
    MXS("Music Split Track"),
    FXS("Effects Split Track"),
    SPRM("Simplified Primary"),
    OP("Optional Music and Effects"),
    SOP("Simplified Optional Music and Effects"),
    Unknown("Unknown");

    String description;
    AudioContentKind(String description) {
        this.description = description;
    }

    /**
     * Getter for description for the audio content kind
     * @return a String describing the audio content kind
     */
    public String getDescription() {
        return description;
    }

    /**
     * This method map audio content kind symbol to corresponding enum
     * @param value a Symbol representing audio content kind
     * @return AudioContentKind enumeration for the symbol if present otherwise returns Unknown enumeration
     */
    public static AudioContentKind getAudioContentKindFromSymbol(String value) {
        if(value == null) {
            return Unknown;
        }
        try {
            return AudioContentKind.valueOf(value);
        } catch(IllegalArgumentException e) {
            return Unknown;
        }
    }
}
