package com.netflix.imflibrary.validation;


import com.netflix.imflibrary.IMFErrorLogger;
import com.netflix.imflibrary.IMFErrorLoggerImpl;
import com.netflix.imflibrary.RESTfulInterfaces.PayloadRecord;
import com.netflix.imflibrary.app.IMFTrackFileReader;
import com.netflix.imflibrary.exceptions.MXFException;
import com.netflix.imflibrary.st0377.HeaderPartition;
import com.netflix.imflibrary.st0377.header.GenericPackage;
import com.netflix.imflibrary.st0377.header.Preface;
import com.netflix.imflibrary.st0377.header.SourcePackage;
import com.netflix.imflibrary.st2067_2.Composition;
import com.netflix.imflibrary.st2067_2.IMFCompositionPlaylist;
import com.netflix.imflibrary.st2067_2.IMFTrackFileResourceType;
import com.netflix.imflibrary.utils.*;
import jakarta.annotation.Nonnull;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


abstract public class IMFCPLValidator implements ConstraintsValidator {

    @Override
    public List<ErrorLogger.ErrorObject> validateEssencePartitionConstraints(@Nonnull PayloadRecord headerPartition, @Nonnull List<PayloadRecord> indexPartitionPayloads) {
        return List.of();
    }




}
