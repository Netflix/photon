package com.netflix.imflibrary.utils;

import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.st0377.RandomIndexPack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * A tool that is capable of extracting partitions present within an IMFTrack file and
 * represent them in the form of a file with a specific extension, for e.g. ".hdr" for HeaderPartition.
 */
public class IMFTrackFilePartitionsExtractor {

    private static final Logger logger = LogManager.getLogger(IMFTrackFilePartitionsExtractor.class);

    private static File extractHeaderPartition(File input, File workingDirectory) throws IOException {

        //Code to extract the HeaderPartition and write to a file
        ResourceByteRangeProvider resourceByteRangeProvider = new FileByteRangeProvider(input);
        Long archiveFileSize = resourceByteRangeProvider.getResourceSize();
        Long randomIndexPackSize;
        {//logic to provide as an input stream the portion of the archive that contains randomIndexPack size
            long rangeEnd = archiveFileSize - 1;
            long rangeStart = archiveFileSize - 4;

            File fileWithRandomIndexPackSize = resourceByteRangeProvider.getByteRange(rangeStart, rangeEnd, workingDirectory);
            byte[] bytes = Files.readAllBytes(Paths.get(fileWithRandomIndexPackSize.toURI()));
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

        File fileWithRandomIndexPack = resourceByteRangeProvider.getByteRange(rangeStart, rangeEnd, workingDirectory);
        ByteProvider byteProvider = new FileDataProvider(fileWithRandomIndexPack);
        randomIndexPack = new RandomIndexPack(byteProvider, rangeStart, randomIndexPackSize);
        List<Long> partitionByteOffsets = randomIndexPack.getAllPartitionByteOffsets();

        File headerPartition = resourceByteRangeProvider.getByteRange(partitionByteOffsets.get(0), partitionByteOffsets.get(1) - 1, workingDirectory);
        String inputPath = input.getAbsolutePath();
        if(!headerPartition.renameTo(new File(inputPath + ".hdr"))){
            logger.info(String.format("Couldn't rename the file containing the header partition"));
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

        File input = new File(args[0]);
        File workingDirectory = new File(args[1]);

        File fileWithHeaderPartition = extractHeaderPartition(input, workingDirectory);
        assert fileWithHeaderPartition.length() > 0;
    }
}
