/*
 *
 * Copyright 2026 Netflix, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.netflix.imflibrary.validation;

import com.netflix.imflibrary.*;
import com.netflix.imflibrary.exceptions.IMFException;
import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st0377.PartitionPack;
import com.netflix.imflibrary.st0377.header.*;
import com.netflix.imflibrary.utils.ErrorLogger;
import com.netflix.imflibrary.utils.RFC5646SpokenLanguage;
import com.netflix.imflibrary.utils.Utilities;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * ST 2067-2 core constraints for Timed Text Track Files.
 * Applied through the (edition specific) core constraints validators, never implicitly.
 */
public final class IMFCoreConstraintsText {
    private static final String IMF_ESSENCE_EXCEPTION_PREFIX = "IMF Essence Component check: ";
    public static final String IMSC1TextProfileDesignator = "http://www.w3.org/ns/ttml/profile/imsc1/text";
    public static final String IMSC1ImageProfileDesignator = "http://www.w3.org/ns/ttml/profile/imsc1/image";
    // Timed Text profile designators per SMPTE ST 2067-2:2020, section 5.4.2
    private static final String[] IMSC1TextProfileDesignators2020 = {IMSC1TextProfileDesignator, "http://www.w3.org/ns/ttml/profile/imsc1.1/text", "urn:ebu:tt:distribution:2014-01", "urn:ebu:tt:distribution:2018-04", "http://www.w3.org/ns/ttml/profile/sdp-us", "https://www.netflix.com/ns/imsc1.1/text/1"};
    private static final String[] IMSC1ImageProfileDesignators2020 = {IMSC1ImageProfileDesignator, "http://www.w3.org/ns/ttml/profile/imsc1.1/image"};
    public static final String IMSC1ImageResourceMimeMediaType = "image/png";
    public static final String IMSC1FontResourceMimeMediaType = "application/x-font-opentype";

    private IMFCoreConstraintsText() {}

    public static void checkTimedTextTrackFile(@Nonnull TimedTextDescriptor timedTextDescriptor, @Nonnull UUID packageID, @Nonnull IMFErrorLogger imfErrorLogger) {

        //Section 6.8 st2067-2:2016 and  st0 429-5 section 7
        if (timedTextDescriptor.getEssenceContainerUL().getULAsBytes()[13] != 0x13) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ESSENCE_EXCEPTION_PREFIX + String.format("Invalid Mapping Kind in TimedText essence container UL within IMFTrackFile represented by ID %s.",
                    packageID.toString()));
        }

        //https://www.w3.org/TR/ttml-imsc1/ Section 6.1
        if (!timedTextDescriptor.getUCSEncoding().equalsIgnoreCase("UTF-8")) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ESSENCE_EXCEPTION_PREFIX + String.format("Invalid UCSEncoding(%s) in TimedTextDescriptor within trackFile represented by ID %s. Only UTF-8 is valid UCSEncoding. ",
                    timedTextDescriptor
                    .getUCSEncoding(),
                    packageID.toString()));
        }
        //https://www.w3.org/TR/ttml-imsc1/ Section 6.3
        if (!(Arrays.asList(IMSC1TextProfileDesignators2020).contains(timedTextDescriptor.getNamespaceURI()) || Arrays.asList(IMSC1ImageProfileDesignators2020).contains(timedTextDescriptor.getNamespaceURI()))) {
            imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ESSENCE_EXCEPTION_PREFIX + String.format("Invalid NamespaceURI(%s) in TimedTextDescriptor within trackFile represented by ID %s. Valid NamespaceURIs: " +
                    "{%s}, {%s}",
                    timedTextDescriptor.getNamespaceURI(), packageID.toString(), Arrays.toString(IMSC1TextProfileDesignators2020), Arrays.toString(IMSC1ImageProfileDesignators2020)));
        }
        for(TimeTextResourceSubDescriptor textResourceSubDescriptor : timedTextDescriptor.getSubDescriptorList()) {
            //Section 5.4.5 and 5.4.6 st2067-2:2016
            if (!textResourceSubDescriptor.getMimeMediaType().equals(IMSC1ImageResourceMimeMediaType) && !textResourceSubDescriptor.getMimeMediaType().equals
                    (IMSC1FontResourceMimeMediaType)) {
                imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CORE_CONSTRAINTS_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL, IMF_ESSENCE_EXCEPTION_PREFIX + String.format("Invalid MIMEMediaType(%s) in TimedTextResourceSubDescriptor within trackFile represented by ID %s. Valid " +
                        "MIMEMediaTypes: {%s} {%s}",
                        timedTextDescriptor.getNamespaceURI(), packageID.toString(), IMSC1ImageResourceMimeMediaType, IMSC1FontResourceMimeMediaType));
            }
        }
    }
}
