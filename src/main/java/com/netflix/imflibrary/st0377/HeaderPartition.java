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

package com.netflix.imflibrary.st0377;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.MXFUID;
import com.netflix.imflibrary.st0377.header.JPEG2000PictureSubDescriptor;
import com.netflix.imflibrary.st2067_2.Composition;
import com.netflix.imflibrary.utils.ByteProvider;
import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.MXFPropertyPopulator;
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.st0377.header.AudioChannelLabelSubDescriptor;
import com.netflix.imflibrary.st0377.header.CDCIPictureEssenceDescriptor;
import com.netflix.imflibrary.st0377.header.ContentStorage;
import com.netflix.imflibrary.st0377.header.EssenceContainerData;
import com.netflix.imflibrary.st0377.header.GenericDescriptor;
import com.netflix.imflibrary.st0377.header.GenericPackage;
import com.netflix.imflibrary.st0377.header.GenericTrack;
import com.netflix.imflibrary.st0377.header.InterchangeObject;
import com.netflix.imflibrary.st0377.header.MaterialPackage;
import com.netflix.imflibrary.st0377.header.PHDRMetaDataTrackSubDescriptor;
import com.netflix.imflibrary.st0377.header.Preface;
import com.netflix.imflibrary.st0377.header.RGBAPictureEssenceDescriptor;
import com.netflix.imflibrary.st0377.header.Sequence;
import com.netflix.imflibrary.st0377.header.SoundFieldGroupLabelSubDescriptor;
import com.netflix.imflibrary.st0377.header.SourceClip;
import com.netflix.imflibrary.st0377.header.SourcePackage;
import com.netflix.imflibrary.st0377.header.StructuralComponent;
import com.netflix.imflibrary.st0377.header.StructuralMetadata;
import com.netflix.imflibrary.st0377.header.TimelineTrack;
import com.netflix.imflibrary.st0377.header.WaveAudioEssenceDescriptor;
import com.netflix.imflibrary.utils.ErrorLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class corresponds to an object model for the Header Partition construct defined in st377-1:2011
 */
@Immutable
@SuppressWarnings({"PMD.SingularField"})
public final class HeaderPartition
{
    private static final String ERROR_DESCRIPTION_PREFIX = "MXF Header Partition: ";
    private static final Long IMF_MXF_HEADER_PARTITION_OFFSET = 0L; //The IMF Essence component spec (SMPTE ST-2067-5:2013) constrains the byte offset of the Header Partition to the start of the file

    private final PartitionPack partitionPack;
    private final PrimerPack primerPack;
    private final Map<String, List<InterchangeObject>> interchangeObjectsMap = new LinkedHashMap<>();
    private final Map<String, List<InterchangeObject.InterchangeObjectBO>> interchangeObjectBOsMap = new LinkedHashMap<>();
    private final Map<MXFUID, InterchangeObject> uidToMetadataSets = new LinkedHashMap<>();
    private final Map<MXFUID, InterchangeObject.InterchangeObjectBO> uidToBOs = new LinkedHashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(HeaderPartition.class);

    /**
     * Instantiates a new MXF Header partition.
     *
     * @param byteProvider the input sequence of bytes
     * @param byteOffset the byte offset corresponding to the HeaderPartition
     * @param maxPartitionSize the size of the header partition
     * @param imfErrorLogger an IMFErrorLogger dedicated to this header partition
     * @throws IOException - any I/O related error will be exposed through an IOException
     */
    public HeaderPartition(ByteProvider byteProvider, long byteOffset, long maxPartitionSize, IMFErrorLogger imfErrorLogger) throws IOException
    {
        long numBytesRead = 0;
        int numErrors = imfErrorLogger.getNumberOfErrors(); //Number of errors prior to parsing and reading the HeaderPartition

        //read partition pack
        if(byteOffset != IMF_MXF_HEADER_PARTITION_OFFSET){
            throw new MXFException(String.format("Expected the header partition to be at offset %d, whereas it is located at offset %d in the MXF file", IMF_MXF_HEADER_PARTITION_OFFSET, byteOffset));
        }

        this.partitionPack = new PartitionPack(byteProvider, IMF_MXF_HEADER_PARTITION_OFFSET, false, imfErrorLogger);
        if(!this.partitionPack.isValidHeaderPartition())
        {
            throw new MXFException("Found an invalid header partition");
        }
        numBytesRead += this.partitionPack.getKLVPacketSize();
        Long byteOffsetOfNextKLVPacket = byteOffset + numBytesRead;

        //read primer pack or a single KLV fill item followed by primer pack
        {
            KLVPacket.Header header = new KLVPacket.Header(byteProvider, byteOffsetOfNextKLVPacket);
            byte[] key = Arrays.copyOf(header.getKey(), header.getKey().length);
            numBytesRead += header.getKLSize();

            if (PrimerPack.isValidKey(key))
            {
                this.primerPack = new PrimerPack(byteProvider, header);
                numBytesRead += header.getVSize();
            }
            else
            {
                byteProvider.skipBytes(header.getVSize());
                numBytesRead += header.getVSize();

                header = new KLVPacket.Header(byteProvider, byteOffsetOfNextKLVPacket);
                key = Arrays.copyOf(header.getKey(), header.getKey().length);
                numBytesRead += header.getKLSize();
                if (PrimerPack.isValidKey(key))
                {
                    this.primerPack = new PrimerPack(byteProvider, header);
                    numBytesRead += header.getVSize();
                }
                else
                {
                    throw new MXFException("Could not find primer pack");
                }
            }
        }
        byteOffsetOfNextKLVPacket = byteOffset + numBytesRead;

        //read structural metadata + KLV fill items
        while (numBytesRead < maxPartitionSize)
        {
            KLVPacket.Header header = new KLVPacket.Header(byteProvider, byteOffsetOfNextKLVPacket);
            logger.info(String.format("Found KLV item with key = %s, length field size = %d, length value = %d", new MXFUID(header.getKey()), header.getLSize(), header.getVSize()));
            byte[] key = Arrays.copyOf(header.getKey(), header.getKey().length);
            numBytesRead += header.getKLSize();

            if (StructuralMetadata.isStructuralMetadata(Arrays.copyOf(header.getKey(), header.getKey().length)))
            {
                Class clazz = StructuralMetadata.getStructuralMetadataSetClass(key);
                if(!clazz.getSimpleName().equals(Object.class.getSimpleName())){
                    logger.info(String.format("KLV item with key = %s corresponds to class %s", new MXFUID(header.getKey()), clazz.getSimpleName()));
                    InterchangeObject.InterchangeObjectBO interchangeObjectBO = this.constructInterchangeObjectBO(clazz, header, byteProvider, this.primerPack.getLocalTagEntryBatch().getLocalTagToUIDMap(), imfErrorLogger);
                    List<InterchangeObject.InterchangeObjectBO> list = this.interchangeObjectBOsMap.get(interchangeObjectBO.getClass().getSimpleName());
                    if(list == null){
                        list = new ArrayList<>();
                        this.interchangeObjectBOsMap.put(interchangeObjectBO.getClass().getSimpleName(), list);
                    }
                    list.add(interchangeObjectBO);
                    uidToBOs.put(interchangeObjectBO.getInstanceUID(), interchangeObjectBO);
                    if(interchangeObjectBO instanceof MaterialPackage.MaterialPackageBO
                            || interchangeObjectBO instanceof SourcePackage.SourcePackageBO){
                        GenericPackage.GenericPackageBO genericPackageBO = (GenericPackage.GenericPackageBO)interchangeObjectBO;
                        uidToBOs.put(genericPackageBO.getPackageUID(), genericPackageBO);
                    }
                }
                else
                {
                    byteProvider.skipBytes(header.getVSize());
                }

            }
            else
            {
                byteProvider.skipBytes(header.getVSize());
            }
            numBytesRead += header.getVSize();
            byteOffsetOfNextKLVPacket = byteOffset + numBytesRead;
        }

        //header partition validation
        int prefaceSetCount = (this.interchangeObjectBOsMap.containsKey(Preface.PrefaceBO.class.getSimpleName()) && this.interchangeObjectBOsMap.get(Preface.PrefaceBO.class.getSimpleName()) != null)
                ? this.interchangeObjectBOsMap.get(Preface.PrefaceBO.class.getSimpleName()).size() : 0;

        if (prefaceSetCount != 1)
        {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.MXF_PARTITION_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL,
                    HeaderPartition.ERROR_DESCRIPTION_PREFIX + String.format("Found %d Preface sets, only one is allowed in header partition",
                            prefaceSetCount));
        }

