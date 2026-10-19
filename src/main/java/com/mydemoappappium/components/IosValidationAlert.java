package com.mydemoappappium.components;

import com.mydemoappappium.core.BaseComponent;
import com.mydemoappappium.core.Waits;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;

import java.util.List;

/**
 * Locators VERIFIED (Appium live search, 2026-10-18, iPhone 17 iOS 27.0).
 */
class IosValidationAlert extends BaseComponent implements ValidationMessages {

    private final By alert = AppiumBy.iOSClassChain("**/XCUIElementTypeAlert[`name == 'Validation Error!'`]");
    private final By messages = AppiumBy.iOSClassChain("**/XCUIElementTypeAlert[`name == 'Validation Error!'`]"
            + "/**/XCUIElementTypeStaticText[`label != 'Validation Error!'`]");
    private final By okButton = AppiumBy.iOSClassChain("**/XCUIElementTypeAlert[`name == 'Validation Error!'`]"
            + "/**/XCUIElementTypeButton[`label == 'OK'`]");

    @Override
    public List<String> read() {
        try {
            return Waits.until(driver -> {
                List<String> texts = textsOf(messages);
                return texts.isEmpty() ? null : texts;
            }, "validation alert");
        } catch (TimeoutException e) {
            return List.of();
        }
    }

    @Override
    public void dismiss() {
        if (isVisible(alert)) {
            click(okButton);
            waitUntilGone(alert);
        }
    }
}
