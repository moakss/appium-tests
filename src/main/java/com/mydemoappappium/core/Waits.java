package com.mydemoappappium.core;

import com.mydemoappappium.config.Config;
import com.mydemoappappium.driver.DriverHolder;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.function.Function;

/**
 * All explicit waits go through this class. Timeouts and polling come from configuration only.
 */
public final class Waits {

    private Waits() {
    }

    public static <T> T until(Function<WebDriver, T> condition, String description) {
        return until(condition, description, Config.get().defaultTimeout());
    }

    public static <T> T until(Function<WebDriver, T> condition, String description, Duration timeout) {
        WebDriverWait wait = new WebDriverWait(DriverHolder.driver(), timeout, Config.get().polling());
        wait.ignoring(StaleElementReferenceException.class).withMessage(() -> "waiting for " + description);
        return wait.until(condition);
    }

    public static WebElement visible(SearchContext context, By locator, Duration timeout) {
        return until(driver -> displayedOrNull(context, locator), locator + " to be visible", timeout);
    }

    public static WebElement clickable(SearchContext context, By locator) {
        return until(driver -> {
            WebElement element = displayedOrNull(context, locator);
            return element != null && element.isEnabled() ? element : null;
        }, locator + " to be clickable", Config.get().defaultTimeout());
    }

    /**
     * For text input: iOS reports fields covered by the keyboard as not visible, although they accept input.
     */
    public static WebElement enabled(SearchContext context, By locator) {
        return until(driver -> {
            try {
                return context.findElements(locator).stream().filter(WebElement::isEnabled).findFirst().orElse(null);
            } catch (StaleElementReferenceException | NoSuchElementException e) {
                return null;
            }
        }, locator + " to be enabled", Config.get().defaultTimeout());
    }

    public static boolean isVisibleNow(SearchContext context, By locator) {
        return displayedOrNull(context, locator) != null;
    }

    private static WebElement displayedOrNull(SearchContext context, By locator) {
        try {
            return context.findElements(locator).stream().filter(WebElement::isDisplayed).findFirst().orElse(null);
        } catch (StaleElementReferenceException | NoSuchElementException e) {
            return null;
        }
    }
}