        if (imfErrorLogger.getNumberOfErrors() > numErrors)//Flag an exception if any errors were accumulated while parsing and reading the HeaderPartition
        {
            List<ErrorLogger.ErrorObject> errorObjectList = imfErrorLogger.getErrors();
            for(int i=numErrors; i< errorObjectList.size(); i++) {
                logger.error(errorObjectList.get(i).getErrorDescription());
            }
            throw new MXFException(String.format("%d errors encountered when reading header partition", imfErrorLogger.getNumberOfErrors() - numErrors));
        }

        Set<InterchangeObject.InterchangeObjectBO> parsedInterchangeObjectBOs = new LinkedHashSet<>();
        for (Map.Entry<MXFUID, InterchangeObject.InterchangeObjectBO> entry : uidToBOs.entrySet())
        {
            parsedInterchangeObjectBOs.add(entry.getValue());
        }
        Map<MXFUID, Node> instanceIDToNodes = new LinkedHashMap<>();

        for(InterchangeObject.InterchangeObjectBO interchangeObjectBO : parsedInterchangeObjectBOs)
        {
            instanceIDToNodes.put(interchangeObjectBO.getInstanceUID(), new Node(interchangeObjectBO.getInstanceUID()));
        }

        for (Map.Entry<MXFUID, Node> entry : instanceIDToNodes.entrySet())
        {
            Node node = entry.getValue();
            InterchangeObject.InterchangeObjectBO interchangeObjectBO = uidToBOs.get(node.uid);
            List<MXFUID> dependentUIDs = MXFPropertyPopulator.getDependentUIDs(interchangeObjectBO);
            for(MXFUID MXFUID : dependentUIDs)
            {
                InterchangeObject.InterchangeObjectBO dependentInterchangeObjectBO = uidToBOs.get(MXFUID);
                if (dependentInterchangeObjectBO != null)
                {
                    Node providerNode = instanceIDToNodes.get(dependentInterchangeObjectBO.getInstanceUID());
//                    providerNode.provides.add(node);
                    node.depends.add(providerNode);
                }
            }
        }

        List<Node> nodeList = new ArrayList<>(instanceIDToNodes.values());
        List<Node> resolvedList = resolve(nodeList);

