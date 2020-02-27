package com.netflix.imflibrary.st0377.header;

import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.KLVPacket;
import com.netflix.imflibrary.MXFUID;
import com.netflix.imflibrary.annotations.MXFProperty;
import com.netflix.imflibrary.utils.ByteProvider;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.util.Map;

public class TextBasedDMFramework extends DMFramework {

    private static final String ERROR_DESCRIPTION_PREFIX = "MXF Header Partition: " + TextBasedObject.class.getSimpleName() + " : ";
    private final TextBasedDMFrameworkBO textBasedDMFrameworkBO;
    private final TextBasedObject textBaseObject;

    public TextBasedDMFramework(TextBasedDMFrameworkBO textBasedDMFrameworkBO, TextBasedObject textBasedObject) {
        super(textBasedDMFrameworkBO);
        this.textBasedDMFrameworkBO = textBasedDMFrameworkBO;
        this.textBaseObject = textBasedObject;
    }

    public TextBasedObject getTextBaseObject() {
        return textBaseObject;
    }

    /**
     * Object corresponding to a parsed TextBasedDMFramework structural metadata set defined in RP 2057:2011
     */
    @Immutable
    @SuppressWarnings({"PMD.FinalFieldCouldBeStatic"})
    public static class TextBasedDMFrameworkBO extends DMFrameworkBO {

        @MXFProperty(size=16, depends=true) private final StrongRef text_based_object = null;
        private final IMFErrorLogger imfErrorLogger;

        /**
         * Instantiates a new parsed GenericStreamTextBasedSetBO object by virtue of parsing the MXF file bitstream
         *
         * @param header the parsed header (K and L fields from the KLV packet)
         * @param byteProvider the input stream corresponding to the MXF file
         * @param localTagToUIDMap mapping from local tag to element UID as provided by the Primer Pack defined in st377-1:2011
         * @param imfErrorLogger logger for recording any parsing errors
         * @throws IOException - any I/O related error will be exposed through an IOException
         */
        public TextBasedDMFrameworkBO(KLVPacket.Header header, ByteProvider byteProvider, Map<Integer, MXFUID> localTagToUIDMap, IMFErrorLogger imfErrorLogger) throws IOException {
            super(header);
            this.imfErrorLogger = imfErrorLogger;

            long numBytesToRead = this.header.getVSize();

            StructuralMetadata.populate(this, byteProvider, numBytesToRead, localTagToUIDMap);

        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("================== TextBasedDMFramework ======================\n");
            sb.append(this.header.toString());
            if (this.text_based_object != null) {
                sb.append(String.format("description = %s%n", this.text_based_object));
            }
            return sb.toString();
        }
    }
}
