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

package com.netflix.imflibrary.writerTools;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.writerTools.utils.IMFUUIDGenerator;
import com.netflix.imflibrary.writerTools.utils.IMFUtils;
import org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType;
import org.smpte_ra.schemas.st2067_2_2013.CompositionTimecodeType;
import org.smpte_ra.schemas.st2067_2_2013.ContentMaturityRatingType;
import org.smpte_ra.schemas.st2067_2_2013.ContentVersionType;
import org.smpte_ra.schemas.st2067_2_2013.LocaleType;
import org.w3c.dom.Node;

import javax.annotation.Nonnull;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.File;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A class that implements the logic to build a SMPTE st2067-2:2013 schema compliant CompositionPlaylist document.
 */
public class CompositionPlaylistBuilder_2013 {

    private final UUID uuid;
    private final XMLGregorianCalendar issueDate;
    private final List<Long> editRate;
    private final File workingDirectory;
    private final IMFErrorLogger imfErrorLogger;
    private final Map<Node, String> essenceDescriptorIDMap = new HashMap<>();

    private static String defaultContentKindScope = "http://www.smpte-ra.org/schemas/2067-3/XXXX#content-kind";


    /**
     * A constructor for CompositionPlaylistBuilder class to build a CompositionPlaylist document compliant with st2067-2:2013 schema
     * @param uuid identifying the CompositionPlaylist document
     * @param issueDate date at which the CompositionPlaylist was issued
     * @param editRate the composition EditRate
     * @param workingDirectory a folder location where the generated CompositionPlaylist Document will be written to
     * @param imfErrorLogger a logger object to record errors that occur during the creation of the CompositionPlaylist document
     */
    public CompositionPlaylistBuilder_2013(UUID uuid, XMLGregorianCalendar issueDate, List<Long> editRate, File workingDirectory, IMFErrorLogger imfErrorLogger){
        this.uuid = uuid;
        this.issueDate = issueDate;
        this.editRate = Collections.unmodifiableList(editRate);
        this.workingDirectory = workingDirectory;
        this.imfErrorLogger = imfErrorLogger;
    }

    /**
     * A method to construct a UserTextType compliant with the 2013 schema for IMF CompositionPlaylist documents
     * @param value the string that is a part of the annotation text
     * @param language the language code of the annotation text
     * @return a UserTextType conforming to the 2013 schema
     */
    public static org.smpte_ra.schemas.st2067_2_2013.UserTextType buildCPLUserTextType_2013(String value, String language){
        org.smpte_ra.schemas.st2067_2_2013.UserTextType userTextType = new org.smpte_ra.schemas.st2067_2_2013.UserTextType();
        userTextType.setValue(value);
        userTextType.setLanguage(language);
        return userTextType;
    }

