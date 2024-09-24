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

    private final J2KExtendedCapabilitiesB0 capabilitiesB0;

    public J2KExtendedCapabilities(J2KExtendedCapabilitiesB0 capabilitiesB0) {
        this.capabilitiesB0 = capabilitiesB0;
    }

    /**
     * Object corresponding to a parsed J2KExtendedCapabilities as defined in ISO/IEC 15444-15 Annex A.5.2
     */
    @Immutable
    public static final class J2KExtendedCapabilitiesB0 {
        @MXFProperty(size=4) protected final Integer pCap = null;
        @MXFProperty(size=0, depends=true) protected final CompoundDataTypes.MXFCollections.MXFCollection<Short> cCapi = null;

        /**
         * Instantiates a new parsed J2KExtendedCapabilities object
         *
         * @param bytes the byte array corresponding to the 4 fields
         * @throws IOException - any I/O related error will be exposed through an IOException
         */

        public J2KExtendedCapabilitiesB0(byte[] bytes) throws IOException
        {
            ByteProvider byteProvider = new ByteArrayDataProvider(bytes);
            MXFPropertyPopulator.populateField(byteProvider, this, "pCap");
            MXFPropertyPopulator.populateField(byteProvider, this, "cCapi");
        }

    }
}
