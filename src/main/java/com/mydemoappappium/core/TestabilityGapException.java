package com.mydemoappappium.core;

/**
 * Thrown when no reliable locator exists for a required element. The test is reported as skipped
 * (blocked) with this message instead of failing on a brittle locator (CLAUDE.md §4.1).
 */
public class TestabilityGapException extends RuntimeException {

    public TestabilityGapException(String message) {
        super("Blocked: " + message);
    }
}
