package com.mydemoappappium.core;

import com.mydemoappappium.config.Config;
import com.mydemoappappium.config.Platform;

public final class PlatformLocator {

    private PlatformLocator() {
    }

    /**
     * Picks the value for the current platform: usually a locator, sometimes a platform-specific attribute name.
     */
    public static <T> T of(T android, T ios) {
        return Config.get().platform() == Platform.ANDROID ? android : ios;
    }
}
