package com.mydemoappappium.components;

import com.mydemoappappium.core.BaseComponent;
import com.mydemoappappium.core.PlatformLocator;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

/**
 * Cart entry point: Android header, iOS bottom bar.
 * Locators VERIFIED (Appium live search, 2026-10-18, emulator-5554 / iPhone 17 iOS 27.0).
 */
public class NavigationBar extends BaseComponent {

    private final By cartButton = PlatformLocator.of(
            AppiumBy.accessibilityId("View cart"),
            AppiumBy.accessibilityId("Cart-tab-item"));

    // GAP-WORKAROUND: no accessibilityIdentifier, suggested "Cart-badge"
    private final By cartBadge = PlatformLocator.of(
            androidId("cartTV"),
            AppiumBy.iOSClassChain("**/XCUIElementTypeOther[$name == 'Cart-tab-item'$]"
                    + "/XCUIElementTypeOther/XCUIElementTypeStaticText[`label MATCHES '^[0-9]+$'`]"));

    public void openCart() {
        click(cartButton);
    }

    /**
     * The badge does not exist on either platform while the cart is empty. Presence is used because
     * iOS reports the badge as not visible although it is shown.
     */
    public int cartItemCount() {
        return present(cartBadge).map(badge -> parseCount(badge.getText())).orElse(0);
    }
}
