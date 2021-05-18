/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.imflibrary.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.Objects;

/**
 * Locator Proxy for a {@link File} object.
 */
public class FileLocator implements Locator {

    private final File file;

    /**
     * Instantiate a new FileLocator object from a file location.
     *
     * @param location      the file location
     * @param configuration Not used.
     */
    public FileLocator(String location, Configuration configuration) {
        this.file = new File(location);
    }

    /**
     * Instantiate a new FileLocator object from a file URI.
     *
     * @param uri           the location URI.
     * @param configuration Not used.
     */
    public FileLocator(URI uri, Configuration configuration) {
        this.file = new File(uri);
    }

    /**
     * Instantiate a new FileLocator object from a file object.
     *
     * @param file the file object that is being proxied.
     */
    public FileLocator(File file) {
        this.file = file;
    }

    /**
     * Returns the underlying file object.
     *
     * @return The underlying file object.
     */
    public File getFile() {
        return file;
    }

    @Override
    public Configuration getConfiguration() {
        return null;
    }

    @Override
    public boolean exists() {
        return file.exists();
    }

    @Override
    public ResourceByteRangeProvider getResourceByteRangeProvider() {
        return new FileByteRangeProvider(file);
    }

    @Override
    public String getAbsolutePath() {
        return file.getAbsolutePath();
    }

    @Override
    public boolean isDirectory() {
        return file.isDirectory();
    }

    @Override
    public Locator[] listFiles(Filter filter) {
        String fileList[] = file.list();
        if (fileList == null) {
            return null;
        }
        LinkedList<Locator> files = new LinkedList<>();
        for (String f : fileList) {
            final Locator fl = this.getChild(f);
            if ((filter == null) || filter.accept(fl)) {
                files.add(fl);
            }
        }
        return files.toArray(new Locator[files.size()]);
    }

    @Override
    public URI toURI() {
        return file.toURI();
    }

    @Override
    public Locator getParent() {
        File parentFile = this.file.getParentFile();
        if (parentFile != null) {
            return new FileLocator(parentFile);
        }
        return null;
    }

    @Override
    public Locator getChild(String child) {
        return new FileLocator(new File(this.file, child));
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public byte[] readAllBytes() throws IOException {
        return Files.readAllBytes(file.toPath());
    }

    @Override
    public byte[] readBytes(long start, long end) throws IOException {
        try (SeekableByteChannel sbc = Files.newByteChannel(file.toPath());
             InputStream in = Channels.newInputStream(sbc)) {
            ResourceByteRangeProvider.Utilities.validateRangeRequest(file.length(), start, end);
            sbc.position(start);
            long size = (end - start + 1);
            if (size > (long) (Integer.MAX_VALUE - 8)) {
                throw new OutOfMemoryError("Required array size too large");
            }
            return Locator.toByteArray((int) size, in);
        }
    }

    @Override
    public long length() {
        return file.length();
    }

    @Override
    public boolean mkdir() {
        return file.mkdir();
    }

    @Override
    public String getPath() {
        return file.getPath();
    }

    @Override
    public String toString() {
        return file.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.file);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FileLocator other = (FileLocator) obj;
        return Objects.equals(this.file, other.file);
    }
}
