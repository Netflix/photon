package com.netflix.imflibrary.writerTools;

/**
 * A class that implements the logic to build a st2067-2:2016 schema compliant CompositionPlaylist document.
 */
public class CompositionPlaylistBuilder_2016 {
    /**
     * A method to construct a UserTextType compliant with the 2016 schema for IMF Composition Playlist documents
     * @param value the string that is a part of the annotation text
     * @param language the language code of the annotation text
     * @return a UserTextType
     */
    public static org.smpte_ra.schemas.st2067_2_2016.UserTextType buildCPLUserTextType_2016(String value, String language){
        org.smpte_ra.schemas.st2067_2_2016.UserTextType userTextType = new org.smpte_ra.schemas.st2067_2_2016.UserTextType();
        userTextType.setValue(value);
        userTextType.setLanguage(language);
        return userTextType;
    }
}
