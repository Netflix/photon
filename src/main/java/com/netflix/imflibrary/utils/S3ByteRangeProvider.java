package com.netflix.imflibrary.utils;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;

import java.io.*;


public class S3ByteRangeProvider implements ResourceByteRangeProvider {
    private static final AmazonS3 s3Client = new AmazonS3Client(DefaultAWSCredentialsProviderChain.getInstance());

    private static final int EOF = -1;
    private static final int BUFFER_SIZE = 1024;

    private AmazonS3URI s3Uri;
    private long fileSize;

    /**
     * Constructor for a S3ByteRangeProvider
     */
    public S3ByteRangeProvider(FileLocator fileLocator)
    {
        try {
            this.s3Uri = new AmazonS3URI(fileLocator.toURI());
            ObjectMetadata metadata = s3Client.getObjectMetadata(this.s3Uri.getBucket(), this.s3Uri.getKey());

            this.fileSize = metadata.getContentLength();
        } catch (IOException ex) {
            throw new Error(ex);
        }
    }

    /**
     * A method that returns the size in bytes of the underlying resource, in this case a File
     * @return the size in bytes of the underlying resource, in this case a File
     */
    public long getResourceSize()
    {
        return this.fileSize;
    }

    /**
     * A method to obtain bytes in the inclusive range [start, endOfFile] as a file
     *
     * @param rangeStart zero indexed inclusive start offset; ranges from 0 through (resourceSize -1) both included
     * @param workingDirectory the working directory where the output file is placed
     * @return file containing desired byte range from rangeStart through end of file
     * @throws IOException - any I/O related error will be exposed through an IOException
     */
    public File getByteRange(long rangeStart, File workingDirectory) throws IOException
    {
        return this.getByteRange(rangeStart, this.fileSize - 1, workingDirectory);
    }

    /**
     * A method to obtain bytes in the inclusive range [start, end] as a file
     *
     * @param rangeStart zero indexed inclusive start offset; range from [0, (resourceSize -1)] inclusive
     * @param rangeEnd zero indexed inclusive end offset; range from [0, (resourceSize -1)] inclusive
     * @param workingDirectory the working directory where the output file is placed
     * @return file containing desired byte range
     * @throws IOException - any I/O related error will be exposed through an IOException
     */
    public File getByteRange(long rangeStart, long rangeEnd, File workingDirectory) throws IOException
    {
        try (InputStream input = this.getByteRangeAsStream(rangeStart, rangeEnd)) {
            File rangeFile = new File(workingDirectory, "range");
            try (FileOutputStream fos = new FileOutputStream(rangeFile)) {
                IOUtils.copy(input, fos);
            }
            return rangeFile;
        }
    }

    /**
     * This method provides a way to obtain a byte range from the resource in-memory. A limitation of this method is
     * that the total size of the byte range request is capped at 0x7fffffff (the maximum value possible for type int
     * in java)
     *
     * @param rangeStart zero indexed inclusive start offset; ranges from 0 through (resourceSize -1) both included
     * @param rangeEnd zero indexed inclusive end offset; ranges from 0 through (resourceSize -1) both included
     * @return byte[] containing desired byte range
     * @throws IOException - any I/O related error will be exposed through an IOException
     */
    public byte[] getByteRangeAsBytes(long rangeStart, long rangeEnd) throws IOException
    {
        //validation of range request guarantees that 0 <= rangeStart <= rangeEnd <= (resourceSize - 1)
        ResourceByteRangeProvider.Utilities.validateRangeRequest(this.fileSize, rangeStart, rangeEnd);
        if((rangeEnd - rangeStart + 1) > Integer.MAX_VALUE){
            throw new IOException(String.format("Number of bytes requested = %d is greater than %d", (rangeEnd - rangeStart + 1), Integer.MAX_VALUE));
        }

        try (InputStream input = this.getByteRangeAsStream(rangeStart, rangeEnd)) {
            return IOUtils.toByteArray(input);
        }
    }

    public InputStream getByteRangeAsStream(long rangeStart, long rangeEnd) throws IOException {
        //validation of range request guarantees that 0 <= rangeStart <= rangeEnd <= (resourceSize - 1)
        ResourceByteRangeProvider.Utilities.validateRangeRequest(this.fileSize, rangeStart, rangeEnd);

        GetObjectRequest request = new GetObjectRequest(this.s3Uri.getBucket(), this.s3Uri.getKey());
        request.setRange(rangeStart, rangeEnd);

        S3Object obj = s3Client.getObject(request);

        return obj.getObjectContent();
    }
}