package com.mydemoappappium.pages;

import com.mydemoappappium.core.BasePage;
import com.mydemoappappium.core.Gestures;
import com.mydemoappappium.core.PlatformLocator;
import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

import java.math.BigDecimal;
import java.util.List;

/**
 * Locators VERIFIED (Appium live search, 2026-10-18, emulator-5554 / iPhone 17 iOS 27.0).
 */
public class ReviewOrderPage extends BasePage {

    // GAP-WORKAROUND (iOS): no screen identifier, suggested "ReviewOrder-screen"
    private final By screen = PlatformLocator.of(
            androidIdWithText("enterShippingAddressTV", "Review your order"),
            AppiumBy.iOSNsPredicateString("type == 'XCUIElementTypeStaticText' AND label == 'Review your order'"));
    // GAP-WORKAROUND (iOS): no accessibilityIdentifier, suggested "PlaceOrder-button"
    private final By placeOrderButton = PlatformLocator.of(
            AppiumBy.accessibilityId("Completes the process of checkout"),
            AppiumBy.iOSNsPredicateString("type == 'XCUIElementTypeButton' AND label == 'Place Order'"));
    // GAP-WORKAROUND (iOS): no accessibilityIdentifier, suggested "Review-total"
    private final By total = PlatformLocator.of(
            androidId("totalAmountTV"),
            AppiumBy.iOSClassChain("**/XCUIElementTypeOther[$label == 'Total:'$]"
                    + "/XCUIElementTypeStaticText[`label BEGINSWITH '$'`]"));

    public ReviewOrderPage() {
        waitUntilLoaded();
    }

    @Override
    protected List<By> loadedIndicators() {
        return List.of(screen, placeOrderButton);
    }

    public BigDecimal total() {
        return parsePrice(textOf(total));
    }

    /**
     * Whether the order summary shows the text (product, address or card details), scrolling on Android if needed.
     */
    public boolean shows(String text) {
        return present(PlatformLocator.of(
                Gestures.androidScrollIntoView("new UiSelector().textContains(\"" + text + "\")"),
                AppiumBy.iOSNsPredicateString("type == 'XCUIElementTypeStaticText' AND label CONTAINS '" + text + "'")))
                .isPresent();
    }

    public boolean showsDeliveryFee(BigDecimal fee) {
        return shows("$" + fee.toPlainString());
    }

    @Step("Place order")
    public CheckoutCompletePage placeOrder() {
        click(placeOrderButton);
        return new CheckoutCompletePage();
    }
}
