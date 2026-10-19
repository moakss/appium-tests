package com.mydemoappappium.pages;

import com.mydemoappappium.components.CartLine;
import com.mydemoappappium.core.BasePage;
import com.mydemoappappium.core.PlatformLocator;
import com.mydemoappappium.core.Waits;
import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

import java.math.BigDecimal;
import java.util.List;

/**
 * Locators VERIFIED (Appium live search, 2026-10-18, emulator-5554 / iPhone 17 iOS 27.0).
 * iOS fallbacks are anchored to the existing "Cart-screen" identifier. Line details and actions go through CartLine.
 */
public class CartPage extends BasePage {

    private static final String IOS_SCREEN = "Cart-screen";

    // Android: the cart content is cartCL with items and noItemCL when empty (no "My Cart" title then).
    private final By screen = PlatformLocator.of(
            androidSelector("new UiSelector().resourceIdMatches(\".*:id/(cartCL|noItemCL)\")"),
            AppiumBy.accessibilityId(IOS_SCREEN));
    // GAP-WORKAROUND (iOS): no accessibilityIdentifier, suggested "Cart-itemCount"
    private final By itemCount = PlatformLocator.of(
            androidId("itemsTV"),
            iosInScreen(IOS_SCREEN, "XCUIElementTypeStaticText[`label MATCHES '^[0-9]+ Items$'`]"));
    // GAP-WORKAROUND (iOS): no accessibilityIdentifier, suggested "Cart-total"
    private final By total = PlatformLocator.of(
            androidId("totalPriceTV"),
            iosInScreen(IOS_SCREEN, "XCUIElementTypeOther[$label == 'Total:'$]"
                    + "/XCUIElementTypeStaticText[`label BEGINSWITH '$'`]"));
    // GAP-WORKAROUND (iOS): no accessibilityIdentifier, suggested "CartItem"
    private final By lines = PlatformLocator.of(
            androidId("titleTV"),
            iosInScreen(IOS_SCREEN, "XCUIElementTypeCell"));
    // GAP-WORKAROUND (iOS): no accessibilityIdentifier, suggested "Cart-empty"
    private final By emptyState = PlatformLocator.of(
            androidIdWithText("noItemTitleTV", "No Items"),
            iosInScreen(IOS_SCREEN, "XCUIElementTypeStaticText[`label == 'No Items'`]"));
    private final By goShoppingButton = PlatformLocator.of(
            androidId("shoppingBt"),
            AppiumBy.accessibilityId("GoShopping"));
    private final By checkoutButton = PlatformLocator.of(
            AppiumBy.accessibilityId("Confirms products for checkout"),
            AppiumBy.accessibilityId("ProceedToCheckout"));

    public CartPage() {
        waitUntilLoaded();
    }

    @Override
    protected List<By> loadedIndicators() {
        return List.of(screen);
    }

    public boolean isEmpty() {
        return isVisible(emptyState);
    }

    public int lineCount() {
        return textsOf(lines).size();
    }

    public boolean hasLine(String productName) {
        // GAP-WORKAROUND (iOS): no accessibilityIdentifier, suggested "CartItem-name"
        return isVisible(PlatformLocator.of(
                androidIdWithText("titleTV", productName),
                iosInScreen(IOS_SCREEN, "XCUIElementTypeCell/XCUIElementTypeStaticText[`label == '"
                        + productName + "'`]")));
    }

    public int lineQuantity(String productName) {
        return CartLine.of(productName).quantity();
    }

    /**
     * @param color colour name as shown in the cart, e.g. "Blue"
     */
    public int lineQuantity(String productName, String color) {
        return CartLine.of(productName, color).quantity();
    }

    public BigDecimal linePrice(String productName) {
        return CartLine.of(productName).price();
    }

    @Step("Increase quantity of \"{productName}\"")
    public CartPage increaseQuantity(String productName) {
        int expected = itemCount() + 1;
        CartLine.of(productName).increaseQuantity();
        Waits.until(driver -> itemCount() == expected, "cart item count to become " + expected);
        return this;
    }

    @Step("Remove \"{productName}\" from cart")
    public CartPage remove(String productName) {
        CartLine.of(productName).remove();
        Waits.until(driver -> !hasLine(productName), "'" + productName + "' to be removed");
        return this;
    }

    public int itemCount() {
        return parseCount(textOf(itemCount));
    }

    public BigDecimal total() {
        return parsePrice(textOf(total));
    }

    @Step("Go shopping")
    public CatalogPage goShopping() {
        click(goShoppingButton);
        return new CatalogPage();
    }

    @Step("Proceed to checkout (logged out)")
    public LoginPage proceedToCheckoutLoggedOut() {
        click(checkoutButton);
        return new LoginPage();
    }

    @Step("Proceed to checkout (logged in)")
    public ShippingAddressPage proceedToCheckout() {
        click(checkoutButton);
        return new ShippingAddressPage();
    }
}
