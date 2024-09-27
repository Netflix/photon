package com.netflix.imflibrary.writerTools.utils;

import com.netflix.imflibrary.st2067_2.IMFTrackFileResourceType;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SequenceDurationValidator {

    /**
     * Validates and adjusts the duration of the resources in the sequence to ensure the total duration
     * is an integer number of Composition Edit Units.
     *
     * @param resources the list of resources in the sequence
     * @param compositionEditRateNumerator of the composition edit rate
     * @param compositionEditRateDenominator of the composition edit rate
     * @return a new list of resources with adjusted durations if needed
     */
    public static List<IMFTrackFileResourceType> validateAndAdjustResourceDurations(List<IMFTrackFileResourceType> resources, long compositionEditRateNumerator, long compositionEditRateDenominator) {
        long totalResourceDuration = resources.stream()
                .mapToLong(resource -> {
                    if (resource != null) {
                        return resource.getSourceDuration().longValue();
                    }
                    return 0;
                })
                .sum();

        List<IMFTrackFileResourceType> adjustedResources = new ArrayList<>(resources);
        IMFTrackFileResourceType lastResource = adjustedResources.get(adjustedResources.size() - 1);
        long resourceEditRateNumerator = lastResource.getEditRate().getNumerator();
        long resourceEditRateDenominator = lastResource.getEditRate().getDenominator();
        long remainder = 0;

        if (resourceEditRateDenominator == 1001) {
            // Non-integer frame rates must be evenly divisible by 5 in order to align with audio samples
            remainder = totalResourceDuration % 5;
        } else if (compositionEditRateDenominator == 1001 && resourceEditRateNumerator == 48000) {
            // Audio samples must align with composition edit units (video frames)
            remainder = ((totalResourceDuration * 30000 * resourceEditRateDenominator) % (compositionEditRateDenominator * resourceEditRateNumerator )) / 30000;
        } else {
            remainder = ((totalResourceDuration * compositionEditRateNumerator * resourceEditRateDenominator) % (compositionEditRateDenominator * resourceEditRateNumerator )) / compositionEditRateNumerator;
        }

        if (remainder != 0) {
            // Adjust the source duration of the last resource to ensure the duration is an integer number of Composition Edit Units

            List<Long> editRate = Arrays.asList(lastResource.getEditRate().getNumerator(), lastResource.getEditRate().getDenominator());
            adjustedResources.set(adjustedResources.size() - 1, new IMFTrackFileResourceType(
                    lastResource.getId(),
                    lastResource.getTrackFileId(),
                    editRate,
                    lastResource.getIntrinsicDuration(),
                    lastResource.getEntryPoint(),
                    BigInteger.valueOf(lastResource.getSourceDuration().longValue() - remainder),
                    lastResource.getRepeatCount(),
                    lastResource.getSourceEncoding(),
                    lastResource.getHash(),
                    lastResource.getHashAlgorithm()
            ));
        }

        return adjustedResources;
    }
}