package com.netflix.imflibrary.utils;

import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.st0377.RandomIndexPack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * A tool that is capable of extracting partitions present within an IMFTrack file and
 * represent them in the form of a file with a specific extension, for e.g. ".hdr" for HeaderPartition.
 */
public class IMFTrackFilePartitionsExtractor {

    private static final Logger logger = LoggerFactory.getLogger(IMFTrackFilePartitionsExtractor.class);

    private static Path extractHeaderPartition(Path input, Path workingDirectory) throws IOException {

        //Code to extract the HeaderPartition and write to a file
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(input);
        long archiveFileSize = resourceByteRangeProvider.getResourceSize();
        long randomIndexPackSize;
        {//logic to provide as an input stream the portion of the archive that contains randomIndexPack size
            long rangeEnd = archiveFileSize - 1;
            long rangeStart = archiveFileSize - 4;

            Path pathWithRandomIndexPackSize = resourceByteRangeProvider.getByteRange(rangeStart, rangeEnd, workingDirectory);
            byte[] bytes = Files.readAllBytes(pathWithRandomIndexPackSize);
            randomIndexPackSize = (long)(ByteBuffer.wrap(bytes).getInt());
        }

        RandomIndexPack randomIndexPack;
        //logic to provide as an input stream the portion of the archive that contains randomIndexPack
        long rangeEnd = archiveFileSize - 1;
        long rangeStart = archiveFileSize - randomIndexPackSize;
        if (rangeStart < 0) {
            throw new MXFException(String.format("randomIndexPackSize = %d obtained from last 4 bytes of the MXF file is larger than archiveFile size = %d, implying that this file does not contain a RandomIndexPack",
                    randomIndexPackSize, archiveFileSize));
        }

        Path pathWithRandomIndexPack = resourceByteRangeProvider.getByteRange(rangeStart, rangeEnd, workingDirectory);
        ByteProvider byteProvider = new FileDataProvider(pathWithRandomIndexPack);
        randomIndexPack = new RandomIndexPack(byteProvider, rangeStart, randomIndexPackSize);
        List<Long> partitionByteOffsets = randomIndexPack.getAllPartitionByteOffsets();

        Path headerPartition = resourceByteRangeProvider.getByteRange(partitionByteOffsets.get(0), partitionByteOffsets.get(1) - 1, workingDirectory);
        String inputPath = input.toString();

        try {
            Files.move(headerPartition, Utilities.getPathFromString(inputPath  + ".hdr"));
        } catch (Exception e) {
            logger.info(String.format("Couldn't rename the path containing the header partition"));
        }

        return headerPartition;
    }

    private static String usage()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Usage:%n"));
        sb.append(String.format("%s <inputFilePath> <workingDirectory>%n", IMFTrackFilePartitionsExtractor.class.getName()));
        return sb.toString();
    }

    public static void main(String[] args)throws IOException {

        if(args.length < 2){
            usage();
            System.exit(-1);
        }

        Path input = Utilities.getPathFromString(args[0]);
        Path workingDirectory = Utilities.getPathFromString(args[1]);

        Path fileWithHeaderPartition = extractHeaderPartition(input, workingDirectory);
        assert Files.size(fileWithHeaderPartition) > 0;
    }
}
