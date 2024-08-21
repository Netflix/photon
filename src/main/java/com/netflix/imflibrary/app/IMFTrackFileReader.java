/*
 *
 *  Copyright 2015 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.netflix.imflibrary.app;

import com.netflix.imflibrary.IMFConstraints;
import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.MXFOperationalPattern1A;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st0377.IndexTableSegment;
import com.netflix.imflibrary.st0377.PartitionPack;
import com.netflix.imflibrary.st0377.RandomIndexPack;
import com.netflix.imflibrary.st0377.StructuralMetadataID;
import com.netflix.imflibrary.st0377.header.EssenceContainerData;
import com.netflix.imflibrary.st0377.header.FileDescriptor;
import com.netflix.imflibrary.st0377.header.GenericPackage;
import com.netflix.imflibrary.st0377.header.InterchangeObject;
import com.netflix.imflibrary.st0377.header.InterchangeObject.InterchangeObjectBO.StrongRef;
import com.netflix.imflibrary.st0377.header.Preface;
import com.netflix.imflibrary.st0377.header.SourcePackage;
import com.netflix.imflibrary.st2067_201.IABTrackFileConstraints;
import com.netflix.imflibrary.st2067_203.MGASADMTrackFileConstraints;
import com.netflix.imflibrary.st2067_204.ADMAudioTrackFileConstraints;
import com.netflix.imflibrary.st2067_204.ADM_CHNASubDescriptor;
import com.netflix.imflibrary.st2067_204.ADM_CHNASubDescriptor.ADM_CHNASubDescriptorBO;
import com.netflix.imflibrary.utils.ByteArrayDataProvider;
import com.netflix.imflibrary.utils.ByteProvider;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.FileByteRangeProvider;
import com.netflix.imflibrary.utils.FileDataProvider;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;
import com.netflix.imflibrary.utils.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * A simple application to exercise the core logic of Photon for reading and validating IMF Track files.
 */
@ThreadSafe
public final class IMFTrackFileReader
{
    private final File workingDirectory;
    private final ResourceByteRangeProvider resourceByteRangeProvider;
    private volatile RandomIndexPack randomIndexPack = null;
    private volatile List<PartitionPack> partitionPacks = null;
    private volatile List<PartitionPack> referencedPartitionPacks = null;
    private volatile IMFConstraints.HeaderPartitionIMF headerPartition;
    private volatile List<IndexTableSegment> indexTableSegments = null;


    private static final Logger logger = LoggerFactory.getLogger(IMFTrackFileReader.class);

    /**
     * Lazily creates a model instance corresponding to a st2067-5 compliant MXF file
     * @param workingDirectory the working directory
     * @param resourceByteRangeProvider the MXF file represented as a {@link com.netflix.imflibrary.utils.ResourceByteRangeProvider}
     */
    public IMFTrackFileReader(File workingDirectory, ResourceByteRangeProvider resourceByteRangeProvider)
    {
        this.workingDirectory = workingDirectory;
        this.resourceByteRangeProvider = resourceByteRangeProvider;
    }

    private IMFConstraints.HeaderPartitionIMF getHeaderPartitionIMF(@Nonnull IMFErrorLogger imfErrorLogger) throws IOException
    {
        if (this.headerPartition == null)
        {
            RandomIndexPack randomIndexPack = getRandomIndexPack(imfErrorLogger);
            List<Long> allPartitionByteOffsets = randomIndexPack.getAllPartitionByteOffsets();
            try {
                setHeaderPartitionIMF(allPartitionByteOffsets.get(0), allPartitionByteOffsets.get(1) - 1, imfErrorLogger);
            }
            catch (IMFException e){
                throw new IMFException(String.format("Could not set Header Partition"), imfErrorLogger);
            }
        }

        return this.headerPartition;
    }

    private HeaderPartition getHeaderPartition(@Nonnull IMFErrorLogger imfErrorLogger) throws IOException
    {
        try {
            IMFConstraints.HeaderPartitionIMF headerPartitionIMF = getHeaderPartitionIMF(imfErrorLogger);
            return headerPartitionIMF.getHeaderPartitionOP1A().getHeaderPartition();
        }
        catch (IMFException e){
            throw new IMFException(String.format("Could not read Header Partition"), imfErrorLogger);
        }
    }

