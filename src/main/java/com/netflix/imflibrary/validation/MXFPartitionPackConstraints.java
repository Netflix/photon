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
 * Verifies the compliance of MXF partition packs (see st377-1:2011) with st2067-5:2013.
 */
public final class MXFPartitionPackConstraints {
    private static final String IMF_ESSENCE_EXCEPTION_PREFIX = "IMF Essence Component check: ";

    private MXFPartitionPackConstraints() {}

    public static List<ErrorLogger.ErrorObject> checkCompliance(PartitionPack partitionPack) throws IOException {
        IMFErrorLogger imfErrorLogger = new IMFErrorLoggerImpl();

        try {
            List<PartitionPack> partitionPacks = new ArrayList<>();
            partitionPacks.add(partitionPack);
            checkCompliance(partitionPacks, imfErrorLogger);
        } catch (IMFException e) {
            imfErrorLogger.addAllErrors(e.getErrors());
        }
        return imfErrorLogger.getErrors();
    }


    /**
     * Checks the compliance of partition packs found in an MXF file with st2067-5:2013. A runtime
     * exception is thrown in case of non-compliance
     *
     * @param partitionPacks the list of partition packs for which the compliance check is performed
     * @param imfErrorLogger - an object for logging errors
     */
    @SuppressWarnings({"PMD.CollapsibleIfStatements"})
    public static void checkCompliance(List<PartitionPack> partitionPacks, IMFErrorLogger imfErrorLogger)
    {
        int previousNumberOfErrors = imfErrorLogger.getErrors().size();

        //From st2067-5-2013 section 5.1.5, a partition shall only have one of header metadata, essence, index table
        for (PartitionPack partitionPack : partitionPacks)
        {
            if (partitionPack.hasHeaderMetadata())
            {
                if (partitionPack.hasEssenceContainer() || partitionPack.hasIndexTableSegments())
                {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_ESSENCE_EXCEPTION_PREFIX + String.format("PartitionPack at offset %d : header metadata = true, essenceContainerData = %s, indexTableSegment = %s, a partition shall have only one of header metadata, essence or index table.",
                            partitionPack.getPartitionByteOffset(), partitionPack.hasEssenceContainer(), partitionPack.hasIndexTableSegments()));
                }
            }
            else if (partitionPack.hasEssenceContainer())
            {
                if (partitionPack.hasHeaderMetadata() || partitionPack.hasIndexTableSegments())
                {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_ESSENCE_EXCEPTION_PREFIX + String.format("PartitionPack at offset %d : essenceContainerData = true, header metadata = %s, indexTableSegment = %s, a partition shall have only one of header metadata, essence or index table.",
                            partitionPack.getPartitionByteOffset(), partitionPack.hasHeaderMetadata(), partitionPack.hasIndexTableSegments()));
                }
            }
            else if (partitionPack.hasIndexTableSegments())
            {
                if (partitionPack.hasEssenceContainer() || partitionPack.hasHeaderMetadata())
                {
                    imfErrorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_ESSENCE_COMPONENT_ERROR, IMFErrorLogger.IMFErrors.ErrorLevels.FATAL, IMF_ESSENCE_EXCEPTION_PREFIX + String.format("PartitionPack at offset %d : indexTableSegment = true, essenceContainerData = %s, header metadata = %s, a partition shall have only one of header metadata, essence or index table.",
                            partitionPack.getPartitionByteOffset(), partitionPack.hasEssenceContainer(), partitionPack.hasHeaderMetadata()));
                }
            }
        }
        if(imfErrorLogger.hasFatalErrors(previousNumberOfErrors, imfErrorLogger.getNumberOfErrors())){
            throw new MXFException(String.format("Found fatal errors in the IMFTrackFile that violate the IMF Core constraints"), imfErrorLogger);
        }
    }
}
