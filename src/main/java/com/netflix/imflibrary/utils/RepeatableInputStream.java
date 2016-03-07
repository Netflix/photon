package com.netflix.imflibrary.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A class that provides a repeatable input stream functionality inheriting this behavior from its parent the BufferedInputStream.
 * This class provides a specialized behavior that clients receiving this object's instances cannot close the underlying input
 * stream by invoking a close().
 *
 */
public final class RepeatableInputStream extends BufferedInputStream {

    /**
     * A constructor to create RepeatableInputStream objects
     * @param inputStream that needs to be wrapped in a RepeatableInputStream object
     */
    public RepeatableInputStream(InputStream inputStream) {
        super(inputStream);
        super.mark(Integer.MAX_VALUE);
    }

    /**
     * Overridden method mark(int)
     * @param readLimit the maximum limit of bytes that can be read before
     *                      the mark position becomes invalid.
     *
     */
    @Override
    public synchronized void mark(int readLimit) {
        super.mark(Integer.MAX_VALUE);
    }

    /**
     * Overridden method reset() to reposition the inputstream to the last marked position.
     *
     */
    @Override
    public synchronized void reset() throws IOException {
        super.reset();
    }

    /**
     * Overridden method close() to close the input stream. This function does nothing
     * and that is by design in order to prevent methods receiving this object
     * instance to not be able to close the underlying input stream.
     *
     */
    @Override
    public void close() throws IOException {
        // Do nothing.
    }

    /**
     * Close this input stream explicitly.
     * @throws IOException - any I/O related error is exposed through an IOException
     */
    public void forceClose() throws IOException {
        // Actually close.
        in.close();
    }
}
