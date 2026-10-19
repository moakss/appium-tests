package com.mydemoappappium.config;

import java.util.Arrays;
import java.util.Locale;

public enum Platform {
    ANDROID("android"),
    IOS("ios");

    private final String id;

    Platform(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static Platform from(String value) {
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(platform -> platform.id.equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown platform '" + value + "'. Use -Dplatform=android or -Dplatform=ios"));
    }
}
