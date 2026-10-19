package com.mydemoappappium.pages;

import com.mydemoappappium.components.NavigationBar;
import com.mydemoappappium.components.NavigationMenu;
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
 */
public class ProductDetailsPage extends BasePage {

    private final NavigationBar navigationBar = new NavigationBar();
    private final NavigationMenu navigationMenu = NavigationMenu.create();

    private final By screen = PlatformLocator.of(
            AppiumBy.accessibilityId("Tap to add product to cart"),
            AppiumBy.accessibilityId("ProductDetails-screen"));
    private final By addToCartButton = PlatformLocator.of(
            AppiumBy.accessibilityId("Tap to add product to cart"),
            AppiumBy.accessibilityId("AddToCart"));
    private final By price = PlatformLocator.of(
            androidId("priceTV"),
            AppiumBy.accessibilityId("Price"));
    private final By quantity = PlatformLocator.of(
            androidId("noTV"),
            AppiumBy.accessibilityId("Amount"));

    // GAP-WORKAROUND (iOS): no accessibilityIdentifier, suggested "Quantity-plus" / "Quantity-minus"; named after images
    private final By increaseButton = PlatformLocator.of(
            AppiumBy.accessibilityId("Increase item quantity"),
            iosInScreen("ProductDetails-screen", "XCUIElementTypeButton[`name == 'AddPlus Icons'`]"));
    private final By decreaseButton = PlatformLocator.of(
            AppiumBy.accessibilityId("Decrease item quantity"),
            iosInScreen("ProductDetails-screen", "XCUIElementTypeButton[`name == 'SubtractMinus Icons'`]"));

    public ProductDetailsPage() {
        waitUntilLoaded();
    }

    @Step("Increase quantity")
    public ProductDetailsPage increaseQuantity() {
        changeQuantity(increaseButton, 1);
        return this;
    }

    @Step("Decrease quantity")
    public ProductDetailsPage decreaseQuantity() {
        changeQuantity(decreaseButton, -1);
        return this;
    }

    /**
     * @param color colour name as shown in the app, e.g. "Blue"
     */
    @Step("Select colour {color}")
    public ProductDetailsPage selectColor(String color) {
        // GAP-WORKAROUND (iOS): no accessibilityIdentifier, suggested "Color-<name>"; named after its image
        click(PlatformLocator.of(
                AppiumBy.accessibilityId(color + " color"),
                iosInScreen("ProductDetails-screen", "XCUIElementTypeButton[`name BEGINSWITH '" + color + "Color'`]")));
        return this;
    }

    private void changeQuantity(By button, int delta) {
        int expected = quantity() + delta;
        click(button);
        Waits.until(driver -> quantity() == expected, "quantity to become " + expected);
    }

    @Override
    protected List<By> loadedIndicators() {
        return List.of(screen, addToCartButton);
    }

    public boolean showsProduct(String name) {
        // GAP-WORKAROUND: no accessibilityIdentifier, suggested "ProductDetails-name"
        return isVisible(PlatformLocator.of(
                androidIdWithText("productTV", name),
                AppiumBy.iOSClassChain("**/XCUIElementTypeOther[`name == 'ProductDetails-screen'`]"
                        + "/**/XCUIElementTypeStaticText[`label == '" + name + "'`]")));
    }

    public BigDecimal price() {
        return parsePrice(textOf(price));
    }

    public int quantity() {
        return parseCount(textOf(quantity));
    }

    public boolean isAddToCartEnabled() {
        return isEnabled(addToCartButton);
    }

    @Step("Add to cart")
    public ProductDetailsPage addToCart() {
        int before = navigationBar.cartItemCount();
        click(addToCartButton);
        Waits.until(driver -> navigationBar.cartItemCount() > before, "cart badge to increase from " + before);
        return this;
    }

    public int cartItemCount() {
        return navigationBar.cartItemCount();
    }

    @Step("Open cart")
    public CartPage openCart() {
        navigationBar.openCart();
        return new CartPage();
    }

    @Step("Back to catalog")
    public CatalogPage openCatalog() {
        navigationMenu.openCatalog();
        return new CatalogPage();
    }
}
