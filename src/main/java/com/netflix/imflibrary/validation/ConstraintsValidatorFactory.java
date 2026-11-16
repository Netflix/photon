package com.netflix.imflibrary.validation;

import com.netflix.imflibrary.IMFErrorLogger;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Factory class that manages instantiation of concrete validator classes .
 */
public class ConstraintsValidatorFactory {

    private static final Map<String, Supplier<ConstraintsValidator>> registry = new HashMap<>();

    static {

        /*
         *  To add support for new ConstraintsValidator classes, add a line below to register a new
         *  Application/Plugin namespace URI with the new ConstraintsValidator class.
         */

        // Core Constraints
        registerValidator("http://www.smpte-ra.org/schemas/2067-2/2013", IMFCoreConstraints2013Validator::new);
        registerValidator("http://www.smpte-ra.org/schemas/2067-2/2016", IMFCoreConstraints2016Validator::new);
        registerValidator("http://www.smpte-ra.org/ns/2067-2/2020", IMFCoreConstraints2020Validator::new);

        // CPL
        registerValidator("http://www.smpte-ra.org/schemas/2067-3/2013", IMFCPL2013Validator::new);
        registerValidator("http://www.smpte-ra.org/schemas/2067-3/2016", IMFCPL2016Validator::new);

        // IMF App #2E
        registerValidator(IMFApp2E5EDConstraintsValidator.applicationIdentification, IMFApp2E5EDConstraintsValidator::new);
        registerValidator("http://www.smpte-ra.org/ns/2067-21/2021", IMFApp2E2021ConstraintsValidator::new);
        registerValidator("http://www.smpte-ra.org/ns/2067-21/2020", IMFApp2E2020ConstraintsValidator::new);
        registerValidator("http://www.smpte-ra.org/schemas/2067-21/2016", IMFApp2E2016ConstraintsValidator::new);
        registerValidator("http://www.smpte-ra.org/schemas/2067-21/2014", IMFApp2E2014ConstraintsValidator::new);

        // IMF App #5
        registerValidator("http://www.smpte-ra.org/ns/2067-50/2017", IMFApp5ConstraintsValidator::new);

        // Plugins
        registerValidator("http://www.smpte-ra.org/ns/2067-201/2019", IMFIABLevel0PluginConstraintsValidator::new);
        registerValidator("http://www.smpte-ra.org/ns/2067-202/2022", IMFISXDPluginConstraintsValidator::new);
        registerValidator("http://www.smpte-ra.org/ns/2067-203/2022", IMFMGASADMPluginConstraintsValidator::new);
        registerValidator("http://www.smpte-ra.org/ns/2067-204/2022", IMFADMAudioPluginConstraintsValidator::new);
    }

    private static void registerValidator(String type, Supplier<ConstraintsValidator> constructor) {
        registry.put(type.toLowerCase(), constructor);
    }


    /**
     * A static factory method that instantiates and returns the validator object associated with the provided namespace
     * @param namespaceURI the namespace URI for which a validator object is requested. Such namespace URI typically
     *                     represents one of the following:
     *                     - a CPL schema namespace URI
     *                     - the XML namespace for a CPL sequence
     *                     - an ApplicationID
     * @return an object that implements the ConstraintsValidator interface, or null if no matching class is registered.
     */
    public static ConstraintsValidator getValidator(String namespaceURI) {
        Supplier<ConstraintsValidator> constructor = registry.get(namespaceURI.toLowerCase());
        if (constructor != null) {
            return constructor.get();
        }

        return null;
    }


    /**
     * Resolves all validators that apply in the given context: the validator registered for the sequence namespace
     * first, followed by the validators registered for each of the application identifications. Namespaces
     * without a registered validator are skipped; each validator class is returned at most once.
     * <p>
     * A sequence namespace without a registered validator (e.g. one from a newer edition of the specification) is
     * reported as a WARNING to the error logger, since the validations that validator would have provided (essence kind
     * specific checks) are not run. Unregistered application identifications are not reported, those may legitimately
     * have no validator.
     *
     * @param context the context in which the Track File is used by a Composition
     * @param errorLogger receives the warning
     */
    public static List<ConstraintsValidator> getValidators(@Nonnull EssenceValidationContext context, @Nonnull IMFErrorLogger errorLogger) {
        List<ConstraintsValidator> validators = new ArrayList<>();
        Set<Class<?>> seen = new HashSet<>();
        String sequenceNamespace = context.getSequenceNamespace();
        List<String> keys = new ArrayList<>();
        if (sequenceNamespace != null && !sequenceNamespace.isEmpty()) {
            keys.add(sequenceNamespace);
        }
        keys.addAll(context.getApplicationIdentifications());
        for (String key : keys) {
            ConstraintsValidator validator = getValidator(key);
            if (validator == null) {
                if (key.equals(sequenceNamespace)) {
                    errorLogger.addError(IMFErrorLogger.IMFErrors.ErrorCodes.IMF_CPL_ERROR,
                            IMFErrorLogger.IMFErrors.ErrorLevels.WARNING,
                            "Namespace not supported: " + key + ", essence kind specific checks were not run");
                }
            } else if (seen.add(validator.getClass())) {
                validators.add(validator);
            }
        }
        return validators;
    }
}
