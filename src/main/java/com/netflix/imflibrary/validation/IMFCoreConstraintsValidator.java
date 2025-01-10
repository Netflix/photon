package com.netflix.imflibrary.validation;


import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.RESTfulInterfaces.PayloadRecord;
import com.netflix.imflibrary.st0377.header.GenericPictureEssenceDescriptor;
import com.netflix.imflibrary.st2067_2.*;
import com.netflix.imflibrary.utils.DOMNodeObjectModel;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.RegXMLLibDictionary;
import com.netflix.imflibrary.utils.UUIDHelper;
import jakarta.annotation.Nonnull;

import java.util.*;

import static com.netflix.imflibrary.validation.ConstraintsValidatorUtils.checkVirtualTrackHomogeneity;

abstract public class IMFCoreConstraintsValidator implements ConstraintsValidator {

    @Override
    public List<ErrorLogger.ErrorObject> validateEssencePartitionConstraints(@Nonnull PayloadRecord headerPartition, @Nonnull List<PayloadRecord> indexPartitionPayloads) {
        return List.of();
    }

    protected static final String MAIN_IMAGE_SEQUENCE = "MainImageSequence";
    protected static final String MAIN_AUDIO_SEQUENCE = "MainAudioSequence";

    protected static final String SUBTITLES_SEQUENCE = "SubtitlesSequence";
    protected static final String HEARING_IMPAIRED_CAPTIONS_SEQUENCE = "HearingImpairedCaptionsSequence";
    protected static final String VISUALLY_IMPAIRED_SEQUENCE = "VisuallyImpairedTextSequence";
    protected static final String COMMENTARY_SEQUENCE = "CommentarySequence";
    protected static final String KARAOKE_SEQUENCE = "KaraokeSequence";
    protected static final String FORCED_NARRATIVE_SEQUENCE = "ForcedNarrativeSequence";

    // todo: this list should only contain items related to core constraints and others should move into specific app/plugin classes
    private static final Set<String> homogeneitySelectionSet = new HashSet<String>(){{
        add("CDCIDescriptor");
        add("RGBADescriptor");
        add("SubDescriptors");
        add("JPEG2000SubDescriptor");
        add("WAVEPCMDescriptor");
        add("StoredWidth");
        add("StoredHeight");
        add("FrameLayout");
        add("SampleRate");
        add("PixelLayout");
        add("ColorPrimaries");
        add("TransferCharacteristic");
        add("PictureCompression");
        add("ComponentMaxRef");
        add("ComponentMinRef");
        add("BlackRefLevel");
        add("WhiteRefLevel");
        add("ColorRange");
        add("ColorSiting");
        add("ComponentDepth");
        add("HorizontalSubsampling");
        add("VerticalSubsampling");
        add("Xsiz");
        add("Ysiz");
        add("Csiz");
        add("J2CLayout");
        add("RGBAComponent");
        add("Code");
        add("ComponentSize");
        add("PictureComponentSizing");
        add("J2KComponentSizing");
        add("Ssiz");
        add("XRSiz");
        add("YRSiz");
        add("AudioSampleRate");
        add("QuantizationBits");
    }};

    abstract protected String getNamespaceURI();


