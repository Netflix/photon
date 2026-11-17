package com.netflix.imflibrary.validation;

import com.netflix.imflibrary.IMFErrorLogger;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * The Composition Playlist Marker Label scopes known to Photon, and the Marker Label values each of them defines.
 * <p>
 * The scope of a Marker Label determines which values are permitted (ST 2067-3, section 6.14.1.2). Each scope is
 * registered with the severity to use for a value that the scope does not list: constraints from a SMPTE standard are
 * normative, whereas a scope documenting industry best practice would only warrant a warning.
 * <p>
 * Currently only the scopes defined by SMPTE ST 2067-3:2020 (Table 21) are registered. Additional scopes can be added
 * through {@link #register} without any change to the validators using this class.
 */
public final class MarkerLabelRegistry {

    /** The scope of a Marker Label that has no scope attribute (default value in the ST 2067-3 schema). */
    public static final String DEFAULT_SCOPE = "http://www.smpte-ra.org/schemas/2067-3/2013#standard-markers";

    /**
     * A Marker Label scope and the values it defines.
     */
    public static final class Scope {
        private final String uri;
        private final Set<String> labels;
        private final IMFErrorLogger.IMFErrors.ErrorLevels unlistedLabelLevel;

        private Scope(String uri, Set<String> labels, IMFErrorLogger.IMFErrors.ErrorLevels unlistedLabelLevel) {
            this.uri = uri;
            this.labels = Set.copyOf(labels);
            this.unlistedLabelLevel = unlistedLabelLevel;
        }

        public String getUri() {
            return uri;
        }

        public boolean isPermitted(String label) {
            return labels.contains(label);
        }

        /**
         * @return the error level to report for a Marker Label value that is not defined by this scope
         */
        public IMFErrorLogger.IMFErrors.ErrorLevels getUnlistedLabelLevel() {
            return unlistedLabelLevel;
        }
    }

    private static final Map<String, Scope> SCOPES = new LinkedHashMap<>();

    static {
        // SMPTE ST 2067-3:2020, Table 21
        register("http://www.smpte-ra.org/schemas/2067-3/2013#standard-markers",
                IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                "FFBT", "FFCB", "FFCL", "FFDL", "FFEC", "FFHS", "FFMC", "FFOB",
                "FFOC", "FFOI", "FFSP", "FFTC", "FFTS", "FTXC", "FTXE", "FTXM",
                "LFBT", "LFCB", "LFCL", "LFDL", "LFEC", "LFHS", "LFMC", "LFOB",
                "LFOC", "LFOI", "LFSP", "LFTC", "LFTS", "LTXC", "LTXE", "LTXM",
                "FPCI", "FFCO", "LFCO", "FFOA", "LFOA");
        register("http://www.smpte-ra.org/schemas/2067-3/2016#standard-markers",
                IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                "FFDC", "LFDC");
        register("http://www.smpte-ra.org/schemas/2067-3/2020#standard-markers",
                IMFErrorLogger.IMFErrors.ErrorLevels.NON_FATAL,
                "FFEI", "FFER", "FFUN", "LFEI", "LFER", "LFUN");
    }

    private MarkerLabelRegistry() {
        // static registry
    }

    private static void register(String scopeUri, IMFErrorLogger.IMFErrors.ErrorLevels unlistedLabelLevel, String... labels) {
        SCOPES.put(scopeUri, new Scope(scopeUri, Set.of(labels), unlistedLabelLevel));
    }

    /**
     * @param scopeUri the scope attribute of a Marker Label, null for the default scope
     * @return the scope definition, or null if the scope is not known to Photon
     */
    @Nullable
    public static Scope getScope(@Nullable String scopeUri) {
        return SCOPES.get(scopeUri == null ? DEFAULT_SCOPE : scopeUri);
    }

    /**
     * @return the scopes known to Photon, by URI
     */
    @Nonnull
    public static Map<String, Scope> getScopes() {
        return Collections.unmodifiableMap(SCOPES);
    }
}