    /**
     * A method to construct a ContentKindType object conforming to the 2013 schema
     * @param value the string correspding to the Content Kind
     * @param scope a string corresponding to the scope attribute of a Content Kind
     * @return a ContentKind object conforming to the 2013 schema
     */
    public org.smpte_ra.schemas.st2067_2_2013.ContentKindType buildContentKindType(@Nonnull String value, String scope)  {

        org.smpte_ra.schemas.st2067_2_2013.ContentKindType contentKindType = new org.smpte_ra.schemas.st2067_2_2013.ContentKindType();
        if(!scope.matches("^[a-zA-Z0-9._-]+") == true) {
            this.imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("The ContentKind scope %s does not follow the syntax of a valid URI (a-z, A-Z, 0-9, ., _, -)", scope)));
            contentKindType.setScope(scope);
        }
        else{
            contentKindType.setScope(scope);
        }
        contentKindType.setValue(value);
        return contentKindType;
    }

    /**
     * A method to construct a ContentVersionType object conforming to the 2013 schema
     * @param id URI corresponding to the content version type
     * @param value a UserTextType representing the value attribute of the ContentVersion
     * @return a content version object conforming to the 2013 schema
     * @throws URISyntaxException any syntax errors with the id attribute is exposed through a URISyntaxException
     */
    public org.smpte_ra.schemas.st2067_2_2013.ContentVersionType buildContentVersionType(String id, org.smpte_ra.schemas.st2067_2_2013.UserTextType value) throws URISyntaxException {
        ContentVersionType contentVersionType = new ContentVersionType();
        if(!id.matches("^[a-zA-Z0-9._-]+") == true) {
            //this.imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("The ContentKind scope %s does not follow the syntax of a valid URI (a-z, A-Z, 0-9, ., _, -)", id)));
            throw new URISyntaxException("Invalid URI", "The ContentVersion Id %s does not follow the syntax of a valid URI (a-z, A-Z, 0-9, ., _, -)");
        }
        contentVersionType.setId(id);
        contentVersionType.setLabelText(value);
        return contentVersionType;
    }

    /**
     * A method to construct a ContentVersionList object conforming to the 2013 schema
     * @param contentVersions a list of ContentVersion objects conforming to the 2013 schema
     * @return a content version list object conforming to the 2013 schema
     */
    public org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType.ContentVersionList buildContentVersionList(List<org.smpte_ra.schemas.st2067_2_2013.ContentVersionType> contentVersions){
        org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType.ContentVersionList contentVersionList = new CompositionPlaylistType.ContentVersionList();
        contentVersionList.getContentVersion().addAll(contentVersions);
        return contentVersionList;
    }

    /**
     * A method to construct an EssenceDescriptorBaseType object conforming to the 2013 schema
     * @param node a regxml representation of an EssenceDescriptor
     * @return a EssenceDescriptorBaseType object conforming to the 2013 schema
     */
    public org.smpte_ra.schemas.st2067_2_2013.EssenceDescriptorBaseType buildEssenceDescriptorBaseType(Node node){
        org.smpte_ra.schemas.st2067_2_2013.EssenceDescriptorBaseType essenceDescriptorBaseType = new org.smpte_ra.schemas.st2067_2_2013.EssenceDescriptorBaseType();
        String id = IMFUUIDGenerator.getInstance().getUrnUUID();
        essenceDescriptorBaseType.setId(id);
        this.essenceDescriptorIDMap.put(node, id);
        essenceDescriptorBaseType.getAny().add(node);
        return essenceDescriptorBaseType;
    }

    /**
     * A method to construct an EssenceDescriptorList conforming to the 2013 schema
     * @param essenceDescriptorBaseTypes a list of EssenceDescritorBaseType objects conforming to the 2013 schema
     * @return EssenceDescriptorList type object
     */
    public org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType.EssenceDescriptorList buildEssenceDescriptorList(List<org.smpte_ra.schemas.st2067_2_2013.EssenceDescriptorBaseType> essenceDescriptorBaseTypes){
        org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType.EssenceDescriptorList essenceDescriptorList = new org.smpte_ra.schemas.st2067_2_2013.CompositionPlaylistType.EssenceDescriptorList();
        essenceDescriptorList.getEssenceDescriptor().addAll(essenceDescriptorBaseTypes);
        return essenceDescriptorList;
    }

    /**
     * A method to construct a CompositionTimecodeType conforming to the 2013 schema
     * @param compositionEditRate the EditRate corresponding to the Composition's EditRate
     * @return a CompositionTimecodeType conforming to the 2013 schema
     */
    public org.smpte_ra.schemas.st2067_2_2013.CompositionTimecodeType buildCompositionTimeCode(BigInteger compositionEditRate){
        org.smpte_ra.schemas.st2067_2_2013.CompositionTimecodeType compositionTimecodeType = new CompositionTimecodeType();
        compositionTimecodeType.setTimecodeDropFrame(false);/*TimecodeDropFrame set to false by default*/
        compositionTimecodeType.setTimecodeRate(compositionEditRate);
        compositionTimecodeType.setTimecodeStartAddress(IMFUtils.generateTimecodeStartAddress());
        return compositionTimecodeType;
    }


    /**
     * A method to construct a LocaleType conforming to the 2013 schema
     * @param annotationText for the localeType
     * @param languages a list of string representing Language Tags as specified in RFC-5646
     * @param regions a list of strings representing regions
     * @param contentMaturityRatings a list of ContentMaturityRating objects conforming to the 2013 schema
     * @return a LocaleType object conforming to the 2013 schema
     */
    public org.smpte_ra.schemas.st2067_2_2013.LocaleType buildLocaleType(org.smpte_ra.schemas.st2067_2_2013.UserTextType annotationText,
                                                                         List<String> languages,
                                                                         List<String> regions,
                                                                         List<org.smpte_ra.schemas.st2067_2_2013.ContentMaturityRatingType> contentMaturityRatings){
        org.smpte_ra.schemas.st2067_2_2013.LocaleType localeType = new org.smpte_ra.schemas.st2067_2_2013.LocaleType();
        localeType.setAnnotation(annotationText);
        org.smpte_ra.schemas.st2067_2_2013.LocaleType.LanguageList languageList = new org.smpte_ra.schemas.st2067_2_2013.LocaleType.LanguageList();
        languageList.getLanguage().addAll(languages);
        localeType.setLanguageList(languageList);
        org.smpte_ra.schemas.st2067_2_2013.LocaleType.RegionList regionList = new org.smpte_ra.schemas.st2067_2_2013.LocaleType.RegionList();
        regionList.getRegion().addAll(regions);
        localeType.setRegionList(regionList);
        org.smpte_ra.schemas.st2067_2_2013.LocaleType.ContentMaturityRatingList contentMaturityRatingList = new org.smpte_ra.schemas.st2067_2_2013.LocaleType.ContentMaturityRatingList();
        contentMaturityRatingList.getContentMaturityRating().addAll(contentMaturityRatings);
        return localeType;
    }

    /**
     * A method to construct a ContentMaturityRatingType
     * @param agency a string representing the agency that issued the rating for this Composition
     * @param rating a human-readable representation of the rating of this Composition
     * @param audience a human-readable representation of the intended target audience of this Composition
     * @return a ContentMaturityRating object conforming to the 2013 schema
     * @throws URISyntaxException any syntax errors with the agency attribute is exposed through a URISyntaxException
     */
    public org.smpte_ra.schemas.st2067_2_2013.ContentMaturityRatingType buildContentMaturityRatingType(String agency, String rating, org.smpte_ra.schemas.st2067_2_2013.ContentMaturityRatingType.Audience audience) throws URISyntaxException {
        org.smpte_ra.schemas.st2067_2_2013.ContentMaturityRatingType contentMaturityRatingType = new org.smpte_ra.schemas.st2067_2_2013.ContentMaturityRatingType();
        if(!agency.matches("^[a-zA-Z0-9._-]+") == true) {
            //this.imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("The ContentKind scope %s does not follow the syntax of a valid URI (a-z, A-Z, 0-9, ., _, -)", id)));
            throw new URISyntaxException("Invalid URI", "The ContentMaturityRating agency %s does not follow the syntax of a valid URI (a-z, A-Z, 0-9, ., _, -)");
        }
        contentMaturityRatingType.setAgency(agency);
        contentMaturityRatingType.setRating(rating);
        contentMaturityRatingType.setAudience(audience);
        return contentMaturityRatingType;
    }

}
