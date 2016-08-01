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

import com.netflix.imflibrary.*;
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
import com.netflix.imflibrary.st0377.header.Preface;
import com.netflix.imflibrary.st0377.header.SourcePackage;
import com.netflix.imflibrary.utils.ByteArrayDataProvider;
import com.netflix.imflibrary.utils.ByteProvider;
import com.netflix.imflibrary.utils.FileByteRangeProvider;
import com.netflix.imflibrary.utils.FileDataProvider;
import com.netflix.imflibrary.utils.ResourceByteRangeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

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
import java.util.*;

/**
 * A simple application to exercise the core logic of Photon for reading and validating IMF Track files.
 */
@ThreadSafe
final class IMFTrackFileReader
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
    IMFTrackFileReader(File workingDirectory, ResourceByteRangeProvider resourceByteRangeProvider)
    {
        this.workingDirectory = workingDirectory;
        this.resourceByteRangeProvider = resourceByteRangeProvider;
    }

    private IMFConstraints.HeaderPartitionIMF getHeaderPartitionIMF(IMFErrorLogger imfErrorLogger) throws IOException
    {
        if (this.headerPartition == null)
        {
            RandomIndexPack randomIndexPack = getRandomIndexPack();
            List<Long> allPartitionByteOffsets = randomIndexPack.getAllPartitionByteOffsets();
            setHeaderPartitionIMF(allPartitionByteOffsets.get(0), allPartitionByteOffsets.get(1) - 1, imfErrorLogger);
        }

        return this.headerPartition;
    }

    private HeaderPartition getHeaderPartition(IMFErrorLogger imfErrorLogger) throws IOException
    {
        IMFConstraints.HeaderPartitionIMF headerPartitionIMF = getHeaderPartitionIMF(imfErrorLogger);
        return headerPartitionIMF.getHeaderPartitionOP1A().getHeaderPartition();
    }

    private void setHeaderPartitionIMF(long inclusiveRangeStart, long inclusiveRangeEnd, IMFErrorLogger imfErrorLogger) throws IOException
    {
        File fileWithHeaderPartition = this.resourceByteRangeProvider.getByteRange(inclusiveRangeStart, inclusiveRangeEnd, this.workingDirectory);
        ByteProvider byteProvider = this.getByteProvider(fileWithHeaderPartition);
        HeaderPartition headerPartition = new HeaderPartition(byteProvider, inclusiveRangeStart, inclusiveRangeEnd - inclusiveRangeStart + 1, imfErrorLogger);

        //validate header partition
        MXFOperationalPattern1A.HeaderPartitionOP1A headerPartitionOP1A = MXFOperationalPattern1A.checkOperationalPattern1ACompliance(headerPartition);
        this.headerPartition = IMFConstraints.checkIMFCompliance(headerPartitionOP1A);
    }

    private List<IndexTableSegment> getIndexTableSegments() throws IOException
    {
        if (this.indexTableSegments == null)
        {
            setIndexTableSegments();
        }
        return Collections.unmodifiableList(this.indexTableSegments);
    }

    private void setIndexTableSegments() throws IOException
    {
        RandomIndexPack randomIndexPack = getRandomIndexPack();

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

    private List<PartitionPack> getReferencedPartitionPacks(IMFErrorLogger imfErrorLogger) throws IOException
    {
        if (this.referencedPartitionPacks == null)
        {
            setReferencedPartitionPacks(imfErrorLogger);
        }

        return this.referencedPartitionPacks;
    }

    private void setReferencedPartitionPacks(IMFErrorLogger imfErrorLogger) throws IOException
    {
        List<PartitionPack> allPartitionPacks = getPartitionPacks();
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

    private List<PartitionPack> getPartitionPacks() throws IOException
    {
        if (this.partitionPacks == null)
        {
            setPartitionPacks();
        }
        return Collections.unmodifiableList(this.partitionPacks);

    }

    List<String> getPartitionPacksType() throws IOException
    {
        if (this.partitionPacks == null)
        {
            setPartitionPacks();
        }
        ArrayList<String> partitionPackTypeString = new ArrayList<String>();
        for(PartitionPack partitionPack : this.partitionPacks){
            partitionPackTypeString.add(partitionPack.getPartitionPackType().getPartitionTypeString());
        }
        return Collections.unmodifiableList(partitionPackTypeString);

    }

    private void setPartitionPacks() throws IOException
    {
        RandomIndexPack randomIndexPack = getRandomIndexPack();

        List<PartitionPack> partitionPacks = new ArrayList<>();
        List<Long> allPartitionByteOffsets = randomIndexPack.getAllPartitionByteOffsets();
        for (long offset : allPartitionByteOffsets)
        {
            partitionPacks.add(getPartitionPack(offset));
        }

        //validate partition packs
        MXFOperationalPattern1A.checkOperationalPattern1ACompliance(partitionPacks);
        IMFConstraints.checkIMFCompliance(partitionPacks);

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
     * @throws IOException
     */
    RandomIndexPack getRandomIndexPack() throws IOException
    {
        if (this.randomIndexPack == null)
        {
            setRandomIndexPack();
        }
        return this.randomIndexPack;
    }

    private void setRandomIndexPack() throws IOException
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

        RandomIndexPack randomIndexPack;
        {//logic to provide as an input stream the portion of the archive that contains randomIndexPack
            long rangeEnd = archiveFileSize - 1;
            long rangeStart = archiveFileSize - randomIndexPackSize;
            if (rangeStart < 0)
            {
                throw new MXFException(String.format("randomIndexPackSize = %d obtained from last 4 bytes of the MXF file is larger than archiveFile size = %d, implying that this file does not contain a RandomIndexPack",
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
     *
     * @return List<InterchangeObjectBO></> corresponding to every EssenceDescriptor in the underlying resource
     * @throws IOException - any I/O related error will be exposed through an IOException
     */
    List<InterchangeObject.InterchangeObjectBO> getEssenceDescriptors() throws IOException {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
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
     *
     * @return List<MXFKLVPacket.Header></> corresponding to every SubDescriptor in the underlying resource
     * @throws IOException - any I/O related error will be exposed through an IOException
     */
    List<KLVPacket.Header> getSubDescriptors() throws IOException {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
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
     * @return List<MXFKLVPacket.Header></> corresponding to every SubDescriptor in the underlying resource
     * @throws IOException - any I/O related error will be exposed through an IOException
     */
    List<KLVPacket.Header> getSubDescriptorKLVHeader(InterchangeObject.InterchangeObjectBO essenceDescriptor) throws IOException {
        List<KLVPacket.Header> subDescriptorHeaders = new ArrayList<>();
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        List<InterchangeObject.InterchangeObjectBO>subDescriptors = this.getHeaderPartition(imfErrorLogger).getSubDescriptors(essenceDescriptor);
        for(InterchangeObject.InterchangeObjectBO subDescriptorBO : subDescriptors){
            if(subDescriptorBO != null) {
                subDescriptorHeaders.add(subDescriptorBO.getHeader());
            }
        }
        return subDescriptorHeaders;
    }

    String getEssenceType() throws IOException {
        String result = "";
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        HeaderPartition headerPartition = this.getHeaderPartitionIMF(imfErrorLogger).getHeaderPartitionOP1A().getHeaderPartition();
        if(headerPartition.hasCDCIPictureEssenceDescriptor() || headerPartition.hasRGBAPictureEssenceDescriptor()){
            result = "MainImageSequence";
        }
        else if(headerPartition.hasWaveAudioEssenceDescriptor()){
            result = "MainAudioSequence";
        }
        return result;
    }

    /**
     * An accessor for the PrimerPack KLV header
     *
     * @return a File containing the Primer pack
     * @throws IOException
     */
    KLVPacket.Header getPrimerPackHeader() throws IOException {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
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
     * @return a list of interchangeObjectBOs corresponding to the ID. Null if there is no InterchangeObjectBO with the ID
     * with the name passed in.
     */
    @Nullable
    List<InterchangeObject.InterchangeObjectBO> getStructuralMetadataByName(StructuralMetadataID structuralMetadataID) throws IOException {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        return this.getHeaderPartition(imfErrorLogger).getStructuralMetadata(structuralMetadataID);
    }

    /**
     * Returns the EditRate as a BigInteger
     * @return
     */
    BigInteger getEssenceEditRate() throws IOException {
        BigInteger result = BigInteger.valueOf(0);
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        if(!(this.getHeaderPartition(imfErrorLogger).getEssenceDescriptors().size() > 0)){
            throw new MXFException(String.format("No EssenceDescriptors were found in the MXF essence"));
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
     * @return
     */
    List<Long> getEssenceEditRateAsList() throws IOException {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        if(!(this.getHeaderPartition(imfErrorLogger).getEssenceDescriptors().size() > 0)){
            throw new MXFException(String.format("No EssenceDescriptors were found in the MXF essence"));
        }
        FileDescriptor.FileDescriptorBO fileDescriptorBO = (FileDescriptor.FileDescriptorBO) this.getHeaderPartition(imfErrorLogger).getEssenceDescriptors().get(0);
        return fileDescriptorBO.getSampleRate();
    }

    /**
     * Return the duration of the Essence including the source duration of all the Structural Components in the MXF Sequence
     * @return essenceDuration
     */
    BigInteger getEssenceDuration() throws IOException {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
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

    String getAudioEssenceLanguage() throws IOException {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        return this.getHeaderPartition(imfErrorLogger).getAudioEssenceSpokenLanguage();
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();
        try
        {
            sb.append(this.getRandomIndexPack());
            sb.append(this.getPartitionPacks());
            sb.append(this.getReferencedPartitionPacks(imfErrorLogger));
            sb.append(this.getIndexTableSegments());
            sb.append(this.getHeaderPartitionIMF(imfErrorLogger));
        }
        catch(IOException e)
        {
            throw new MXFException(e);
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
        File workingDirectory = new File(args[1]);

        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(inputFile);
        IMFTrackFileReader imfTrackFileReader = new IMFTrackFileReader(workingDirectory, resourceByteRangeProvider);
        IMFTrackFileCPLBuilder imfTrackFileCPLBuilder = new IMFTrackFileCPLBuilder(workingDirectory, inputFile);

        try {
            for (InterchangeObject.InterchangeObjectBO essenceDescriptor : imfTrackFileReader.getEssenceDescriptors()) {
                /* create dom */
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                Document document = docBuilder.newDocument();
                /*Output file containing the RegXML representation of the EssenceDescriptor*/
                KLVPacket.Header essenceDescriptorHeader = essenceDescriptor.getHeader();
                File outputFile = imfTrackFileCPLBuilder.getEssenceDescriptorAsXMLFile(document, essenceDescriptorHeader);
            }
        }
        catch(ParserConfigurationException | TransformerException e){
            throw new MXFException(e);
        }


        logger.info(String.format("%n %s", imfTrackFileReader.toString()));
    }
}
