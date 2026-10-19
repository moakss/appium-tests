package com.mydemoappappium.pages;

import com.mydemoappappium.core.BasePage;
import com.mydemoappappium.core.PlatformLocator;
import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

import java.util.List;

/**
 * Locators VERIFIED (Appium live search, 2026-10-18, emulator-5554 / iPhone 17 iOS 27.0).
 */
public class CheckoutCompletePage extends BasePage {

    private final By screen = PlatformLocator.of(
            androidIdWithText("completeTV", "Checkout Complete"),
            AppiumBy.accessibilityId("CheckoutComplete-screen"));
    private final By continueShoppingButton = PlatformLocator.of(
            AppiumBy.accessibilityId("Tap to open catalog"),
            AppiumBy.accessibilityId("ContinueShopping"));

    public CheckoutCompletePage() {
        waitUntilLoaded();
    }

    @Override
    protected List<By> loadedIndicators() {
        return List.of(screen, continueShoppingButton);
    }

    @Step("Continue shopping")
    public CatalogPage continueShopping() {
        click(continueShoppingButton);
        return new CatalogPage();
    }
}
