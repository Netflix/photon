package com.netflix.imflibrary.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by schakrovorthy on 3/7/16.
 */
public final class NonClosingInputStream extends BufferedInputStream {

    public NonClosingInputStream(InputStream inputStream) {
        super(inputStream);
        super.mark(Integer.MAX_VALUE);
    }

    @Override
    public synchronized void mark(int readLimit) {
        super.mark(Integer.MAX_VALUE);
    }

    @Override
    public synchronized void reset() throws IOException {
        super.reset();
    }

    @Override
    public void close() throws IOException {
        // Do nothing.
    }

    public void reallyClose() throws IOException {
        // Actually close.
        in.close();
    }
}
