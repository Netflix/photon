package com.netflix.imflibrary.utils;

import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.st0377.RandomIndexPack;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A tool that is capable of extracting partitions present within an IMFTrack file and
 * represent them in the form of a file with a specific extension, for e.g. ".hdr" for HeaderPartition.
 */
public class IMFTrackFilePartitionsExtractor {

    private static final Logger logger = LoggerFactory.getLogger(IMFTrackFilePartitionsExtractor.class);

    private static File extractHeaderPartition(Locator input, File workingDirectory) throws IOException {

        //Code to extract the HeaderPartition and write to a file        
        ResourceByteRangeProvider resourceByteRangeProvider = input.getResourceByteRangeProvider();
        Long archiveFileSize = resourceByteRangeProvider.getResourceSize();
        Long randomIndexPackSize;
        {//logic to provide as an input stream the portion of the archive that contains randomIndexPack size
            long rangeEnd = archiveFileSize - 1;
            long rangeStart = archiveFileSize - 4;
            
            byte[] bytes = input.readBytes(rangeStart, rangeEnd);
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
        if(!headerPartition.renameTo(new File(input.getName() + ".hdr"))){
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
        Locator[] locations = Locator.all(args, t -> {
            if (t < 2) {
                usage();
                System.exit(-1);
            }
        });
        Locator input = locations[0];
        if (!(locations[1] instanceof FileLocator)) {
            logger.error(usage());
            throw new IllegalArgumentException("Invalid parameters");
        }
        File workingDirectory = ((FileLocator)locations[1]).getFile();

        File fileWithHeaderPartition = extractHeaderPartition(input, workingDirectory);
        assert fileWithHeaderPartition.length() > 0;
    }
}
