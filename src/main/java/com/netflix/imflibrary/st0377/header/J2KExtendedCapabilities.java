package com.netflix.imflibrary.st0377.header;

import com.netflix.imflibrary.MXFPropertyPopulator;
import com.netflix.imflibrary.annotations.MXFProperty;
import com.netflix.imflibrary.st0377.CompoundDataTypes;
import com.netflix.imflibrary.utils.ByteArrayDataProvider;
import com.netflix.imflibrary.utils.ByteProvider;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.util.List;

/**
 * Object model corresponding to J2KExtendedCapabilities as defined in ISO/IEC 15444-1:2019 Annex A.5.2
 */

@Immutable
public final class J2KExtendedCapabilities {

    @MXFProperty(size=4) protected final Integer pCap = null;
    @MXFProperty(size=0, depends=true) protected final CompoundDataTypes.MXFCollections.MXFCollection<Short> cCap = null;

    public Integer getpCap() {
        return pCap;
    }

    public CompoundDataTypes.MXFCollections.MXFCollection<Short> getcCap() {
        return cCap;
    }

    /**
     * Instantiates a new parsed J2KExtendedCapabilities object
     *
     * @param byteProvider the bytes corresponding to the 2 fields
     * @throws IOException - any I/O related error will be exposed through an IOException
     */

    public J2KExtendedCapabilities(ByteProvider byteProvider) throws IOException {
        MXFPropertyPopulator.populateField(byteProvider, this, "pCap");
        MXFPropertyPopulator.populateField(byteProvider, this, "cCap");
    }
}
