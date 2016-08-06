package com.netflix.imflibrary.writerTools;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.RESTfulInterfaces.PayloadRecord;
import com.netflix.imflibrary.st2067_2.Composition;
import com.netflix.imflibrary.st2067_2.CompositionModels.CompositionModel_st2067_2_2016;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.UUIDHelper;
import com.netflix.imflibrary.writerTools.utils.IMFUUIDGenerator;
import com.netflix.imflibrary.writerTools.utils.IMFUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A stateless class that will create the AssetMap, Packing List and CompositionPlaylist that represent a complete IMF Master Package by utilizing the relevant builders
 */
public class IMPBuilder {

    /**
     * To prevent instantiation
     */
    private IMPBuilder(){

    }

    /*public static List<ErrorLogger.ErrorObject> buildIMP(List<Composition.VirtualTrack> virtualTracks, Map<UUID, IMFTrackFileMetadata> essencesHeaderPartitionMap, File workingDirectory){

    }*/

    /*public static List<ErrorLogger.ErrorObject> buildIMP_2016(List<Composition.VirtualTrack> virtualTracks, Composition.EditRate compositionEditRate, Map<UUID, IMFTrackFileMetadata> essencesHeaderPartitionMap, File workingDirectory){

    }*/

    /**
     * A thin class representing the EssenceMetadata required to construct a CPL document
     */
    public static class IMFTrackFileMetadata {
        byte[] headerPartition;
        byte[] hash;
        String hashAlgorithm;
        String originalFileName;

        /**
         * A constructor for the EssenceMetadata required to construct a CPL document
         * @param headerPartition a byte[] containing the EssenceHeaderPartition metadata of the IMFTrack file
         * @param hash a byte[] containing the SHA-1, Base64 encoded hash of the IMFTrack file
         * @param hashAlgorithm a string representing the Hash Algorithm used to generate the Hash of the IMFTrack file
         * @param originalFileName a string representing the FileName of the IMFTrack file
         */
        public IMFTrackFileMetadata(byte[] headerPartition, byte[] hash, String hashAlgorithm, String originalFileName){
            this.headerPartition = Arrays.copyOf(headerPartition, headerPartition.length);
            this.hash = Arrays.copyOf(hash, hash.length);
            this.hashAlgorithm = hashAlgorithm;
            this.originalFileName = originalFileName;
        }

        /**
         * Getter for the HeaderPartition metadata for the IMFTrack file corresponding to the IMFTrackFile metadata
         * @return a byte[] containing the HeaderParition metadata
         */
        public byte[] getHeaderPartition(){
            return Arrays.copyOf(this.headerPartition, this.headerPartition.length);
        }

        /**
         * Getter for the Hash of the IMFTrackFile
         * @return a byte[] containing the SHA-1 Base64 encoded hash of the IMFTrackFile
         */
        public byte[] getHash(){
            return Arrays.copyOf(this.hash, this.hash.length);
        }

        /**
         * Getter for the HashAlgorithm used to create the Hash of the IMFTrackFile
         * @return a string representing the Hash of the IMFTrackFile
         */
        public String getHashAlgorithm(){
            return this.hashAlgorithm;
        }

        /**
         * Getter for the original file name of the IMFTrackFile
         * @return a string representing the name of the IMFTrackFile
         */
        public String getOriginalFileName(){
            return this.originalFileName;
        }
    }
}
