package com.netflix.imflibrary.st2067_2;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class CoreConstraints
{
    private CoreConstraints() {} // Prevent instantiation. This class is constants and utilities only

    // SMPTE ST 2067-2 version namespaces
    public static final String NAMESPACE_IMF_2013 = "http://www.smpte-ra.org/schemas/2067-2/2013";
    public static final String NAMESPACE_IMF_2016 = "http://www.smpte-ra.org/schemas/2067-2/2016";
    public static final String NAMESPACE_IMF_2020 = "http://www.smpte-ra.org/ns/2067-2/2020";

    static final List<String> SUPPORTED_NAMESPACES = Collections.unmodifiableList(Arrays.asList(
            NAMESPACE_IMF_2013, NAMESPACE_IMF_2016, NAMESPACE_IMF_2020));

    /**
     * @deprecated Remove once all deprecated, package-based, 'getCoreConstraintsVersion' methods are removed.
     */
    @Deprecated
    static String packageFromSchema(String coreConstraintsSchema)
    {
        if (coreConstraintsSchema.equals(NAMESPACE_IMF_2013))
            return "org.smpte_ra.schemas._2067_3._2013";
        else if (coreConstraintsSchema.equals(NAMESPACE_IMF_2016))
            return "org.smpte_ra.schemas.st2067_2_2016";
        else if (coreConstraintsSchema.equals(NAMESPACE_IMF_2020))
            return "org.smpte_ra.schemas.st2067_2_2020";
        else
            return coreConstraintsSchema; // No mapping, just return the schema value
    }

    // Determine the highest Core Constraints version based on the ApplicationIds used
    @Nullable public static String fromApplicationId(@Nonnull Collection<String> applicationIds)
    {
        // NOTE- When adding new namespaces or core constraint versions, be sure that the most recent core constraints
        // are checked first. That way if there are multiple ApplicationIdentifications, the newest version is returned.
        if (applicationIds.contains(Application2ExtendedComposition.SCHEMA_URI_APP2E_2020) || (applicationIds.contains(Application2E2021.APP_IDENTIFICATION)))
        {
            return CoreConstraints.NAMESPACE_IMF_2020;
        }
        else if (applicationIds.contains(Application5Composition.SCHEMA_URI_APP5_2017)
                || applicationIds.contains(Application2ExtendedComposition.SCHEMA_URI_APP2E_2016)
                || applicationIds.contains(Application2Composition.SCHEMA_URI_APP2_2016))
        {
            return CoreConstraints.NAMESPACE_IMF_2016;
        }
        else if (applicationIds.contains(Application2ExtendedComposition.SCHEMA_URI_APP2E_2014)
                || applicationIds.contains(Application2Composition.SCHEMA_URI_APP2_2013))
        {
            return CoreConstraints.NAMESPACE_IMF_2013;
        }
        else
        {
            return null;
        }
    }

    // Determine the most recent core constraints version, based on a collection of element namespaces
    static String fromElementNamespaces(@Nonnull Collection<String> namespaces)
    {
        // NOTE- When adding new namespaces or core constraint versions, be sure that the most recent core constraints
        // are checked first. That way if there are multiple different namespaces, the newest version is returned.
        if (namespaces.contains(CoreConstraints.NAMESPACE_IMF_2020))
        {
            return CoreConstraints.NAMESPACE_IMF_2020;
        }
        else if (namespaces.contains(CoreConstraints.NAMESPACE_IMF_2016))
        {
            return CoreConstraints.NAMESPACE_IMF_2016;
        }
        else if (namespaces.contains(CoreConstraints.NAMESPACE_IMF_2013))
        {
            return CoreConstraints.NAMESPACE_IMF_2013;
        }
        else
        {
            // TODO- Consider identify core constraints based on other namespaces
            // Example- IABSequence "http://www.smpte-ra.org/ns/2067-201/2019" requires core constraints ST 2067-2:2016
            return null;
        }
    }
}
