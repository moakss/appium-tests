package com.mydemoappappium.core;

import com.mydemoappappium.config.Config;
import com.mydemoappappium.config.Platform;
import com.mydemoappappium.driver.DriverHolder;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.remote.AppiumWebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;

import java.util.Locale;
import java.util.Map;

/**
 * Low-level gesture mechanics. Pages and components expose them through intent methods only.
 */
public final class Gestures {

    public enum Direction { UP, DOWN, LEFT, RIGHT }

    private static final double SCROLL_PERCENT = 0.75;

    private Gestures() {
    }

    /**
     * Scrolls the screen content once. On Android returns false when the end is reached;
     * on iOS always returns true (the driver does not report it).
     */
    public static boolean scroll(Direction direction) {
        String dir = direction.name().toLowerCase(Locale.ROOT);
        if (Config.get().platform() == Platform.IOS) {
            DriverHolder.driver().executeScript("mobile: scroll", Map.of("direction", dir));
            return true;
        }
        Dimension size = DriverHolder.driver().manage().window().getSize();
        Object canScrollMore = DriverHolder.driver().executeScript("mobile: scrollGesture", Map.of(
                "left", size.width / 10, "top", size.height / 5,
                "width", size.width * 8 / 10, "height", size.height * 3 / 5,
                "direction", dir, "percent", SCROLL_PERCENT));
        return Boolean.TRUE.equals(canScrollMore);
    }

    /**
     * iOS: scrolls the nearest scroll view until the element is visible. On Android off-screen elements are not
     * in the hierarchy, so {@link #androidScrollIntoView(String)} is used in the locator instead.
     */
    public static void scrollIntoView(WebElement element) {
        if (Config.get().platform() == Platform.IOS) {
            DriverHolder.driver().executeScript("mobile: scroll",
                    Map.of("elementId", ((AppiumWebElement) element).getId(), "toVisible", true));
        }
    }

    /**
     * Android only: a locator that scrolls the first scrollable container until the element matching
     * {@code uiSelector} (a UiSelector expression) is in view.
     */
    public static By androidScrollIntoView(String uiSelector) {
        return AppiumBy.androidUIAutomator(
                "new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(" + uiSelector + ")");
    }
}
