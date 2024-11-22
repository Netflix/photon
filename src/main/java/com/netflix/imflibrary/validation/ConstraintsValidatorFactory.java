package com.netflix.imflibrary.validation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ConstraintsValidatorFactory {

    private static final Map<String, Supplier<ConstraintsValidator>> registry = new HashMap<>();

    static {
        // CPL
        registerValidator("http://www.smpte-ra.org/schemas/2067-3/2013", IMFCPL2013Validator::new);
        registerValidator("http://www.smpte-ra.org/schemas/2067-3/2016", IMFCPL2016Validator::new);

        // IMF App #2E
        registerValidator("http://www.smpte-ra.org/ns/2067-21/2021", IMFApp2E2021ConstraintsValidator::new);
        registerValidator("http://www.smpte-ra.org/ns/2067-21/2020", IMFApp2E2020ConstraintsValidator::new);
        registerValidator("http://www.smpte-ra.org/schemas/2067-21/2016", IMFApp2E2016ConstraintsValidator::new);
        registerValidator("http://www.smpte-ra.org/schemas/2067-21/2014", IMFApp2E2014ConstraintsValidator::new);

        // IMF App #5
        registerValidator("http://www.smpte-ra.org/ns/2067-50/2017", IMFApp5ConstraintsValidator::new);

        // Plugins
        registerValidator("http://www.smpte-ra.org/ns/2067-201/2019", IMFIABLevel0PluginConstraintsValidator::new);
        registerValidator("http://www.smpte-ra.org/ns/2067-203/2022", IMFMGASADMPluginConstraintsValidator::new);
    }

    private static void registerValidator(String type, Supplier<ConstraintsValidator> constructor) {
        registry.put(type.toLowerCase(), constructor);
    }

    public static ConstraintsValidator getValidator(String type) {
        Supplier<ConstraintsValidator> constructor = registry.get(type.toLowerCase());
        if (constructor != null) {
            return constructor.get();
        }

        return null;
    }

}
