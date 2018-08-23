package com.netflix.imflibrary.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

public class LocalFileLocator implements FileLocator {

    private File file;

    public LocalFileLocator(String location) {
        this.file = new File(location);
    }

    public LocalFileLocator(URI location) {
        this.file = new File(location);
    }

    public LocalFileLocator(File file) {
        this.file = file;
    }

    /**
     * Tests whether the file denoted by this abstract pathname is a
     * directory.
     *
     * @return <code>true</code> if and only if the file denoted by this
     * abstract pathname exists <em>and</em> is a directory;
     * <code>false</code> otherwise
     */
    public boolean isDirectory() throws IOException {
        return this.file.isDirectory();
    }

    public String getAbsolutePath() throws IOException {
        return this.file.getAbsolutePath();
    }

    public LocalFileLocator[] listFiles(FilenameFilter filenameFilter) throws IOException {
        File[] files = this.file.listFiles(filenameFilter);
        ArrayList<LocalFileLocator> fileLocators = new ArrayList<LocalFileLocator>();
        for (File file : files) {
            fileLocators.add(new LocalFileLocator(file));
        }

        return fileLocators.toArray(new LocalFileLocator[0]);
    }

    public URI toURI() throws IOException {
        return this.file.toURI();
    }

    public boolean exists() throws IOException {
        return this.file.exists();
    }

    public String getName() throws IOException {
        return this.file.getName();
    }

    public String getPath() throws IOException {
        return this.file.getPath();
    }

    public ResourceByteRangeProvider getResourceByteRangeProvider() {
        return new FileByteRangeProvider(this.file);
    }
}