    protected List<ErrorLogger.ErrorObject> checkNamespaceURI(IMFCompositionPlaylist imfCompositionPlaylist) {

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        String expectedCCNs = CoreConstraints.fromApplicationId(imfCompositionPlaylist.getApplicationIdSet());
        if (expectedCCNs != null && !expectedCCNs.equals(getNamespaceURI())) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.WARNING,
                    String.format("The Application Identification(s) contained in this CPL assume Core Constraints Namespace %s, but %s is used.",
                            expectedCCNs, getNamespaceURI()));
        }

        return imfErrorLogger.getErrors();
    }



    /**
     * Checks that there is only one video track and at least one audio track and that
     * for each virtual track in the given virtual track map that:
     * - the track is made of supported sequences
     * - the CPL edit rate matches one of the MainImageSequence edit rate
     * - the resources are valid see checkVirtualTrackResourceList
     * - each resource has a corresponding essence descriptor
     * - the CPL and descriptor rates match
     * - the descriptors are homogeneous
     *
     * @param imfCompositionPlaylist CPL object
     * @return a list of errors
     */
    protected static List<ErrorLogger.ErrorObject> checkVirtualTracks(IMFCompositionPlaylist imfCompositionPlaylist) {

        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        Map<UUID, DOMNodeObjectModel> essenceDescriptorListMap = imfCompositionPlaylist.getEssenceDescriptorListMap();

        // check if there's exactly one main image sequence virtual track
        // Section 6.3.1 st2067-2:2016 and Section 6.9.3 st2067-3:2016
        long mainImageSequenceCount = imfCompositionPlaylist.getVirtualTrackMap().entrySet().stream()
                .map(Map.Entry::getValue)
                .map(virtualTrack -> imfCompositionPlaylist.getSequenceTypeForVirtualTrackID(virtualTrack.getTrackID()))
                .filter(virtualTrackSequenceName -> virtualTrackSequenceName.equals(MAIN_IMAGE_SEQUENCE))
                .count();

        if (mainImageSequenceCount != 1) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("The Composition contains % main image sequence virtual tracks in its first segment, exactly one is required", mainImageSequenceCount));
            return imfErrorLogger.getErrors();
        }

        RegXMLLibDictionary regXMLLibDictionary = new RegXMLLibDictionary();

        // iterate over virtual tracks
        for (Map.Entry<UUID, ? extends Composition.VirtualTrack> virtualTrackEntry : imfCompositionPlaylist.getVirtualTrackMap().entrySet()) {
            Composition.VirtualTrack virtualTrack = virtualTrackEntry.getValue();

            // retrieve sequence namespace associated with the virtual track
            String virtualTrackSequenceNamespace = imfCompositionPlaylist.getSequenceNamespaceForVirtualTrackID(virtualTrack.getTrackID());

            // skip all virtual tracks that don't fall under core constraints
            if (!CoreConstraints.SUPPORTED_NAMESPACES.contains(virtualTrackSequenceNamespace)) {
                continue;
            }

            String virtualTrackSequenceName = imfCompositionPlaylist.getSequenceTypeForVirtualTrackID(virtualTrack.getTrackID());

            List<? extends IMFBaseResourceType> virtualTrackResourceList = virtualTrack.getResourceList();

            if (virtualTrackSequenceName.equals(MAIN_IMAGE_SEQUENCE)) {
                Composition.EditRate compositionEditRate = imfCompositionPlaylist.getEditRate();
                for (IMFBaseResourceType baseResourceType : virtualTrackResourceList) {
                    Composition.EditRate trackResourceEditRate = baseResourceType.getEditRate();
                    //Section 6.4 st2067-2:2016
                    if (trackResourceEditRate != null
                            && !trackResourceEditRate.equals(compositionEditRate)) {
                        imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("This Composition is invalid since the CompositionEditRate %s is not the same as at least one of the MainImageSequence's Resource EditRate %s. Please refer to st2067-2:2013 Section 6.4", compositionEditRate.toString(), trackResourceEditRate.toString()));
                    }
                }
            }

            if (imfCompositionPlaylist.getEssenceDescriptorList() == null || imfCompositionPlaylist.getEssenceDescriptorList().isEmpty()) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        "Unable to check virtual track conformance as the EssenceDescriptorList in the CPL appears to be empty");
                continue;
            }

            List<DOMNodeObjectModel> virtualTrackEssenceDescriptors = new ArrayList<>();
            String refSourceEncodingElement = "";
            String essenceDescriptorField = "";
            Composition.EditRate essenceEditRate = null;

            // iterate over virtual track resources
            for (IMFBaseResourceType imfBaseResourceType : virtualTrackResourceList) {
                IMFTrackFileResourceType imfTrackFileResourceType = IMFTrackFileResourceType.class.cast(imfBaseResourceType);
                DOMNodeObjectModel domNodeObjectModel = essenceDescriptorListMap.get(UUIDHelper.fromUUIDAsURNStringToUUID(imfTrackFileResourceType.getSourceEncoding()));
                //Section 6.8 st2067-2:2016
                if (domNodeObjectModel == null) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, String.format("EssenceDescriptor ID %s referenced by a " +
                                    "VirtualTrack Resource does not have a corresponding EssenceDescriptor in the EssenceDescriptorList in the CPL",
                            imfTrackFileResourceType.getSourceEncoding()));
                    continue;
                }

                if (!refSourceEncodingElement.equals(imfTrackFileResourceType.getSourceEncoding())) {
                    refSourceEncodingElement = imfTrackFileResourceType.getSourceEncoding();
                    //Section 6.3.1 and 6.3.2 st2067-2:2016 Edit Rate check
                    essenceDescriptorField = "SampleRate";

                    String sampleRate = domNodeObjectModel.getFieldAsString(essenceDescriptorField);
                    if (sampleRate != null) {
                        Long numerator = 0L;
                        Long denominator = 0L;
                        String[] sampleRateElements = (sampleRate.contains(" ")) ? sampleRate.split(" ") : sampleRate.contains("/") ? sampleRate.split("/") : new String[2];
                        if (sampleRateElements.length == 2) {
                            numerator = Long.valueOf(sampleRateElements[0]);
                            denominator = Long.valueOf(sampleRateElements[1]);
                        } else if (sampleRateElements.length == 1) {
                            numerator = Long.valueOf(sampleRateElements[0]);
                            denominator = 1L;
                        }
                        List<Long> editRate = new ArrayList<>();
                        editRate.add(numerator);
                        editRate.add(denominator);

                        essenceEditRate = new Composition.EditRate(editRate);

                        if (virtualTrackSequenceName.equals(MAIN_IMAGE_SEQUENCE)) {
                            CompositionImageEssenceDescriptorModel imageEssenceDescriptorModel = new CompositionImageEssenceDescriptorModel(UUIDHelper.fromUUIDAsURNStringToUUID
                                    (imfTrackFileResourceType.getSourceEncoding()),
                                    domNodeObjectModel,
                                    regXMLLibDictionary);

                            // ignoring the interlaced case since the edit rate depends on the specific interlaced packaging, which is application dependent
                            if (!imageEssenceDescriptorModel.getFrameLayoutType().equals(GenericPictureEssenceDescriptor.FrameLayoutType.SeparateFields) && !essenceEditRate.equals(imfBaseResourceType.getEditRate())) {
                                //Section 6.3.1 and 6.3.2 st2067-2:2016
                                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                                        String.format("This Composition represented by the ID %s is invalid since the VirtualTrack represented by ID %s has a Resource represented by ID %s that refers to a EssenceDescriptor in the CPL's EssenceDescriptorList represented by the ID %s " +
                                                        "whose indicated %s value is %s, however the Resource Edit Rate is %s"
                                                , imfCompositionPlaylist.getUUID().toString(), virtualTrack.getTrackID().toString(), imfBaseResourceType.getId(), imfTrackFileResourceType.getSourceEncoding(), essenceDescriptorField, essenceEditRate.toString(), imfBaseResourceType.getEditRate().toString()));
                            }
                        }
                    }
                }
                if (essenceEditRate == null) {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                            String.format("This Composition represented by the ID %s is invalid since the VirtualTrack represented by ID %s has a Resource represented by ID %s that seems to refer to a EssenceDescriptor in the CPL's EssenceDescriptorList represented by the ID %s " +
                                            "which does not have a value set for the field %s, however the Resource Edit Rate is %s"
                                    , imfCompositionPlaylist.getUUID().toString(), virtualTrack.getTrackID().toString(), imfBaseResourceType.getId(), imfTrackFileResourceType.getSourceEncoding(), essenceDescriptorField, imfBaseResourceType.getEditRate().toString()));
                }
                virtualTrackEssenceDescriptors.add(domNodeObjectModel);
            }

            Set<String> trackHomogeneitySelectionSet = homogeneitySelectionSet;
            if (!virtualTrackEssenceDescriptors.isEmpty()) {
                if (isCDCIEssenceDescriptor(virtualTrackEssenceDescriptors.get(0))) {
                    trackHomogeneitySelectionSet.add("CodingEquations");
                }
            }

            imfErrorLogger.addAllErrors(checkVirtualTrackHomogeneity(virtualTrack, essenceDescriptorListMap, trackHomogeneitySelectionSet));
        }

        //TODO : Add a check to ensure that all the VirtualTracks have the same duration.

        return imfErrorLogger.getErrors();
    }


    private static boolean isCDCIEssenceDescriptor(DOMNodeObjectModel domNodeObjectModel) {
        return domNodeObjectModel.getLocalName().equals("CDCIDescriptor");
    }

}
