package com.netflix.imflibrary.writerTools;

import com.netflix.imflibrary.st429_7.DCPCompositionPlaylist;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Created by schakrovorthy on 1/14/17.
 */
public class DCPCompositionPlaylistBuilder {


    public static File build(String annotationText,
                            String issuer,
                            UUID cplUUID,
                            List<File> trackFiles,
                            File workingDirectory) {
        File cplFile = new File(workingDirectory.toString() + "/" + "DCP-CPL-" + cplUUID);
        return cplFile;

    }
}
