package com.mydemoappappium.components;

import com.mydemoappappium.core.PlatformLocator;

import java.util.List;

/**
 * Form validation feedback: inline error texts on Android, a "Validation Error!" alert on iOS.
 */
public interface ValidationMessages {

    /**
     * Waits for validation messages; returns an empty list if none appear within the default timeout.
     */
    List<String> read();

    void dismiss();

    static ValidationMessages create() {
        return PlatformLocator.of((ValidationMessages) new AndroidInlineErrors(), new IosValidationAlert());
    }
}