        for(Node node : resolvedList) {
            InterchangeObject.InterchangeObjectBO interchangeObjectBO = uidToBOs.get(node.uid);
            if (node.depends.size() == 0
                    && !interchangeObjectBO.getClass().equals(SourceClip.SourceClipBO.class)
                    && !interchangeObjectBO.getClass().equals(Sequence.SequenceBO.class)){
                InterchangeObject interchangeObject = this.constructInterchangeObject(interchangeObjectBO.getClass().getEnclosingClass(), interchangeObjectBO, node);
                this.cacheInterchangeObject(interchangeObject);
                this.uidToMetadataSets.put(interchangeObjectBO.getInstanceUID(), interchangeObject);
                if (interchangeObjectBO instanceof GenericPackage.GenericPackageBO) {
                    this.uidToMetadataSets.put(((GenericPackage.GenericPackageBO) interchangeObjectBO).getPackageUID(), interchangeObject);
                }
            }
            else {
                if (interchangeObjectBO.getClass().getEnclosingClass().equals(SourceClip.class)) {
                    GenericPackage genericPackage = null;
                    for (Node dependent : node.depends) {
                        InterchangeObject dependentInterchangeObject = uidToMetadataSets.get(dependent.uid);
                        if (dependentInterchangeObject instanceof GenericPackage) {
                            genericPackage = (GenericPackage) dependentInterchangeObject;
                        }
                    }
                    SourceClip sourceClip = new SourceClip((SourceClip.SourceClipBO) interchangeObjectBO, genericPackage);
                    this.cacheInterchangeObject(sourceClip);
                    uidToMetadataSets.put(interchangeObjectBO.getInstanceUID(), sourceClip);
                } else if (interchangeObjectBO.getClass().getEnclosingClass().equals(Sequence.class)) {
                    List<StructuralComponent> structuralComponents = new ArrayList<>();
                    for (Node dependent : node.depends) {
                        InterchangeObject dependentInterchangeObject = uidToMetadataSets.get(dependent.uid);
                        if (dependentInterchangeObject instanceof StructuralComponent) {
                            structuralComponents.add((StructuralComponent) dependentInterchangeObject);
                        }
                    }
                    Sequence sequence = new Sequence((Sequence.SequenceBO) interchangeObjectBO, structuralComponents);
                    this.cacheInterchangeObject(sequence);
                    uidToMetadataSets.put(interchangeObjectBO.getInstanceUID(), sequence);
                } else if (interchangeObjectBO.getClass().getEnclosingClass().equals(TimelineTrack.class)) {
                    Sequence sequence = null;
                    for (Node dependent : node.depends) {
                        InterchangeObject dependentInterchangeObject = uidToMetadataSets.get(dependent.uid);
                        if (dependentInterchangeObject instanceof Sequence) {
                            sequence = (Sequence) dependentInterchangeObject;
                        }
                        TimelineTrack timelineTrack = new TimelineTrack((TimelineTrack.TimelineTrackBO) interchangeObjectBO, sequence);
                        this.cacheInterchangeObject(timelineTrack);
                        uidToMetadataSets.put(interchangeObjectBO.getInstanceUID(), timelineTrack);
                    }

                } else if (interchangeObjectBO.getClass().getEnclosingClass().equals(SourcePackage.class)) {
                    List<GenericTrack> genericTracks = new ArrayList<>();
                    GenericDescriptor genericDescriptor = null;
                    for (Node dependent : node.depends) {
                        InterchangeObject dependentInterchangeObject = uidToMetadataSets.get(dependent.uid);
                        if (dependentInterchangeObject instanceof GenericTrack) {
                            genericTracks.add((GenericTrack) dependentInterchangeObject);
                        } else if (dependentInterchangeObject instanceof GenericDescriptor) {
                            genericDescriptor = (GenericDescriptor) dependentInterchangeObject;
                        }
                    }
                    SourcePackage sourcePackage = new SourcePackage((SourcePackage.SourcePackageBO) interchangeObjectBO, genericTracks, genericDescriptor);
                    this.cacheInterchangeObject(sourcePackage);
                    uidToMetadataSets.put(interchangeObjectBO.getInstanceUID(), sourcePackage);
                    uidToMetadataSets.put(((SourcePackage.SourcePackageBO) interchangeObjectBO).getPackageUID(), sourcePackage);
                } else if (interchangeObjectBO.getClass().getEnclosingClass().equals(MaterialPackage.class)) {
                    List<GenericTrack> genericTracks = new ArrayList<>();
                    for (Node dependent : node.depends) {
                        InterchangeObject dependentInterchangeObject = uidToMetadataSets.get(dependent.uid);
                        if (dependentInterchangeObject instanceof GenericTrack) {
                            genericTracks.add((GenericTrack) dependentInterchangeObject);
                        }
                    }
                    MaterialPackage materialPackage = new MaterialPackage((MaterialPackage.MaterialPackageBO) interchangeObjectBO, genericTracks);
                    this.cacheInterchangeObject(materialPackage);
                    uidToMetadataSets.put(interchangeObjectBO.getInstanceUID(), materialPackage);
                    uidToMetadataSets.put(((MaterialPackage.MaterialPackageBO) interchangeObjectBO).getPackageUID(), materialPackage);

                } else if (interchangeObjectBO.getClass().getEnclosingClass().equals(EssenceContainerData.class)) {
                    GenericPackage genericPackage = null;
                    for (Node dependent : node.depends) {
                        InterchangeObject dependentInterchangeObject = uidToMetadataSets.get(dependent.uid);
                        if (dependentInterchangeObject instanceof GenericPackage) {
                            genericPackage = (GenericPackage) dependentInterchangeObject;
                        }
                    }
                    EssenceContainerData essenceContainerData = new EssenceContainerData(
                            (EssenceContainerData.EssenceContainerDataBO) interchangeObjectBO, genericPackage);
                    this.cacheInterchangeObject(essenceContainerData);
                    uidToMetadataSets.put(interchangeObjectBO.getInstanceUID(), essenceContainerData);
                } else if (interchangeObjectBO.getClass().getEnclosingClass().equals(ContentStorage.class)) {
                    List<GenericPackage> genericPackageList = new ArrayList<>();
                    List<EssenceContainerData> essenceContainerDataList = new ArrayList<>();
                    for (Node dependent : node.depends) {
                        InterchangeObject dependentInterchangeObject = uidToMetadataSets.get(dependent.uid);
                        if (dependentInterchangeObject instanceof GenericPackage) {
                            genericPackageList.add((GenericPackage) dependentInterchangeObject);
                        } else if (dependentInterchangeObject instanceof EssenceContainerData) {
                            essenceContainerDataList.add((EssenceContainerData) dependentInterchangeObject);
                        }
                    }
                    ContentStorage contentStorage = new ContentStorage(
                            (ContentStorage.ContentStorageBO) interchangeObjectBO, genericPackageList, essenceContainerDataList);
                    this.cacheInterchangeObject(contentStorage);
                    uidToMetadataSets.put(interchangeObjectBO.getInstanceUID(), contentStorage);

                } else if (interchangeObjectBO.getClass().getEnclosingClass().equals(Preface.class)) {
                    GenericPackage genericPackage = null;
                    ContentStorage contentStorage = null;
                    for (Node dependent : node.depends) {
                        InterchangeObject dependentInterchangeObject = uidToMetadataSets.get(dependent.uid);
                        if (dependentInterchangeObject instanceof GenericPackage) {
                            genericPackage = (GenericPackage) dependentInterchangeObject;
                        } else if (dependentInterchangeObject instanceof ContentStorage) {
                            contentStorage = (ContentStorage) dependentInterchangeObject;
                        }
                    }
                    Preface preface = new Preface((Preface.PrefaceBO) interchangeObjectBO, genericPackage, contentStorage);
                    this.cacheInterchangeObject(preface);
                    uidToMetadataSets.put(interchangeObjectBO.getInstanceUID(), preface);
                } else if(interchangeObjectBO.getClass().getEnclosingClass().equals(CDCIPictureEssenceDescriptor.class)){
                    for(Node dependent : node.depends) {
                        InterchangeObject dependentInterchangeObject = uidToMetadataSets.get(dependent.uid);
                        /*Although we do retrieve the dependent SubDescriptor for this CDCIPictureEssenceDescriptor we do not really have a need for it,
                        * since it can always be retrieved using the strong reference present in the subDescriptors collection of the CDCIPictureEssenceDescriptor
                        * on the other hand passing a reference to the SubDescriptor to the constructor can be problematic since SubDescriptors are optional*/
                        JPEG2000PictureSubDescriptor jpeg2000PictureSubDescriptor = null;
                        if(dependentInterchangeObject instanceof JPEG2000PictureSubDescriptor){
                            jpeg2000PictureSubDescriptor = (JPEG2000PictureSubDescriptor) dependentInterchangeObject;
                        }
                        /*Add similar casting code for other sub descriptors when relevant*/
                    }
                    CDCIPictureEssenceDescriptor cdciPictureEssenceDescriptor = new CDCIPictureEssenceDescriptor((CDCIPictureEssenceDescriptor.CDCIPictureEssenceDescriptorBO) interchangeObjectBO);
                    this.cacheInterchangeObject(cdciPictureEssenceDescriptor);
                    uidToMetadataSets.put(interchangeObjectBO.getInstanceUID(), cdciPictureEssenceDescriptor);
                } else if(interchangeObjectBO.getClass().getEnclosingClass().equals(WaveAudioEssenceDescriptor.class)){
                    List<InterchangeObject> subDescriptors = new ArrayList<InterchangeObject>();
                    for(Node dependent : node.depends) {
                        InterchangeObject dependentInterchangeObject = uidToMetadataSets.get(dependent.uid);
                        /*
                         * Although we do retrieve the dependent SubDescriptors for this WaveAudioEssenceDescriptor we do not really have a need for it,
                         * since it can always be retrieved using the strong reference present in the subDescriptors collection of the WaveAudioEssenceDescriptor.
                         * On the other hand passing a reference to a SubDescriptor to the WaveAudioEssenceDescriptor's constructor can be problematic since
                         * SubDescriptors are optional
                         */
                        AudioChannelLabelSubDescriptor audioChannelLabelSubDescriptor = null;
                        SoundFieldGroupLabelSubDescriptor soundFieldGroupLabelSubDescriptor = null;
                        if(dependentInterchangeObject instanceof AudioChannelLabelSubDescriptor){
                            audioChannelLabelSubDescriptor = (AudioChannelLabelSubDescriptor) dependentInterchangeObject;
                            subDescriptors.add(audioChannelLabelSubDescriptor);
                        }
                        else if (dependentInterchangeObject instanceof SoundFieldGroupLabelSubDescriptor){
                            soundFieldGroupLabelSubDescriptor = (SoundFieldGroupLabelSubDescriptor) dependentInterchangeObject;
                            subDescriptors.add(soundFieldGroupLabelSubDescriptor);
                        }
                    }
                    if(node.depends.size() > 0
                        && subDescriptors.size() == 0){
                        throw new MXFException(String.format("The WaveAudioEssenceDescriptor in the essence has dependencies, but neither of them is a AudioChannelLabelSubDescriptor nor SoundFieldGroupLabelSubDescriptor"));
                    }
                    WaveAudioEssenceDescriptor waveAudioEssenceDescriptor = new WaveAudioEssenceDescriptor((WaveAudioEssenceDescriptor.WaveAudioEssenceDescriptorBO) interchangeObjectBO);
                    this.cacheInterchangeObject(waveAudioEssenceDescriptor);
                    uidToMetadataSets.put(interchangeObjectBO.getInstanceUID(), waveAudioEssenceDescriptor);
                }
            }
        }

    }

    /**
     * A factory method to reflectively construct InterchangeObjectBO types by classname and argument list
     * @return the constructed InterchangeBO
     */
    private InterchangeObject.InterchangeObjectBO constructInterchangeObjectBO(Class clazz, KLVPacket.Header header, ByteProvider byteProvider, Map localTagToUIDMap, IMFErrorLogger imfErrorLogger) throws IOException{
        try {

            Constructor<?> constructor = clazz.getConstructor(KLVPacket.Header.class, ByteProvider.class, Map.class, IMFErrorLogger.class);
            InterchangeObject.InterchangeObjectBO interchangeObjectBO = (InterchangeObject.InterchangeObjectBO)constructor.newInstance(header, byteProvider, localTagToUIDMap, imfErrorLogger);
            String simpleClassName = interchangeObjectBO.getClass().getSimpleName();
            logger.debug(String.format("Parsed and read %s metadata in the header partition.", simpleClassName.substring(0, simpleClassName.length() - 2)));
            return interchangeObjectBO;
        }
        catch(NoSuchMethodException|IllegalAccessException|InstantiationException|InvocationTargetException e){
            throw new IOException(String.format("No matching constructor for class %s", clazz.getSimpleName()));
        }
    }

    /**
     * A factory method to reflectively construct InterchangeObject types by classname
     * @return the constructed InterchangeObject
     */
    private InterchangeObject constructInterchangeObject(Class clazz, InterchangeObject.InterchangeObjectBO interchangeObjectBO, Node node) throws IOException{
        try {
            Constructor<?> constructor = clazz.getConstructor(interchangeObjectBO.getClass());
            InterchangeObject interchangeObject = (InterchangeObject)constructor.newInstance(interchangeObjectBO);
            logger.debug(String.format("Constructing the object model for %s metadata in the header partition.", interchangeObject.getClass().getSimpleName()));
            return interchangeObject;
        }
        catch(NoSuchMethodException|IllegalAccessException|InstantiationException|InvocationTargetException e){
            throw new IOException(String.format("No matching constructor for class %s", clazz.getSimpleName()));
        }
    }

    /**
     * A helper method to cache an InterchangeObject
     */
    private void cacheInterchangeObject(InterchangeObject interchangeObject){
        List<InterchangeObject> list = this.interchangeObjectsMap.get(interchangeObject.getClass().getSimpleName());
        if(list == null){
            list = new ArrayList<InterchangeObject>();
            this.interchangeObjectsMap.put(interchangeObject.getClass().getSimpleName(), list);
        }
        list.add(interchangeObject);
    }

    /**
     * Gets the Preface object corresponding to this HeaderPartition object
     * @return the Preface object in this HeaderPartition
     */
    @Nullable
    public Preface getPreface()
    {
        List<InterchangeObject> list = this.interchangeObjectsMap.get(Preface.class.getSimpleName());
        Preface preface = null;
        if(list != null) {
            preface = (Preface) list.get(0);
        }
        return preface;
    }

    /**
     * Gets the PrimerPack object corresponding to this HeaderPartition object
     * @return the PrimerPack object in this HeaderPartition
     */
    @Nullable
    public PrimerPack getPrimerPack()
    {
        return this.primerPack;
    }

    /**
     * Gets all of the ContentStorage objects corresponding to this HeaderPartition object
     * @return list of ContentStorage objects in this HeaderPartition
     */
    public List<InterchangeObject> getContentStorageList()
    {
        return this.getInterchangeObjects(ContentStorage.class);
    }

    /**
     * Gets all of the MaterialPackage objects corresponding to this HeaderPartition object
     * @return list of MaterialPackage objects in this HeaderPartition
     */
    public List<InterchangeObject> getMaterialPackages()
    {
        return this.getInterchangeObjects(MaterialPackage.class);
    }

    /**
     * Gets all of the EssenceContainerData objects corresponding to this HeaderPartition object
     * @return list of EssenceContainerData objects in this HeaderPartition
     */
    public List<InterchangeObject> getEssenceContainerDataList()
    {
        return this.getInterchangeObjects(EssenceContainerData.class);
    }

    /**
     * Gets all of the SourcePackage objects corresponding to this HeaderPartition object
     * @return list of SourcePackages in this HeaderPartition
     */
    public List<InterchangeObject> getSourcePackages()
    {
        return this.getInterchangeObjects(SourcePackage.class);
    }

    /**
     * Gets all of the EssenceDescriptor objects corresponding to this HeaderPartition object that are referenced by
     * the Source Packages in this header partition
     * @return list of EssenceDescriptor objects referenced by the Source Packages in this HeaderPartition
     */
    public List<InterchangeObject.InterchangeObjectBO> getEssenceDescriptors(){
        List<InterchangeObject.InterchangeObjectBO> sourcePackageBOs = this.interchangeObjectBOsMap.get(SourcePackage.SourcePackageBO.class.getSimpleName());
        List<InterchangeObject.InterchangeObjectBO> essenceDescriptors = new ArrayList<>();
        for(int i=0; i<sourcePackageBOs.size(); i++){
            SourcePackage.SourcePackageBO sourcePackageBO = (SourcePackage.SourcePackageBO) sourcePackageBOs.get(i);
            essenceDescriptors.add(uidToBOs.get(sourcePackageBO.getDescriptorUID()));
        }
        return essenceDescriptors;
    }

    /**
     * Gets all of the SubDescriptor objects corresponding to this HeaderPartition object that are referenced by all
     * the source packages in this header partition
     * @return list of SubDescriptor objects referenced by the Source Packages in this HeaderPartition
     */
    public List<InterchangeObject.InterchangeObjectBO> getSubDescriptors(){
        List<InterchangeObject.InterchangeObjectBO> sourcePackageBOs = this.interchangeObjectBOsMap.get(SourcePackage.SourcePackageBO.class.getSimpleName());
        List<InterchangeObject.InterchangeObjectBO>subDescriptors = new ArrayList<>();
        for(int i=0; i<sourcePackageBOs.size(); i++){
            SourcePackage.SourcePackageBO sourcePackageBO = (SourcePackage.SourcePackageBO) sourcePackageBOs.get(i);
            GenericDescriptor.GenericDescriptorBO genericDescriptorBO = (GenericDescriptor.GenericDescriptorBO)uidToBOs.get(sourcePackageBO.getDescriptorUID());
            CompoundDataTypes.MXFCollections.MXFCollection<InterchangeObject.InterchangeObjectBO.StrongRef> strongRefsCollection = genericDescriptorBO.getSubdescriptors();
            List<InterchangeObject.InterchangeObjectBO.StrongRef>strongRefs = strongRefsCollection.getEntries();
            for(InterchangeObject.InterchangeObjectBO.StrongRef strongRef : strongRefs) {
                subDescriptors.add(uidToBOs.get(strongRef.getInstanceUID()));
            }
        }
        return subDescriptors;
    }

    /**
     * Gets the SubDescriptor objects corresponding to this HeaderPartition object that are referred by the specified
     * EssenceDescriptor
     * @param essenceDescriptor the essence descriptor whose referred subdescriptors are requested
     * @return list of SubDescriptor objects referenced by the Essence Descriptors in this HeaderPartition
     */
    public List<InterchangeObject.InterchangeObjectBO> getSubDescriptors(InterchangeObject.InterchangeObjectBO essenceDescriptor){
        GenericDescriptor.GenericDescriptorBO genericDescriptorBO = (GenericDescriptor.GenericDescriptorBO)essenceDescriptor;
        return this.getSubDescriptors(genericDescriptorBO.getSubdescriptors());
    }


    /**
     * Gets the SubDescriptors in this HeaderPartition object that correspond to the specified StrongRefCollection
     * @param strongRefCollection collection strong references corresponding to the SubDescriptors
     * @return list of SubDescriptors corresponding to the collection of strong references passed in
     */
    List<InterchangeObject.InterchangeObjectBO> getSubDescriptors(CompoundDataTypes.MXFCollections.MXFCollection<InterchangeObject.InterchangeObjectBO.StrongRef> strongRefCollection){
        List<InterchangeObject.InterchangeObjectBO>subDescriptors = new ArrayList<>();
        if(strongRefCollection != null) { /*There might be essences that have no SubDescriptors*/
            List<InterchangeObject.InterchangeObjectBO.StrongRef> strongRefList = strongRefCollection.getEntries();
            for (InterchangeObject.InterchangeObjectBO.StrongRef strongRef : strongRefList) {
                subDescriptors.add(uidToBOs.get(strongRef.getInstanceUID()));
            }
        }
        return subDescriptors;
    }

    /**
     * Returns the largest duration of all the TimelineTracks within the first (in parsing order) Material Package
     * associated with this HeaderPartition object
     * @return the largest duration of all the Timeline tracks within the first Material Package associated with this Header partition
     */
    public BigInteger getEssenceDuration(){
        MaterialPackage materialPackage = (MaterialPackage)this.getMaterialPackages().get(0);
        Long duration = 0L;
        Long maxDuration = duration;
        for (TimelineTrack timelineTrack : materialPackage.getTimelineTracks())
        {
            List<MXFUID> uids = timelineTrack.getSequence().getStructuralComponentInstanceUIDs();
            List<InterchangeObject.InterchangeObjectBO> structuralComponentBOs = new ArrayList<>();
            for(MXFUID uid : uids){
                if(this.uidToBOs.get(uid) != null){
                    structuralComponentBOs.add(this.uidToBOs.get(uid));
                }
            }

            for(InterchangeObject.InterchangeObjectBO interchangeObjectBO : structuralComponentBOs){
                StructuralComponent.StructuralComponentBO structuralComponentBO = (StructuralComponent.StructuralComponentBO) interchangeObjectBO;
                duration += structuralComponentBO.getDuration();
            }

            if(duration > maxDuration){
                maxDuration = duration;
            }
        }
        return BigInteger.valueOf(maxDuration);
    }

    /**
     * Getter for a parsed InterchangeObject by ID
     * @param structuralMetadataID identifier for the structural metadata set
     * @return the InterchangeObjectBO corresponding to the class name
     */
    public List<InterchangeObject.InterchangeObjectBO> getStructuralMetadata(StructuralMetadataID structuralMetadataID){
        String key = structuralMetadataID.getName() + "BO";
        return this.interchangeObjectBOsMap.get(key);
    }

    /**
     * Checks if this HeaderPartition object has a Wave Audio Essence Descriptor
     * @return true/false depending on whether this HeaderPartition contains a WaveAudioEssenceDescriptor or not
     */
    public boolean hasWaveAudioEssenceDescriptor()
    {
        return this.hasInterchangeObject(WaveAudioEssenceDescriptor.class);
    }

    /**
     * Checks if this HeaderPartition object has a CDCI Picture Essence Descriptor
     * @return true/false depending on whether this HeaderPartition contains a CDCIPictureEssenceDescriptor or not
     */
    public boolean hasCDCIPictureEssenceDescriptor()
    {
        return this.hasInterchangeObject(CDCIPictureEssenceDescriptor.class);
    }

    /**
     * Checks if this HeaderPartition object has a RGBA Picture Essence Descriptor
     * @return true/false depending on whether this HeaderPartition contains a RGBAPictureEssenceDescriptor or not
     */
    public boolean hasRGBAPictureEssenceDescriptor()
    {
        return this.hasInterchangeObject(RGBAPictureEssenceDescriptor.class);
    }

    /**
     * Checks if this HeaderPartition object has a PHDR Metadata track SubDescriptor
     * @return true/false depending on whether this HeaderPartition contains a PHDRMetaDataTrackSubDescriptor or not
     */
    public boolean hasPHDRMetaDataTrackSubDescriptor()
    {
        return this.hasInterchangeObject(PHDRMetaDataTrackSubDescriptor.class);
    }

    /**
     * Gets all the wave audio essence descriptors associated with this HeaderPartition object
     * @return list of all the WaveAudioEssenceDescriptors in this header partition
     */
    public List<InterchangeObject> getWaveAudioEssenceDescriptors()
    {
        return this.getInterchangeObjects(WaveAudioEssenceDescriptor.class);
    }

    /**
     * Checks if this HeaderPartition object has any audio channel label sub descriptors
     * @return true/false depending on whether this HeaderPartition contains an AudioChannelLabelSubDescriptor or not
     */
    public boolean hasAudioChannelLabelSubDescriptors()
    {
        return this.hasInterchangeObject(AudioChannelLabelSubDescriptor.class);
    }

    /**
     * Gets all the audio channel label sub descriptors associated with this HeaderPartition object
     * @return list of audio channel label sub descriptors contained in this header partition
     */
    public List<InterchangeObject> getAudioChannelLabelSubDescriptors()
    {
        if(this.hasWaveAudioEssenceDescriptor()) {
            return this.getInterchangeObjects(AudioChannelLabelSubDescriptor.class);
        }
        else{
            return new ArrayList<InterchangeObject>();
        }
    }

    /**
     * Checks if this HeaderPartition object has any sound field group label sub descriptors
     * @return true/false depending on whether this HeaderPartition contains a SoundFieldGroupLabelSubDescriptor or not
     */
    public boolean hasSoundFieldGroupLabelSubDescriptor()
    {
        return this.hasInterchangeObject(SoundFieldGroupLabelSubDescriptor.class);
    }

    /**
     * Gets all the sound field group label sub descriptors associated with this HeaderPartition object
     * @return list of sound field group label sub descriptors contained in this header partition
     */
    public List<InterchangeObject> getSoundFieldGroupLabelSubDescriptors()
    {
        return this.getInterchangeObjects(SoundFieldGroupLabelSubDescriptor.class);
    }

    /**
     * Gets the timeline track associated with this HeaderPartition object corresponding to the specified UID. Returns
     * null if none is found
     * @param MXFUID corresponding to the Timeline Track
     * @return null if this header partition does not contain a timeline track, else a timeline track object
     */
    public @Nullable TimelineTrack getTimelineTrack(MXFUID MXFUID)
    {
        Object object = this.uidToMetadataSets.get(MXFUID);

        TimelineTrack timelineTrack = null;
        if (object instanceof TimelineTrack)
        {
            timelineTrack = (TimelineTrack)object;
        }
        return timelineTrack;
    }

    /**
     * Gets the Sequence object associated with this HeaderPartition object corresponding to the specified UID. Returns
     * null if none is found
     * @param MXFUID corresponding to the Sequence
     * @return null if this header partition does not contain a sequence, else a sequence object
     */
    public @Nullable Sequence getSequence(MXFUID MXFUID)
    {
        Object object = this.uidToMetadataSets.get(MXFUID);

        Sequence sequence = null;
        if (object instanceof Sequence)
        {
            sequence = (Sequence)object;
        }
        return sequence;
    }

    /**
     * Gets the SourceClip object associated with this HeaderPartition object corresponding to the specified UID. Returns
     * null if none is found
     * @param MXFUID corresponding to the Source Clip
     * @return null if this header partition does not contain a source clip, else a source clip object
     */
    public @Nullable SourceClip getSourceClip(MXFUID MXFUID)
    {
        Object object = this.uidToMetadataSets.get(MXFUID);

        SourceClip sourceClip = null;
        if (object instanceof SourceClip)
        {
            sourceClip = (SourceClip)object;
        }
        return sourceClip;
    }

    /**
     * Gets the MaterialPackage object associated with this HeaderPartition object corresponding to the specified UID. Returns
     * null if none is found
     * @param MXFUID corresponding to the Material Package
     * @return null if this header partition does not contain a material package, else a material package object
     */
    public @Nullable MaterialPackage getMaterialPackage(MXFUID MXFUID)
    {
        Object object = this.uidToMetadataSets.get(MXFUID);

        MaterialPackage materialPackage = null;
        if (object instanceof MaterialPackage)
        {
            materialPackage = (MaterialPackage)object;
        }
        return materialPackage;
    }

    /**
     * Gets the SourcePackage object associated with this HeaderPartition object corresponding to the specified UID. Returns
     * null if none is found
     * @param MXFUID corresponding to the Source Package
     * @return null if this header partition does not contain a source package, else a source package object
     */
    public @Nullable SourcePackage getSourcePackage(MXFUID MXFUID)
    {
        Object object = this.uidToMetadataSets.get(MXFUID);

        SourcePackage sourcePackage = null;
        if (object instanceof SourcePackage)
        {
            sourcePackage = (SourcePackage)object;
        }
        return sourcePackage;
    }

    /**
     * Gets the EssenceContainerData object associated with this HeaderPartition object corresponding to the specified UID. Returns
     * null if none is found
     * @param MXFUID corresponding to the EssenceContainerData
     * @return null if this header partition does not contain an EssenceContainerData object, else a EssenceContainerData object
     */
    public @Nullable EssenceContainerData getEssenceContainerData(MXFUID MXFUID)
    {
        Object object = this.uidToMetadataSets.get(MXFUID);

        EssenceContainerData essenceContainerData = null;
        if (object instanceof EssenceContainerData)
        {
            essenceContainerData = (EssenceContainerData)object;
        }
        return essenceContainerData;
    }


    /**
     * Gets the partition pack corresponding to this HeaderPartition
     * @return the partition pack object corresponding to this HeaderPartition
     */
    public PartitionPack getPartitionPack()
    {
        return this.partitionPack;
    }

    /**
     * A method to verify the presence of an InterchangeObject
     * @boolean
     */
    private boolean hasInterchangeObject(Class clazz){
        String simpleName = clazz.getSimpleName();
        return  (this.interchangeObjectsMap.containsKey(simpleName) && (this.interchangeObjectsMap.get(simpleName) != null && this.interchangeObjectsMap.get(simpleName).size() > 0));
    }

    private List<InterchangeObject> getInterchangeObjects(Class clazz){
        String simpleName = clazz.getSimpleName();
        if(this.interchangeObjectsMap.get(simpleName) == null){
            return Collections.unmodifiableList(new ArrayList<InterchangeObject>());
        }
        else {
            return Collections.unmodifiableList(this.interchangeObjectsMap.get(simpleName));
        }
    }

    /*
    L ← Empty list that will contain the sorted nodes
while there are unmarked nodes do
    select an unmarked node n
    visit(n)
function visit(node n)
    if n has a temporary mark then stop (not a DAG)
    if n is not marked (i.e. has not been visited yet) then
        mark n temporarily
        for each node m with an edge from n to m do
            visit(m)
        mark n permanently
        add n to head of L
     */
    private static List<Node> resolve(List<Node> adjacencyList)
    {
        List<Node> sortedList = new LinkedList<>();

        Node node = getUnmarkedNode(adjacencyList);
        while(node != null)
        {
            visit(node, sortedList);
            node = getUnmarkedNode(adjacencyList);
        }

         return sortedList;
    }

    private static void visit(Node node, List<Node> sortedList)
    {
        if (node.mark.equals(Mark.TEMPORARY))
        {
            throw new MXFException("Cycle detected");
        }
        else if (node.mark.equals(Mark.NONE))
        {
            node.mark = Mark.TEMPORARY;
            for (Node neighbor : node.depends)
            {
                visit(neighbor, sortedList);
            }
            node.mark = Mark.PERMANENT;
            sortedList.add(node);
        }
    }

    private static Node getUnmarkedNode(List<Node> adjacencyList)
    {
        Node unmarkedNode = null;
        for (Node node : adjacencyList)
        {
            if (node.mark.equals(Mark.NONE))
            {
                unmarkedNode = node;
                break;
            }
        }
        return unmarkedNode;
    }

    private static class Node
    {
        private final MXFUID uid;
        private final List<Node> depends;
        private Mark mark;

        private Node(MXFUID uid)
        {
            this.uid = uid;
            this.mark = Mark.NONE;
            this.depends = new LinkedList<>();
        }
    }

    private static enum Mark
    {
        /**
         * The NONE.
         */
        NONE,

        /**
         * The TEMPORARY.
         */
        TEMPORARY,

        /**
         * The PERMANENT.
         */
        PERMANENT
    }

    /**
     * A method that retrieves all the EssenceTypes present in the MXF file
     * @return a list of all essence types present in the MXF file
     */
    public List<EssenceTypeEnum> getEssenceTypes(){
        List<EssenceTypeEnum> essenceTypes = new ArrayList<>();
        if(this.hasCDCIPictureEssenceDescriptor() || this.hasRGBAPictureEssenceDescriptor()){
            essenceTypes.add(EssenceTypeEnum.MainImageEssence);
        }
        if(this.hasWaveAudioEssenceDescriptor()){
            essenceTypes.add(EssenceTypeEnum.MainAudioEssence);
        }
        return essenceTypes;
    }

    /**
     * An enumeration of all possible essence types that could be contained in a MXF file.
     */
    public enum EssenceTypeEnum {
        MarkerEssence(Composition.SequenceTypeEnum.MarkerSequence),
        MainImageEssence(Composition.SequenceTypeEnum.MainImageSequence),
        MainAudioEssence(Composition.SequenceTypeEnum.MainAudioSequence),
        SubtitlesEssence(Composition.SequenceTypeEnum.SubtitlesSequence),
        HearingImpairedCaptionsEssence(Composition.SequenceTypeEnum.HearingImpairedCaptionsSequence),
        VisuallyImpairedTextEssence(Composition.SequenceTypeEnum.VisuallyImpairedTextSequence),
        CommentaryEssence(Composition.SequenceTypeEnum.CommentarySequence),
        KaraokeEssence(Composition.SequenceTypeEnum.CommentarySequence),
        AncillaryDataEssence(Composition.SequenceTypeEnum.AncillaryDataSequence),
        UnknownEssence(Composition.SequenceTypeEnum.Unknown);

        private final Composition.SequenceTypeEnum sequenceType;
        private final String name;

        private EssenceTypeEnum(Composition.SequenceTypeEnum sequenceType)
        {
            this.sequenceType = sequenceType;
            this.name = getEssenceTypeString(sequenceType);
        }

        private static EssenceTypeEnum getEssenceTypeEnum(String name)
        {
            switch (name)
            {
                case "MainImageEssence":
                    return MainImageEssence;
                case "MainAudioEssence":
                    return MainAudioEssence;
                case "MarkerEssence":
                    return MarkerEssence;
                case "SubtitlesEssence":
                    return SubtitlesEssence;
                case "HearingImpairedCaptionsEssence":
                    return HearingImpairedCaptionsEssence;
                case "VisuallyImpairedTextEssence":
                    return VisuallyImpairedTextEssence;
                case "CommentaryEssence":
                    return CommentaryEssence;
                case "KaraokeEssence":
                    return KaraokeEssence;
                case "AncillaryDataEssence":
                    return AncillaryDataEssence;
                default:
                    return UnknownEssence;
            }
        }

        private static String getEssenceTypeString(Composition.SequenceTypeEnum sequenceType)
        {
            switch (sequenceType)
            {
                case MainImageSequence:
                    return "MainImageEssence";
                case MainAudioSequence:
                    return "MainAudioEssence";
                case MarkerSequence:
                    return "MarkerEssence";
                case SubtitlesSequence:
                    return "SubtitlesEssence";
                case HearingImpairedCaptionsSequence:
                    return "HearingImpairedCaptionsEssence";
                case VisuallyImpairedTextSequence:
                    return "VisuallyImpairedTextEssence";
                case CommentarySequence:
                    return "CommentaryEssence";
                case KaraokeSequence:
                    return "KaraokeEssence";
                case AncillaryDataSequence:
                    return "AncillaryDataEssence";
                default:
                    return "UnknownEssence";
            }
        }

        public String toString(){
            return this.name;
        }
    }

    /**
     * A method that returns a string representation of a HeaderPartition object
     *
     * @return string representing the object
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("================== HeaderPartition ======================\n");
        sb.append(this.getPartitionPack().toString());
        sb.append(this.getPrimerPack().toString());
        sb.append(this.getPreface().toString());
        /*Set<String> keySet = this.interchangeObjectsMap.keySet();
        for(String key : keySet){
            if(key.equals(ContentStorage.class.getSimpleName())){
                sb.append(this.interchangeObjectsMap.get(ContentStorage.class.getSimpleName()).get(0).toString());
            }
            else if(!key.equals(Preface.class.getSimpleName())) {
                for(InterchangeObject object : this.interchangeObjectsMap.get(key)){
                    sb.append(object.toString());
                }
            }
        }*/
        /* According to FindBugs using an iterator over the Map's entrySet() is more efficient than keySet()
         * (WMI_WRONG_MAP_ITERATOR).
         * Since with the entrySet we get both the key and the value thereby eliminating the need to use
         * Map.get(key) to access the value corresponding to a key in the map.
         */
        Set<Map.Entry<String, List<InterchangeObject>>> entrySet = this.interchangeObjectsMap.entrySet();
        for(Map.Entry<String, List<InterchangeObject>> entry : entrySet){
            if(entry.getKey().equals(ContentStorage.class.getSimpleName())){
                sb.append(this.interchangeObjectsMap.get(ContentStorage.class.getSimpleName()).get(0).toString());
            }
            else if(!entry.getKey().equals(Preface.class.getSimpleName())){
                for(InterchangeObject object : entry.getValue()){
                    sb.append(object.toString());
                }
            }
        }
        return sb.toString();
    }
}
