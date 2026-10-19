package com.mydemoappappium.core;

import com.mydemoappappium.config.Config;
import com.mydemoappappium.driver.DriverHolder;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Shared element interactions for pages and components. Elements are looked up fresh on every call.
 */
public abstract class UiObject {

    protected static final String ANDROID_ID_PREFIX = "com.saucelabs.mydemoapp.android:id/";

    protected AppiumDriver driver() {
        return DriverHolder.driver();
    }

    /**
     * Where locators are searched: the whole screen for pages, the component root for components.
     */
    protected SearchContext context() {
        return driver();
    }

    protected WebElement visible(By locator) {
        return visible(locator, Config.get().defaultTimeout());
    }

    protected WebElement visible(By locator, Duration timeout) {
        return Waits.visible(context(), locator, timeout);
    }

    protected void click(By locator) {
        present(locator).filter(element -> !element.isDisplayed()).ifPresent(Gestures::scrollIntoView);
        Waits.clickable(context(), locator).click();
    }

    /**
     * For elements iOS reports as not visible although they are shown and tappable (e.g. image buttons).
     */
    protected void clickEnabled(By locator) {
        Waits.enabled(context(), locator).click();
    }

    protected void type(By locator, String text) {
        WebElement field = Waits.enabled(context(), locator);
        field.clear();
        field.sendKeys(text);
    }

    protected String textOf(By locator) {
        return visible(locator).getText();
    }

    protected boolean isVisible(By locator) {
        return Waits.isVisibleNow(context(), locator);
    }

    /**
     * For elements the platform reports as not visible although they are shown (e.g. iOS cart badge).
     */
    protected Optional<WebElement> present(By locator) {
        return context().findElements(locator).stream().findFirst();
    }

    protected void waitUntilGone(By locator) {
        Waits.until(driver -> !isVisible(locator), locator + " to disappear");
    }

    protected boolean isEnabled(By locator) {
        return visible(locator).isEnabled();
    }

    protected List<String> textsOf(By locator) {
        return context().findElements(locator).stream().map(WebElement::getText).toList();
    }

    protected static By androidId(String id) {
        return AppiumBy.id(ANDROID_ID_PREFIX + id);
    }

    protected static By androidSelector(String uiSelector) {
        return AppiumBy.androidUIAutomator(uiSelector);
    }

    /**
     * iOS class chain scoped to an existing screen identifier, e.g. "Cart-screen".
     */
    protected static By iosInScreen(String screenId, String chainTail) {
        return AppiumBy.iOSClassChain("**/XCUIElementTypeOther[`name == '" + screenId + "'`]/**/" + chainTail);
    }

    /**
     * Android element with the given resource id and exact text — for ids reused across screens or rows.
     */
    protected static By androidIdWithText(String id, String text) {
        return AppiumBy.androidUIAutomator(
                "new UiSelector().resourceId(\"" + ANDROID_ID_PREFIX + id + "\").text(\"" + text + "\")");
    }

    /**
     * Parses UI prices such as "$ 29.99" and "$29.99".
     */
    protected static BigDecimal parsePrice(String text) {
        return new BigDecimal(text.replaceAll("[^0-9.]", ""));
    }

    /**
     * Parses the leading number of texts such as "1" or "2 Items".
     */
    protected static int parseCount(String text) {
        return Integer.parseInt(text.trim().split("\\s+")[0]);
    }
}
