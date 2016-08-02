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

package com.netflix.imflibrary.writerTools.common;

/**
 * A class representing the construction logic of all the common components of IMF documents - AssetMap, PackingList and CompositionPlaylist
 */
public final class IMFDocumentComponents {

    /**
     * To prevent instantiation
     */
    private IMFDocumentComponents(){

    }

    /**
     * A method to construct a UserTextType compliant with the 2013 schema for IMF CompositionPlaylist documents
     * @param value the string that is a part of the annotation text
     * @param language the language code of the annotation text
     * @return a UserTextType
     */
    public static org.smpte_ra.schemas.st2067_2_2013.UserTextType buildCPLUserTextType_2013(String value, String language){
        org.smpte_ra.schemas.st2067_2_2013.UserTextType userTextType = new org.smpte_ra.schemas.st2067_2_2013.UserTextType();
        userTextType.setValue(value);
        userTextType.setLanguage(language);
        return userTextType;
    }

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

    /**
     * A method to construct a UserTextType compliant with the 2007 schema for IMF AssetMap documents
     * @param value the string that is a part of the annotation text
     * @param language the language code of the annotation text
     * @return a UserTextType
     */
    public static org.smpte_ra.schemas.st0429_9_2007.AM.UserText buildAssetMapUserTextType_2007(String value, String language){
        org.smpte_ra.schemas.st0429_9_2007.AM.UserText userTextType = new org.smpte_ra.schemas.st0429_9_2007.AM.UserText();
        userTextType.setValue(value);
        userTextType.setLanguage(language);
        return userTextType;
    }
}
