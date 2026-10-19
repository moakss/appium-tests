package com.mydemoappappium.pages;

import com.mydemoappappium.components.NavigationBar;
import com.mydemoappappium.components.NavigationMenu;
import com.mydemoappappium.config.Config;
import com.mydemoappappium.core.BasePage;
import com.mydemoappappium.core.Gestures;
import com.mydemoappappium.core.PlatformLocator;
import com.mydemoappappium.core.Waits;
import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Locators VERIFIED (Appium live search, 2026-10-18, emulator-5554 / iPhone 17 iOS 27.0).
 */
public class CatalogPage extends BasePage {

    private static final int MAX_SCROLLS = 5;

    private final NavigationBar navigationBar = new NavigationBar();
    private final NavigationMenu navigationMenu = NavigationMenu.create();

    private final By screen = PlatformLocator.of(
            androidIdWithText("productTV", "Products"),
            AppiumBy.accessibilityId("Catalog-screen"));
    private final By productList = PlatformLocator.of(
            AppiumBy.accessibilityId("Displays all products of catalog"),
            AppiumBy.accessibilityId("ProductItem"));
    private final By productNames = PlatformLocator.of(
            androidId("titleTV"),
            AppiumBy.accessibilityId("Product Name"));
    // GAP-WORKAROUND (iOS): no accessibilityIdentifier, suggested "Sort-button"; the only button on the catalog screen
    private final By sortButton = PlatformLocator.of(
            AppiumBy.accessibilityId("Shows current sorting order and displays available sorting options"),
            iosInScreen("Catalog-screen", "XCUIElementTypeButton[`name == 'Button'`]"));
    private final By sortDialogTitle = PlatformLocator.of(
            androidIdWithText("sortTV", "Sort by:"),
            AppiumBy.iOSNsPredicateString("type == 'XCUIElementTypeStaticText' AND label == 'Sort by:'"));

    public CatalogPage() {
        waitUntilLoaded();
    }

    private CatalogPage(Duration timeout) {
        waitUntilLoaded(timeout);
    }

    /**
     * Entry point right after the session starts; allows for app start-up time.
     */
    public static CatalogPage atLaunch() {
        return new CatalogPage(Config.get().launchTimeout());
    }

    @Override
    protected List<By> loadedIndicators() {
        return List.of(screen, productList);
    }

    @Step("Read visible product names")
    public List<String> visibleProductNames() {
        return Waits.until(driver -> textsOf(productNames), "visible product names");
    }

    @Step("Open product \"{name}\"")
    public ProductDetailsPage openProduct(String name) {
        By product = product(name);
        // Products with the same sort key have no stable order, so the product may be further down the list.
        for (int swipe = 0; swipe < MAX_SCROLLS && !isVisible(product); swipe++) {
            Gestures.scroll(Gestures.Direction.DOWN);
        }
        click(product);
        return new ProductDetailsPage();
    }

    @Step("Sort catalog by {order}")
    public CatalogPage sortBy(SortOrder order) {
        click(sortButton);
        click(sortOption(order));
        waitUntilGone(sortDialogTitle);
        waitUntilListSettled();
        return this;
    }

    /**
     * Android re-sorts asynchronously after the dialog closes; until then the list still shows the previous order.
     */
    private void waitUntilListSettled() {
        AtomicReference<List<String>> previous = new AtomicReference<>(List.of());
        Waits.until(driver -> {
            List<String> names = textsOf(productNames);
            return !names.isEmpty() && names.equals(previous.getAndSet(names));
        }, "catalog list to settle after sorting");
    }

    public int cartItemCount() {
        return navigationBar.cartItemCount();
    }

    @Step("Open cart")
    public CartPage openCart() {
        navigationBar.openCart();
        return new CartPage();
    }

    @Step("Open login from menu")
    public LoginPage openLogin() {
        navigationMenu.openLogin();
        return new LoginPage();
    }

    @Step("Log out from menu")
    public LoginPage logOut() {
        navigationMenu.logOut();
        return new LoginPage();
    }

    private By product(String name) {
        // Android: the product title is not clickable, so tap the image of the same card.
        return PlatformLocator.of(
                androidSelector("new UiSelector().text(\"" + name + "\")"
                        + ".fromParent(new UiSelector().resourceId(\"" + ANDROID_ID_PREFIX + "productIV\"))"),
                AppiumBy.iOSNsPredicateString("name == 'Product Name' AND label == '" + name + "'"));
    }

    private static By sortOption(SortOrder order) {
        // GAP-WORKAROUND (iOS): sort options have no accessibilityIdentifier, suggested "Sort-<order>"
        return switch (order) {
            case NAME_ASCENDING -> sortOption("Ascending order by name", "Name - Ascending");
            case NAME_DESCENDING -> sortOption("Descending order by name", "Name - Descending");
            case PRICE_ASCENDING -> sortOption("Ascending order by price", "Price - Ascending");
            case PRICE_DESCENDING -> sortOption("Descending order by price", "Price - Descending");
        };
    }

    private static By sortOption(String androidDescription, String iosLabel) {
        return PlatformLocator.of(
                AppiumBy.accessibilityId(androidDescription),
                AppiumBy.iOSNsPredicateString("type == 'XCUIElementTypeButton' AND label == '" + iosLabel + "'"));
    }
}
