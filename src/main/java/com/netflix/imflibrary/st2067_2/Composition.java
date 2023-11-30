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

package com.netflix.imflibrary.st2067_2;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.RESTfulInterfaces.IMPValidator;
import com.netflix.imflibrary.RESTfulInterfaces.PayloadRecord;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.FileByteRangeProvider;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * This class represents a canonical model of the XML type 'CompositionPlaylistType' defined by SMPTE st2067-3,
 * A Composition object can be constructed from an XML file only if it satisfies all the constraints specified
 * in st2067-3 and st2067-2. This object model is intended to be agnostic of specific versions of the definitions of a
 * CompositionPlaylist(st2067-3) and its accompanying Core constraints(st2067-2).
 */
@Immutable
public final class Composition {
    private static final Logger logger = LoggerFactory.getLogger(Composition.class);

    private Composition() {

    }

    /**
     * This class is an immutable implementation of a rational number described as a ratio of two longs and used to hold
     * non-integer frame rate values
     */
    @Immutable
    public static final class EditRate {
        private final Long numerator;
        private final Long denominator;
        private final IMFErrorLogger imfErrorLogger;

        /**
         * Constructor for the rational frame rate number.
         *
         * @param numbers the input list of numbers. The first number in the list is treated as the numerator and the
         *                second as
         *                the denominator. Construction succeeds only if the list has exactly two numbers
         */
        public EditRate(List<Long> numbers) {
            Long denominator = 1L;
            Long numerator = 1L;
            imfErrorLogger = new IMFErrorLoggerImpl();
            if (numbers.size() != 2) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors
                        .ErrorLevels.NON_FATAL, String.format(
                        "Input list is expected to contain 2 numbers representing numerator and denominator " +
                                "respectively, found %d numbers in list %s",
                        numbers.size(), Arrays.toString(numbers.toArray())));

            } else if (numbers.get(0) == 0
                    || numbers.get(1) == 0) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors
                        .ErrorLevels.NON_FATAL, String.format(
                        "Input list is expected to contain 2 non-zero numbers representing numerator and denominator " +
                                "of the EditRate respectively, found Numerator %d, Denominator %d",
                        numbers.get(0), numbers.get(1)));
            }
            else {
                numerator = numbers.get(0);
                denominator = numbers.get(1);
            }

            if(imfErrorLogger.hasFatalErrors())
            {
                throw new IMFException("Failed to create IMFBaseResourceType", imfErrorLogger);
            }

            this.numerator = numerator;
            this.denominator = denominator;
        }

        /**
         * Constructor for the rational frame rate number
         * @param numerator a long integer representing the numerator component of the EditRate
         * @param denominator a long integer representing the denominator component of the EditRate
         */
        public EditRate(Long numerator, Long denominator){
            this(new ArrayList<Long>(){{add(numerator); add(denominator);}});
        }

        /**
         * Getter for the frame rate numerator
         *
         * @return a long value corresponding to the frame rate numerator
         */
        public Long getNumerator() {
            return this.numerator;
        }

        /**
         * Getter for the frame rate denominator
         *
         * @return a long value corresponding to the frame rate denominator
         */
        public Long getDenominator() {
            return this.denominator;
        }

        /**
         * A method that returns a string representation of a Composition object
         *
         * @return string representing the object
         */
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=================== EditRate =====================\n");
            sb.append(String.format("numerator = %d, denominator = %d%n", this.numerator, this.denominator));
            return sb.toString();
        }

        /**
         * Overridden equals method.
         *
         * @param object the EditRate to be compared with.
         * @return boolean false if the object is null or is not an instance of the EditRate class.
         */
        @Override
        public boolean equals(Object object) {
            if (object == null
                    || !(object instanceof EditRate)) {
                return false;
            }
            EditRate other = (EditRate) object;
            return ((this.getNumerator().equals(other.getNumerator())) && (this.getDenominator().equals(other.getDenominator())));
        }

        /**
         * A Java compliant implementation of the hashCode() method
         *
         * @return integer containing the hash code corresponding to this object
         */
        @Override
        public int hashCode() {
            int hash = 1;
            hash = hash * 31 + this.numerator.hashCode(); /*Numerator can be used since it is non-null*/
            hash = hash * 31
                    + this.denominator.hashCode();/*Another field that is indicated to be non-null*/
            return hash;
        }
    }

    /**
     * This class enumerates various types of {@link org.smpte_ra.schemas._2067_3._2013.SequenceType Sequence} that are valid in
     * Composition document that is compliant with st2067-2:2013. Such types are mostly defined in Section 6.3 of st2067-2:2013
     */
    public static enum SequenceTypeEnum {
        MarkerSequence("MarkerSequence"),
        MainImageSequence("MainImageSequence"),
        MainAudioSequence("MainAudioSequence"),
        SubtitlesSequence("SubtitlesSequence"),
        HearingImpairedCaptionsSequence("HearingImpairedCaptionsSequence"),
        VisuallyImpairedTextSequence("VisuallyImpairedTextSequence"),
        CommentarySequence("CommentarySequence"),
        KaraokeSequence("KaraokeSequence"),
        ForcedNarrativeSequence("ForcedNarrativeSequence"),
        AncillaryDataSequence("AncillaryDataSequence"),
        IABSequence("IABSequence"),
        MGASADMSignalSequence("MGASADMSignalSequence"),
        ADMAudioSequence("ADMAudioSequence"),
        UnsupportedSequence("UnsupportedSequence");

        private final String name;

        private SequenceTypeEnum(String name) {
            this.name = name;
        }

        /**
         * A getter for the SequenceTypeEnum given a string that represents the name of a SequenceTypeEnum
         *
         * @param name the string that should represent the SequenceTypeEnum
         * @return the SequenceTypeEnum value corresponding to the name that was passed
         */
        public static SequenceTypeEnum getSequenceTypeEnum(String name) {
            switch (name) {
                case "MarkerSequence":
                    return MarkerSequence;
                case "MainImageSequence":
                    return MainImageSequence;
                case "MainAudioSequence":
                    return MainAudioSequence;
                case "SubtitlesSequence":
                    return SubtitlesSequence;
                case "HearingImpairedCaptionsSequence":
                    return HearingImpairedCaptionsSequence;
                case "VisuallyImpairedTextSequence":
                    return VisuallyImpairedTextSequence;
                case "CommentarySequence":
                    return CommentarySequence;
                case "KaraokeSequence":
                    return KaraokeSequence;
                case "ForcedNarrativeSequence":
                    return ForcedNarrativeSequence;
                case "AncillaryDataSequence":
                    return AncillaryDataSequence;
                case "IABSequence":
                    return IABSequence;
                case "MGASADMSignalSequence":
                    return MGASADMSignalSequence;
                case "ADMAudioSequence":
                    return ADMAudioSequence;
                case "UnsupportedSequence":
                default:
                    return UnsupportedSequence;
            }
        }

        /**
         * An override of the toString() method
         *
         * @return a string representing the SequenceTypeEnum
         */
        @Override
        public String toString() {
            return this.name;
        }

    }

    /**
     * The class is an immutable implementation of the virtual track concept defined in Section 6.9.3 of st2067-3:2013. A
     * virtual track is characterized by its UUID, the type of sequence and a list of UUIDs of the
     * IMF track files that comprise it.
     */
    @Immutable
    public abstract static class VirtualTrack {
        protected final UUID trackID;
        protected final SequenceTypeEnum sequenceTypeEnum;
        protected final List<? extends IMFBaseResourceType> resources;
        protected final Composition.EditRate compositionEditRate;

        /**
         * Constructor for a VirtualTrack object
         *
         * @param trackID          the UUID associated with this VirtualTrack object
         * @param sequenceTypeEnum the type of the associated sequence
         * @param resources        the resource list of the Virtual Track
         * @param compositionEditRate the edit rate of the composition
         */
        public VirtualTrack(UUID trackID, SequenceTypeEnum sequenceTypeEnum, List<? extends IMFBaseResourceType> resources, Composition.EditRate compositionEditRate) {
            this.trackID = trackID;
            this.sequenceTypeEnum = sequenceTypeEnum;
            this.resources = resources;
            this.compositionEditRate = compositionEditRate;
        }

        /**
         * Getter for the sequence type associated with this VirtualTrack object
         *
         * @return the sequence type associated with this VirtualTrack object as an enum
         */
        public SequenceTypeEnum getSequenceTypeEnum() {
            return this.sequenceTypeEnum;
        }

        /**
         * Getter for the UUID associated with this VirtualTrack object
         *
         * @return the UUID associated with the Virtual track
         */
        public UUID getTrackID() {
            return this.trackID;
        }

        /**
         * Getter for the Resources of the Virtual Track
         *
         * @return an unmodifiable list of resources of the Virtual Track
         */
        public List<? extends IMFBaseResourceType> getResourceList() {
            return Collections.unmodifiableList(this.resources);
        }

        /**
         * A method to return the duration of this VirtualTrack
         * @return a long integer representing the duration of this VirtualTrack in Track Edit Rate units
         */
        public long getDurationInTrackEditRateUnits(){
            long duration = 0L;
            for(IMFBaseResourceType imfBaseResourceType : this.resources){
                // Only handle TrackFileResource sequences currently
                if (imfBaseResourceType instanceof IMFTrackFileResourceType) {
                    duration += imfBaseResourceType.getSourceDuration().longValue() * imfBaseResourceType.getRepeatCount().longValue();
                }
            }
            return duration;
        }

        /**
         * A method to return the duration of this VirtualTrack
         * @return a long integer representing the duration of this VirtualTrack in Composition Edit Rate units
         */
        public long getDuration(){
            long duration = getDurationInTrackEditRateUnits();
            Composition.EditRate resourceEditRate = this.resources.get(0).getEditRate();//Resources of this virtual track should all have the same edit rate we enforce that check during IMFCoreConstraintsChecker.checkVirtualTracks()
            long durationInCompositionEditUnits = Math.round((double) duration * (((double)this.compositionEditRate.getNumerator()/this.compositionEditRate.getDenominator()) / ((double)resourceEditRate.getNumerator()/resourceEditRate.getDenominator())));
            return durationInCompositionEditUnits;
        }

        /**
         * A method to determine the equivalence of any 2 virtual tracks.
         *
         * @param other - the object to compare against
         * @return boolean indicating if the 2 virtual tracks are equivalent or represent the same timeline
         */
        public boolean equivalent(Composition.VirtualTrack other) {
            if (other == null
                    || (!this.getSequenceTypeEnum().equals(other.getSequenceTypeEnum()))
                    || (this.resources.size() == 0 || other.resources.size() == 0)) {
                return false;
            }

            List<? extends IMFBaseResourceType> otherResourceList = other.resources;
            boolean result = false;
            if(this instanceof IMFEssenceComponentVirtualTrack){
                if(this.getDuration() != other.getDuration()){
                    return false;
                }
                IMFEssenceComponentVirtualTrack thisVirtualTrack = IMFEssenceComponentVirtualTrack.class.cast(this);
                IMFEssenceComponentVirtualTrack otherVirtualTrack = IMFEssenceComponentVirtualTrack.class.cast(other);
                List<IMFTrackFileResourceType> normalizedResourceList = this.normalizeResourceList(thisVirtualTrack.getTrackFileResourceList());
                List<IMFTrackFileResourceType> normalizedOtherResourceList = this.normalizeResourceList(otherVirtualTrack.getTrackFileResourceList());
                if(normalizedResourceList.size() != normalizedOtherResourceList.size()){
                    return false;
                }
                result = normalizedResourceList.get(0).equivalent(normalizedOtherResourceList.get(0));
                for (int i = 1; i < normalizedResourceList.size(); i++) {
                    IMFBaseResourceType thisResource = normalizedResourceList.get(i);
                    IMFBaseResourceType otherResource = normalizedOtherResourceList.get(i);

                    result &= thisResource.equivalent(otherResource);
                }
            }
            else{
                result = this.resources.get(0).equivalent(otherResourceList.get(0));
                for (int i = 1; i < this.resources.size(); i++) {
                    IMFBaseResourceType thisResource = this.resources.get(i);
                    IMFBaseResourceType otherResource = otherResourceList.get(i);

                    result &= thisResource.equivalent(otherResource);
                }
            }

            return result;
        }

        private List<IMFTrackFileResourceType> normalizeResourceList(List<IMFTrackFileResourceType> resourceList){
            List<IMFTrackFileResourceType> normalizedResourceList = new ArrayList<>();
            IMFTrackFileResourceType prev = resourceList.get(0);
            for(int i=1; i< resourceList.size(); i++){
                IMFTrackFileResourceType curr = IMFTrackFileResourceType.class.cast(resourceList.get(i));
                if(curr.getTrackFileId().equals(prev.getTrackFileId())
                        && curr.getEditRate().equals(prev.getEditRate())
                        && curr.getEntryPoint().longValue() == (prev.getEntryPoint().longValue() + prev.getSourceDuration().longValue())){
                    //Candidate for normalization - We could create one resource representing the timelines of prev and curr
                    List<Long> editRate = new ArrayList<>();
                    editRate.add(prev.getEditRate().getNumerator());
                    editRate.add(prev.getEditRate().getDenominator());

                    if(prev.getRepeatCount().longValue() > 1) {
                        BigInteger newRepeatCount = new BigInteger(String.format("%d", prev.getRepeatCount().longValue() - 1L));
                        IMFTrackFileResourceType modifiedPrevTrackFileResourceType =
                                new IMFTrackFileResourceType(prev.getId(),
                                        prev.getTrackFileId(),
                                        editRate,
                                        prev.getIntrinsicDuration(),
                                        prev.getEntryPoint(),
                                        prev.getSourceDuration(),
                                        newRepeatCount,
                                        prev.getSourceEncoding(),
                                        prev.getHash(),
                                        prev.getHashAlgorithm());
                        normalizedResourceList.add(modifiedPrevTrackFileResourceType);
                    }

                    BigInteger newSourceDuration = new BigInteger(String.format("%d", curr.getSourceDuration().longValue() + prev.getSourceDuration().longValue()));
                    IMFTrackFileResourceType mergedTrackFileResourceType = new IMFTrackFileResourceType(prev.getId(),
                            prev.getTrackFileId(),
                            editRate,
                            prev.getIntrinsicDuration(),
                            prev.getEntryPoint(),
                            newSourceDuration,
                            new BigInteger("1"),
                            prev.getSourceEncoding(),
                            prev.getHash(),
                            prev.getHashAlgorithm());
                    prev = mergedTrackFileResourceType;

                    if(curr.getRepeatCount().longValue() > 1) {
                        BigInteger newRepeatCount = new BigInteger(String.format("%d", curr.getRepeatCount().longValue() - 1L));
                        editRate = new ArrayList<>();
                        editRate.add(curr.getEditRate().getNumerator());
                        editRate.add(curr.getEditRate().getDenominator());
                        IMFTrackFileResourceType modifiedCurrTrackFileResourceType =
                                new IMFTrackFileResourceType(curr.getId(),
                                        curr.getTrackFileId(),
                                        editRate,
                                        curr.getIntrinsicDuration(),
                                        curr.getEntryPoint(),
                                        curr.getSourceDuration(),
                                        newRepeatCount,
                                        curr.getSourceEncoding(),
                                        curr.getHash(),
                                        curr.getHashAlgorithm());
                        //We are replacing prev here so add the mergedTrackFileResourceType, modifiedCurrTrackFileResourceType will be added when we process the last resource in the list
                        normalizedResourceList.add(prev);
                        prev = modifiedCurrTrackFileResourceType;
                    }
                }
                else{//prev and curr cannot be merged as they either point to different resources or do not represent continuous timeline
                    normalizedResourceList.add(prev);
                    prev = curr;
                }
            }
            normalizedResourceList.add(prev);//Add the track file resource pointed to by prev

            return Collections.unmodifiableList(normalizedResourceList);
        }
    }

    private static String usage()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Usage:%n"));
        sb.append(String.format("%s <inputFilePath>%n", Composition.class.getName()));
        return sb.toString();
    }

    public static void main(String args[]) throws IOException, SAXException, JAXBException
    {
        if (args.length != 1)
        {
            logger.error(usage());
            throw new IllegalArgumentException("Invalid parameters");
        }

        File inputFile = new File(args[0]);
        if(!inputFile.exists()){
            logger.error(String.format("File %s does not exist", inputFile.getAbsolutePath()));
            System.exit(-1);
        }
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        byte[] bytes = resourceByteRangeProvider.getByteRangeAsBytes(0, resourceByteRangeProvider.getResourceSize()-1);
        PayloadRecord payloadRecord = new PayloadRecord(bytes, PayloadRecord.PayloadAssetType.CompositionPlaylist, 0L, resourceByteRangeProvider.getResourceSize());
        List<ErrorLogger.ErrorObject>errors = IMPValidator.validateCPL(payloadRecord);

        if(errors.size() > 0){
            long warningCount = errors.stream().filter(e -> e.getErrorLevel().equals(IMFErrorLogger.IMFErrors.ErrorLevels
                    .WARNING)).count();
            logger.info(String.format("CompositionPlaylist Document has %d errors and %d warnings",
                    errors.size() - warningCount, warningCount));
            for(ErrorLogger.ErrorObject errorObject : errors){
                if(errorObject.getErrorLevel() != IMFErrorLogger.IMFErrors.ErrorLevels.WARNING) {
                    logger.error(errorObject.toString());
                }
                else if(errorObject.getErrorLevel() == IMFErrorLogger.IMFErrors.ErrorLevels.WARNING) {
                    logger.warn(errorObject.toString());
                }
            }
        }
        else{
            logger.info("No errors were detected in the CompositionPlaylist Document.");
        }
    }

    /**
     * An object model for a HeaderPartition and access to the raw bytes corresponding to the HeaderPartition
     */
    public static class HeaderPartitionTuple {
        private final HeaderPartition headerPartition;
        private final ResourceByteRangeProvider resourceByteRangeProvider;

        public HeaderPartitionTuple(HeaderPartition headerPartition, ResourceByteRangeProvider resourceByteRangeProvider){
            this.headerPartition = headerPartition;
            this.resourceByteRangeProvider = resourceByteRangeProvider;
        }

        /**
         * A getter for the resourceByteRangeProvider object corresponding to this HeaderPartition to allow
         * access to the raw bytes
         * @return ResourceByteRangeProvider object corresponding to this HeaderPartition
         */
        public ResourceByteRangeProvider getResourceByteRangeProvider(){
            return this.resourceByteRangeProvider;
        }

        /**
         * A getter for the HeaderPartition object corresponding to a resource referenced from the Composition
         * @return HeaderPartition of a certain resource in the Composition
         */
        public HeaderPartition getHeaderPartition(){
            return this.headerPartition;
        }
    }

}