    private void setHeaderPartitionIMF(long inclusiveRangeStart, long inclusiveRangeEnd, @Nonnull IMFErrorLogger imfErrorLogger) throws IOException
    {
        File fileWithHeaderPartition = this.resourceByteRangeProvider.getByteRange(inclusiveRangeStart, inclusiveRangeEnd, this.workingDirectory);
        ByteProvider byteProvider = this.getByteProvider(fileWithHeaderPartition);
        HeaderPartition headerPartition = null;
        try {
            headerPartition = new HeaderPartition(byteProvider, inclusiveRangeStart, inclusiveRangeEnd - inclusiveRangeStart + 1, imfErrorLogger);
            //validate header partition
            MXFOperationalPattern1A.HeaderPartitionOP1A headerPartitionOP1A = MXFOperationalPattern1A.checkOperationalPattern1ACompliance(headerPartition, imfErrorLogger);
            this.headerPartition = IMFConstraints.checkIMFCompliance(headerPartitionOP1A, imfErrorLogger);
            if (this.headerPartition != null) {
                IABTrackFileConstraints.checkCompliance(this.headerPartition, imfErrorLogger);
                MGASADMTrackFileConstraints.checkCompliance(this.headerPartition, imfErrorLogger);
                ADMAudioTrackFileConstraints.checkComplianceFromIMFTrackFileReader(this.headerPartition, imfErrorLogger);
            }
        }
        catch (MXFException | IMFException e){
            if(headerPartition == null){
                imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("IMFTrackFile has fatal errors")));
            }
            else {
                Preface preface = headerPartition.getPreface();
                GenericPackage genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
                SourcePackage filePackage = (SourcePackage) genericPackage;
                UUID packageUUID = filePackage.getPackageMaterialNumberasUUID();
                imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("IMFTrackFile with ID %s has fatal errors", packageUUID.toString())));
            }
            if(e instanceof IMFException){
                IMFException imfException = (IMFException)e;
                imfErrorLogger.addAllErrors(imfException.getErrors());
            }
            else if(e instanceof MXFException){
                MXFException mxfException = (MXFException)e;
                imfErrorLogger.addAllErrors(mxfException.getErrors());
            }
            throw new IMFException(String.format("Fatal errors in the IMFTrackFile's Header Partition"), imfErrorLogger);
        }
    }

    private List<IndexTableSegment> getIndexTableSegments(@Nonnull IMFErrorLogger imfErrorLogger) throws IOException
    {
        if (this.indexTableSegments == null)
        {
            setIndexTableSegments(imfErrorLogger);
        }
        return Collections.unmodifiableList(this.indexTableSegments);
    }

    private void setIndexTableSegments(@Nonnull IMFErrorLogger imfErrorLogger) throws IOException
    {
        RandomIndexPack randomIndexPack = getRandomIndexPack(imfErrorLogger);

        List<IndexTableSegment> indexTableSegments = new ArrayList<>();
        List<Long> allPartitionByteOffsets = randomIndexPack.getAllPartitionByteOffsets();

        int numPartitions = allPartitionByteOffsets.size();
        for (int i=0; i<numPartitions -1; i++)
        {
            indexTableSegments.addAll(getIndexTableSegments(allPartitionByteOffsets.get(i), allPartitionByteOffsets.get(i+1) -1));
        }
        indexTableSegments.addAll(getIndexTableSegments(
                allPartitionByteOffsets.get(numPartitions -1), this.resourceByteRangeProvider.getResourceSize() - 1));

        this.indexTableSegments = Collections.unmodifiableList(indexTableSegments);
    }

    private List<IndexTableSegment> getIndexTableSegments(long inclusivePartitionStart, long inclusivePartitionEnd) throws IOException
    {
        long archiveFileSize = this.resourceByteRangeProvider.getResourceSize();
        KLVPacket.Header header;
        {//logic to provide as an input stream the portion of the archive that contains PartitionPack KLVPacker Header
            long rangeEnd = inclusivePartitionStart +
                    (KLVPacket.KEY_FIELD_SIZE + KLVPacket.LENGTH_FIELD_SUFFIX_MAX_SIZE) -1;
            rangeEnd = rangeEnd < (archiveFileSize - 1) ? rangeEnd : (archiveFileSize - 1);

            File fileWithPartitionPackKLVPacketHeader = this.resourceByteRangeProvider.getByteRange(inclusivePartitionStart, rangeEnd, this.workingDirectory);
            ByteProvider byteProvider = this.getByteProvider(fileWithPartitionPackKLVPacketHeader);
            header = new KLVPacket.Header(byteProvider, inclusivePartitionStart);
        }

        PartitionPack partitionPack;
        {//logic to provide as an input stream the portion of the archive that contains a PartitionPack

            long rangeEnd = inclusivePartitionStart +
                    (KLVPacket.KEY_FIELD_SIZE + header.getLSize() + header.getVSize()) -1;
            rangeEnd = rangeEnd < (archiveFileSize - 1) ? rangeEnd : (archiveFileSize - 1);

            File fileWithPartitionPack = this.resourceByteRangeProvider.getByteRange(inclusivePartitionStart, rangeEnd, this.workingDirectory);
            ByteProvider byteProvider = this.getByteProvider(fileWithPartitionPack);
            partitionPack = new PartitionPack(byteProvider, inclusivePartitionStart, false);
        }

        List<IndexTableSegment> indexTableSegments = new ArrayList<>();
        if (partitionPack.hasIndexTableSegments())
        {//logic to provide as an input stream the portion of the archive that contains a Partition
            long byteOffset = inclusivePartitionStart;
            long rangeEnd = inclusivePartitionEnd;
            rangeEnd = rangeEnd < (archiveFileSize - 1) ? rangeEnd : (archiveFileSize - 1);

            File fileWithPartition = this.resourceByteRangeProvider.getByteRange(inclusivePartitionStart, rangeEnd, this.workingDirectory);
            ByteProvider byteProvider = this.getByteProvider(fileWithPartition);

            long numBytesToRead = rangeEnd - inclusivePartitionStart + 1;
            long numBytesRead = 0;
            while (numBytesRead < numBytesToRead)
            {
                header = new KLVPacket.Header(byteProvider, byteOffset);
                numBytesRead += header.getKLSize();

                if (IndexTableSegment.isValidKey(header.getKey()))
                {
                    indexTableSegments.add(new IndexTableSegment(byteProvider, header));
                }
                else
                {
                    byteProvider.skipBytes(header.getVSize());
                }
                numBytesRead += header.getVSize();
                byteOffset += numBytesRead;
            }

        }

        return indexTableSegments;
    }

    private List<PartitionPack> getReferencedPartitionPacks(@Nonnull IMFErrorLogger imfErrorLogger) throws IOException
    {
        if (this.referencedPartitionPacks == null)
        {
            setReferencedPartitionPacks(imfErrorLogger);
        }

        return this.referencedPartitionPacks;
    }

    private void setReferencedPartitionPacks(@Nonnull IMFErrorLogger imfErrorLogger) throws IOException
    {
        List<PartitionPack> allPartitionPacks = getPartitionPacks(imfErrorLogger);
        HeaderPartition headerPartition = getHeaderPartition(imfErrorLogger);

        Set<Long> indexSIDs = new HashSet<>();
        Set<Long> bodySIDs = new HashSet<>();

        for (EssenceContainerData essenceContainerData : headerPartition.getPreface().getContentStorage().getEssenceContainerDataList())
        {
            indexSIDs.add(essenceContainerData.getIndexSID());
            bodySIDs.add(essenceContainerData.getBodySID());
        }

        List<PartitionPack> referencedPartitionPacks = new ArrayList<>();
        for (PartitionPack partitionPack : allPartitionPacks)
        {
            if (partitionPack.hasEssenceContainer())
            {
                if (bodySIDs.contains(partitionPack.getBodySID()))
                {
                    referencedPartitionPacks.add(partitionPack);
                }
            }
            else if (partitionPack.hasIndexTableSegments())
            {
                if (indexSIDs.contains(partitionPack.getIndexSID()))
                {
                    referencedPartitionPacks.add(partitionPack);
                }
            }
            else if(partitionPack.getPartitionPackType() == PartitionPack.PartitionPackType.HeaderPartitionPack
                    || partitionPack.getPartitionPackType() == PartitionPack.PartitionPackType.FooterPartitionPack)
            {//Either of these partitions are important although they might not contain EssenceContainer or IndexTable data
                referencedPartitionPacks.add(partitionPack);
            }
        }

        this.referencedPartitionPacks = referencedPartitionPacks;
    }

    private List<PartitionPack> getPartitionPacks(@Nonnull IMFErrorLogger imfErrorLogger) throws IOException
    {
        if (this.partitionPacks == null)
        {
            setPartitionPacks(imfErrorLogger);
        }
        return Collections.unmodifiableList(this.partitionPacks);

    }

    List<String> getPartitionPacksType(@Nonnull IMFErrorLogger imfErrorLogger) throws IOException
    {
        if (this.partitionPacks == null)
        {
            setPartitionPacks(imfErrorLogger);
        }
        ArrayList<String> partitionPackTypeString = new ArrayList<String>();
        for(PartitionPack partitionPack : this.partitionPacks){
            partitionPackTypeString.add(partitionPack.getPartitionPackType().getPartitionTypeString());
        }
        return Collections.unmodifiableList(partitionPackTypeString);

    }

    private void setPartitionPacks(@Nonnull IMFErrorLogger imfErrorLogger) throws IOException
    {
        RandomIndexPack randomIndexPack = getRandomIndexPack(imfErrorLogger);

        List<PartitionPack> partitionPacks = new ArrayList<>();
        List<Long> allPartitionByteOffsets = randomIndexPack.getAllPartitionByteOffsets();
        for (long offset : allPartitionByteOffsets)
        {
            partitionPacks.add(getPartitionPack(offset));
        }
        try {
            //validate partition packs
            MXFOperationalPattern1A.checkOperationalPattern1ACompliance(partitionPacks);
            IMFConstraints.checkIMFCompliance(partitionPacks, imfErrorLogger);
        }
        catch (IMFException | MXFException e){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("This IMFTrackFile has fatal errors in the partition packs, please see the errors that follow."));
            if(e instanceof IMFException){
                IMFException imfException = (IMFException)e;
                imfErrorLogger.addAllErrors(imfException.getErrors());
            }
            else if(e instanceof MXFException){
                MXFException mxfException = (MXFException)e;
                imfErrorLogger.addAllErrors(mxfException.getErrors());
            }
        }

        //add reference to list of partition packs
        this.partitionPacks  = Collections.unmodifiableList(partitionPacks);
    }

    private PartitionPack getPartitionPack(long resourceOffset) throws IOException
    {
        long archiveFileSize = this.resourceByteRangeProvider.getResourceSize();
        KLVPacket.Header header;
        {//logic to provide as an input stream the portion of the archive that contains PartitionPack KLVPacker Header
            long rangeEnd = resourceOffset +
                    (KLVPacket.KEY_FIELD_SIZE + KLVPacket.LENGTH_FIELD_SUFFIX_MAX_SIZE) -1;
            rangeEnd = rangeEnd < (archiveFileSize - 1) ? rangeEnd : (archiveFileSize - 1);

            File fileWithPartitionPackKLVPacketHeader = this.resourceByteRangeProvider.getByteRange(resourceOffset, rangeEnd, this.workingDirectory);
            ByteProvider byteProvider = this.getByteProvider(fileWithPartitionPackKLVPacketHeader);
            header = new KLVPacket.Header(byteProvider, resourceOffset);
        }

        PartitionPack partitionPack;
        {//logic to provide as an input stream the portion of the archive that contains a PartitionPack and next KLV header

            long rangeEnd = resourceOffset +
                    (KLVPacket.KEY_FIELD_SIZE + header.getLSize() + header.getVSize()) +
                    (KLVPacket.KEY_FIELD_SIZE + KLVPacket.LENGTH_FIELD_SUFFIX_MAX_SIZE) +
                    -1;
            rangeEnd = rangeEnd < (archiveFileSize - 1) ? rangeEnd : (archiveFileSize - 1);

            File fileWithPartitionPack = this.resourceByteRangeProvider.getByteRange(resourceOffset, rangeEnd, this.workingDirectory);
            ByteProvider byteProvider = this.getByteProvider(fileWithPartitionPack);
            partitionPack = new PartitionPack(byteProvider, resourceOffset, true);

        }

        return partitionPack;
    }

    /**
     * Returns a model instance corresponding to the RandomIndexPack section of the MXF file
     * @return a {@link com.netflix.imflibrary.st0377.RandomIndexPack} representation of the random index pack section
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    RandomIndexPack getRandomIndexPack(@Nonnull IMFErrorLogger imfErrorLogger) throws IOException
    {
        if (this.randomIndexPack == null)
        {
            setRandomIndexPack(imfErrorLogger);
        }
        return this.randomIndexPack;
    }

    private void setRandomIndexPack(@Nonnull IMFErrorLogger imfErrorLogger) throws IOException
    {

        long archiveFileSize = this.resourceByteRangeProvider.getResourceSize();
        long randomIndexPackSize;
        {//logic to provide as an input stream the portion of the archive that contains randomIndexPack size
            long rangeEnd = archiveFileSize - 1;
            long rangeStart = archiveFileSize - 4;

            File fileWithRandomIndexPackSize = this.resourceByteRangeProvider.getByteRange(rangeStart, rangeEnd, this.workingDirectory);
            byte[] bytes = Files.readAllBytes(Paths.get(fileWithRandomIndexPackSize.toURI()));
            randomIndexPackSize = (long)(ByteBuffer.wrap(bytes).getInt());
        }
        //RandomIndexPack size min value = 16 + 4 + 36 + 4
        // 16 bytes for the UL, 4 bytes for the overall length of the pack, 3 * 12 bytes since we expect to see atleast 3 partitions, 4 bytes overall length of the pack including the SetKey, Pack Length and SID/Offset fields
        if(randomIndexPackSize < 60){
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("RandomIndexPackSize = %d is smaller than what is required to reliably contain a minimum number of Partition Byte Offsets.", randomIndexPackSize));
            throw new MXFException(String.format("RandomIndexPackSize = %d is smaller than what is required to reliably contain a minimum number of Partition Byte Offsets.", randomIndexPackSize), imfErrorLogger);
        }
        RandomIndexPack randomIndexPack;
        {//logic to provide as an input stream the portion of the archive that contains randomIndexPack
            long rangeEnd = archiveFileSize - 1;
            long rangeStart = archiveFileSize - randomIndexPackSize;
            if (rangeStart < 0)
            {
                throw new MXFException(String.format("RandomIndexPackSize = %d obtained from last 4 bytes of the MXF file is larger than archiveFile size = %d, implying that this file does not contain a RandomIndexPack",
                        randomIndexPackSize, archiveFileSize));
            }

            File fileWithRandomIndexPack = this.resourceByteRangeProvider.getByteRange(rangeStart, rangeEnd, this.workingDirectory);
            ByteProvider byteProvider = this.getByteProvider(fileWithRandomIndexPack);
            randomIndexPack = new RandomIndexPack(byteProvider, rangeStart, randomIndexPackSize);
        }

        this.randomIndexPack =  randomIndexPack;

    }

    /**
     * A method that returns a list of EssenceDescriptor objects referenced by the Source Packages in this HeaderPartition
     * @param imfErrorLogger an error logger for recording any errors - cannot be null
     * @return List<InterchangeObjectBO></> corresponding to every EssenceDescriptor in the underlying resource
     * @throws IOException - any I/O related error will be exposed through an IOException
     */
    List<InterchangeObject.InterchangeObjectBO> getEssenceDescriptors(@Nonnull IMFErrorLogger imfErrorLogger) throws IOException {
        return this.getHeaderPartition(imfErrorLogger).getEssenceDescriptors();
    }

    /**
     * A method that returns an MXF KLV Header corresponding to an EssenceDescriptor
     * @param essenceDescriptor corresponding to the essence in the MXF file
     * @return List<MXFKLVPacket.Header></> corresponding to every EssenceDescriptor in the underlying resource
     */
    KLVPacket.Header getEssenceDescriptorKLVHeader(InterchangeObject.InterchangeObjectBO essenceDescriptor) throws IOException {
        return essenceDescriptor.getHeader();
    }

    /**
     * A method that returns a list of MXF KLV Header corresponding to the SubDescriptors in the MXF file
     * @param imfErrorLogger an error logger for recording any errors - cannot be null
     * @return List<MXFKLVPacket.Header></> corresponding to every SubDescriptor in the underlying resource
     * @throws IOException - any I/O related error will be exposed through an IOException
     */
    List<KLVPacket.Header> getSubDescriptors(@Nonnull IMFErrorLogger imfErrorLogger) throws IOException {
        List<InterchangeObject.InterchangeObjectBO> subDescriptors = this.getHeaderPartition(imfErrorLogger).getSubDescriptors();
        List<KLVPacket.Header> subDescriptorHeaders = new ArrayList<>();
        for(InterchangeObject.InterchangeObjectBO subDescriptorBO : subDescriptors){
            if(subDescriptorBO != null) {
                subDescriptorHeaders.add(subDescriptorBO.getHeader());
            }
        }
        return subDescriptorHeaders;
    }

    /**
     * A method that returns the MXF KLV header corresponding to an EssenceDescriptor in the MXF file
     * @param essenceDescriptor corresponding to the essence in the MXF file
     * @param imfErrorLogger an error logger for recording any errors - cannot be null
     * @return List<MXFKLVPacket.Header></> corresponding to every SubDescriptor in the underlying resource
     * @throws IOException - any I/O related error will be exposed through an IOException
     */
    List<KLVPacket.Header> getSubDescriptorKLVHeader(InterchangeObject.InterchangeObjectBO essenceDescriptor, @Nonnull IMFErrorLogger imfErrorLogger) throws IOException {
        List<KLVPacket.Header> subDescriptorHeaders = new ArrayList<>();
        List<InterchangeObject.InterchangeObjectBO>subDescriptors = this.getHeaderPartition(imfErrorLogger).getSubDescriptors(essenceDescriptor);
        List<InterchangeObject.InterchangeObjectBO> references = new ArrayList<>();
        for (InterchangeObject.InterchangeObjectBO sub : subDescriptors) {
            if (sub.getClass().getSimpleName().equals(ADM_CHNASubDescriptorBO.class.getSimpleName())) {
                ADM_CHNASubDescriptor.ADM_CHNASubDescriptorBO adm = (ADM_CHNASubDescriptor.ADM_CHNASubDescriptorBO) sub;
                    for (StrongRef strongRef : adm.getADMChannelMappingsArray().getEntries()) {
                        references.add(this.getHeaderPartition(imfErrorLogger).getUidToBOs().get(strongRef.getInstanceUID()));
                    }
            }
        }
        if (!references.isEmpty()) {
            for (InterchangeObject.InterchangeObjectBO reference: references) {
                subDescriptors.add(reference);
            }
        }
        for(InterchangeObject.InterchangeObjectBO subDescriptorBO : subDescriptors){
            if(subDescriptorBO != null) {
                subDescriptorHeaders.add(subDescriptorBO.getHeader());
            }
        }
        return subDescriptorHeaders;
    }

    /**
     * A method to get the EssenceType
     * @param imfErrorLogger an error logger for recording any errors - cannot be null
     * @return a String representing the Essence type
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    HeaderPartition.EssenceTypeEnum getEssenceType(@Nonnull IMFErrorLogger imfErrorLogger) throws IOException {
        Set<HeaderPartition.EssenceTypeEnum> supportedEssenceComponentTypes = new HashSet<>();
        supportedEssenceComponentTypes.add(HeaderPartition.EssenceTypeEnum.MainImageEssence);
        supportedEssenceComponentTypes.add(HeaderPartition.EssenceTypeEnum.MainAudioEssence);
        supportedEssenceComponentTypes.add(HeaderPartition.EssenceTypeEnum.MarkerEssence);
        supportedEssenceComponentTypes.add(HeaderPartition.EssenceTypeEnum.IABEssence);
        supportedEssenceComponentTypes.add(HeaderPartition.EssenceTypeEnum.MGASADMEssence);
        List<HeaderPartition.EssenceTypeEnum> supportedEssenceTypesFound = new ArrayList<>();
        List<HeaderPartition.EssenceTypeEnum> essenceTypes = this.getHeaderPartitionIMF(imfErrorLogger).getHeaderPartitionOP1A().getHeaderPartition().getEssenceTypes();

        for(HeaderPartition.EssenceTypeEnum essenceTypeEnum : essenceTypes){
            if(supportedEssenceComponentTypes.contains(essenceTypeEnum)){
                supportedEssenceTypesFound.add(essenceTypeEnum);
            }
        }

        if(supportedEssenceTypesFound.size() > 0) {
            if (supportedEssenceTypesFound.size() > 1) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                        String.format("IMFTrack file seems to have multiple supported essence component types %s only 1 is allowed per IMF Core Constraints, returning the first supported EssenceType", Utilities.serializeObjectCollectionToString(supportedEssenceTypesFound)));
            }
            return supportedEssenceTypesFound.get(0);
        }
        else {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                    String.format("IMFTrack file does not seem to have a supported essence component type, essence types supported %n%s, essence types found %n%s"
                            , Utilities.serializeObjectCollectionToString(supportedEssenceComponentTypes), Utilities.serializeObjectCollectionToString(essenceTypes)));
            return HeaderPartition.EssenceTypeEnum.UnsupportedEssence;
        }
    }

    /**
     * An accessor for the PrimerPack KLV header
     * @param imfErrorLogger an error logger for recording any errors - cannot be null
     * @return a File containing the Primer pack
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    KLVPacket.Header getPrimerPackHeader(@Nonnull IMFErrorLogger imfErrorLogger) throws IOException {
        return this.getHeaderPartition(imfErrorLogger).getPrimerPack().getHeader();
    }

    /**
     * A helper method to retrieve a ByteProvider for the KLV packet
     *
     * @param header representing the object model of a KLV packet
     * @return a ByteProvider corresponding to the header
     * @throws IOException
     */
    ByteProvider getByteProvider(KLVPacket.Header header) throws IOException {
        File file = this.resourceByteRangeProvider.getByteRange(header.getByteOffset(), header.getByteOffset() + header.getKLSize() + header.getVSize(), this.workingDirectory);
        return this.getByteProvider(file);
    }

    private ByteProvider getByteProvider(File file) throws IOException {
        ByteProvider byteProvider;
        Long size = file.length();
        if(size <= 0){
            throw new IOException(String.format("Range of bytes (%d) has to be +ve and non-zero", size));
        }
        if(size <= Integer.MAX_VALUE) {
            byte[] bytes = Files.readAllBytes(Paths.get(file.toURI()));
            byteProvider = new ByteArrayDataProvider(bytes);
        }
        else{
            byteProvider = new FileDataProvider(file);
        }
        return byteProvider;
    }

    /**
     * An accessor that returns a list of InterchangeObjectBO by name
     *
     * @param structuralMetadataID of the InterchangeObject requested.
     * @param imfErrorLogger an error logger for recording any errors - cannot be null
     * @return a list of interchangeObjectBOs corresponding to the ID. Null if there is no InterchangeObjectBO with the ID
     * with the name passed in.
     */
    @Nullable
    List<InterchangeObject.InterchangeObjectBO> getStructuralMetadataByName(StructuralMetadataID structuralMetadataID, @Nonnull IMFErrorLogger imfErrorLogger) throws IOException {
        return this.getHeaderPartition(imfErrorLogger).getStructuralMetadata(structuralMetadataID);
    }

    /**
     * Returns the EditRate as a BigInteger
     *
     * @param imfErrorLogger an error logger for recording any errors - cannot be null
     * @return a BigInteger representing the EditRate of the essence
     */
    BigInteger getEssenceEditRate(@Nonnull IMFErrorLogger imfErrorLogger) throws IOException {
        BigInteger result = BigInteger.valueOf(0);
        if(!(this.getHeaderPartition(imfErrorLogger).getEssenceDescriptors().size() > 0)){
            imfErrorLogger.addError(new ErrorLogger.ErrorObject(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, String.format("No EssenceDescriptors were found in " +
                    "the MXF essence Header Partition")));
        }
        InterchangeObject.InterchangeObjectBO essenceDescriptor = this.getHeaderPartition(imfErrorLogger).getEssenceDescriptors().get(0);
        if(FileDescriptor.FileDescriptorBO.class.isAssignableFrom(essenceDescriptor.getClass())){
            FileDescriptor.FileDescriptorBO fileDescriptorBO = (FileDescriptor.FileDescriptorBO) essenceDescriptor;
            List<Long>list = fileDescriptorBO.getSampleRate();
            Long value = list.get(0)/list.get(1);
            result = BigInteger.valueOf(value);
        }
        return result;
    }

    /**
     * Returns the EditRate as a list containing the numerator and denominator
     * @param imfErrorLogger an error logger for recording any errors - cannot be null
     * @return editRate of the essence as a List of Long Integers
     */
    public List<Long> getEssenceEditRateAsList(@Nonnull IMFErrorLogger imfErrorLogger) throws IOException {
        if(!(this.getHeaderPartition(imfErrorLogger).getEssenceDescriptors().size() > 0)){
            throw new MXFException(String.format("No EssenceDescriptors were found in the MXF essence"));
        }
        FileDescriptor.FileDescriptorBO fileDescriptorBO = (FileDescriptor.FileDescriptorBO) this.getHeaderPartition(imfErrorLogger).getEssenceDescriptors().get(0);
        return fileDescriptorBO.getSampleRate();
    }

    /**
     * Return the duration of the Essence including the source duration of all the Structural Components in the MXF Sequence
     * @param imfErrorLogger an error logger for recording any errors - cannot be null
     * @return essenceDuration
     */
    public BigInteger getEssenceDuration(@Nonnull IMFErrorLogger imfErrorLogger) throws IOException {
        return this.getHeaderPartition(imfErrorLogger).getEssenceDuration();
    }

    /**
     * A method to return the TrackFileId which is a UUID identifying the track file
     * @return UUID identifying the Track File
     */
    UUID getTrackFileId(){

        Preface preface = this.headerPartition.getHeaderPartitionOP1A().getHeaderPartition().getPreface();
        GenericPackage genericPackage = preface.getContentStorage().getEssenceContainerDataList().get(0).getLinkedPackage();
        SourcePackage filePackage = (SourcePackage)genericPackage;

        UUID packageUUID = filePackage.getPackageMaterialNumberasUUID();
        return packageUUID;
    }

    /**
     * A getter for the AudioEssence Language
     * @param imfErrorLogger an error logger for recording any errors - cannot be null
     * @return a string representing the language code in the Audio Essence
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    String getAudioEssenceLanguage(@Nonnull IMFErrorLogger imfErrorLogger) throws IOException {
        return this.getHeaderPartition(imfErrorLogger).getAudioEssenceSpokenLanguage();
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        try
        {
            sb.append(this.getRandomIndexPack(imfErrorLogger));
            sb.append(this.getPartitionPacks(imfErrorLogger));
            sb.append(this.getReferencedPartitionPacks(imfErrorLogger));
            sb.append(this.getIndexTableSegments(imfErrorLogger));
            sb.append(this.getHeaderPartitionIMF(imfErrorLogger));
        }
        catch(IOException e)
        {
            throw new IMFException(e.getMessage(), imfErrorLogger);
        }
        catch (IMFException | MXFException e){
            if(e instanceof IMFException){
                IMFException imfException = (IMFException)e;
                imfErrorLogger.addAllErrors(imfException.getErrors());
            }
            else if(e instanceof MXFException){
                MXFException mxfException = (MXFException)e;
                imfErrorLogger.addAllErrors(mxfException.getErrors());
            }
            logger.error(Utilities.serializeObjectCollectionToString(imfErrorLogger.getErrors()));
        }
        return sb.toString();
    }

    private static String usage()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Usage:%n"));
        sb.append(String.format("%s <inputFilePath> <workingDirectory>%n", IMFTrackFileReader.class.getName()));
        return sb.toString();
    }

    public static void main(String[] args) throws IOException
    {

        if (args.length != 2)
        {
            logger.error(usage());
            throw new IllegalArgumentException("Invalid parameters");
        }

        File inputFile = new File(args[0]);
        if(!inputFile.exists()){
            logger.error(String.format("File %s does not exist", inputFile.getAbsolutePath()));
            System.exit(-1);
        }
        File workingDirectory = new File(args[1]);

        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        IMFTrackFileReader imfTrackFileReader = null;
        IMFTrackFileCPLBuilder imfTrackFileCPLBuilder = null;
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        try {
            imfTrackFileReader = new IMFTrackFileReader(workingDirectory, resourceByteRangeProvider);
            imfTrackFileCPLBuilder = new IMFTrackFileCPLBuilder(workingDirectory, inputFile);
        }
        catch (IMFException | MXFException e){
            if(e instanceof IMFException){
                IMFException imfException = (IMFException)e;
                imfErrorLogger.addAllErrors(imfException.getErrors());
            }
            else if(e instanceof MXFException){
                MXFException mxfException = (MXFException)e;
                imfErrorLogger.addAllErrors(mxfException.getErrors());
            }
            imfErrorLogger.addAllErrors(imfErrorLogger.getErrors());
        }
        Set<HeaderPartition.EssenceTypeEnum> supportedEssenceComponentTypes = new HashSet<>();
        supportedEssenceComponentTypes.add(HeaderPartition.EssenceTypeEnum.MainImageEssence);
        supportedEssenceComponentTypes.add(HeaderPartition.EssenceTypeEnum.MainAudioEssence);
        supportedEssenceComponentTypes.add(HeaderPartition.EssenceTypeEnum.MarkerEssence);
        supportedEssenceComponentTypes.add(HeaderPartition.EssenceTypeEnum.MGASADMEssence);
        if(imfTrackFileReader != null
                && imfTrackFileCPLBuilder != null
                && supportedEssenceComponentTypes.contains(imfTrackFileReader.getEssenceType(imfErrorLogger))) {
            try {
                for (InterchangeObject.InterchangeObjectBO essenceDescriptor : imfTrackFileReader.getEssenceDescriptors(imfErrorLogger)) {
                /* create dom */
                    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                    Document document = docBuilder.newDocument();
                /*Output file containing the RegXML representation of the EssenceDescriptor*/
                    KLVPacket.Header essenceDescriptorHeader = essenceDescriptor.getHeader();
                    List<KLVPacket.Header> subDescriptorHeaders = imfTrackFileReader.getSubDescriptorKLVHeader(essenceDescriptor, imfErrorLogger);
                    File outputFile = imfTrackFileCPLBuilder.getEssenceDescriptorAsXMLFile(document, essenceDescriptorHeader, subDescriptorHeaders, imfErrorLogger);
                    logger.info(String.format("The EssenceDescriptor in the IMFTrackFile has been written to a XML document at the following location %s", outputFile.getAbsolutePath()));
                }
            } catch (ParserConfigurationException | TransformerException e) {
                throw new MXFException(e);
            }
        }
        List<ErrorLogger.ErrorObject> errors = imfErrorLogger.getErrors();
        if(errors.size() > 0){
            long warningCount = errors.stream().filter(e -> e.getErrorLevel().equals(IMFErrorLogger.IMFErrors.ErrorLevels
                    .WARNING)).count();
            logger.info(String.format("IMFTrackFile has %d errors and %d warnings",
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
            /*if(imfTrackFileReader != null
                    && imfTrackFileCPLBuilder != null) {
                logger.info(String.format("%n %s", imfTrackFileReader.toString()));
            }*/
            logger.info("No errors were detected in the IMFTrackFile");
        }
    }
}
