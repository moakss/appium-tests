package com.mydemoappappium.components;

import com.mydemoappappium.core.BaseComponent;
import com.mydemoappappium.core.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;

import java.util.List;

/**
 * Locators VERIFIED (Appium live search, 2026-10-18, emulator-5554): every form error text has an id ending in "ErrorTV".
 */
class AndroidInlineErrors extends BaseComponent implements ValidationMessages {

    private final By errorTexts = androidSelector("new UiSelector().resourceIdMatches(\".*:id/[A-Za-z0-9]+ErrorTV\")");

    @Override
    public List<String> read() {
        try {
            return Waits.until(driver -> {
                List<String> texts = textsOf(errorTexts);
                return texts.isEmpty() ? null : texts;
            }, "validation error texts");
        } catch (TimeoutException e) {
            return List.of();
        }
    }

    @Override
    public void dismiss() {
        // Inline errors need no dismissal.
    }
}
