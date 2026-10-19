package com.mydemoappappium.components;

import com.mydemoappappium.core.BaseComponent;
import com.mydemoappappium.core.Gestures;
import com.mydemoappappium.core.TestabilityGapException;
import com.mydemoappappium.core.Waits;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import java.math.BigDecimal;
import java.util.List;

/**
 * Locators VERIFIED (Appium live search, 2026-10-19, emulator-5554).
 * Cart rows have no id and a UiSelector cannot select a parent by its child, so the row is the list item that
 * contains both the product title and the line controls (element-scoped id searches, which stay inside the row).
 */
class AndroidCartLine extends BaseComponent implements CartLine {

    private static final int MAX_SCROLLS = 3;

    private final By list = androidId("productRV");
    private final By rowCandidates = AppiumBy.className("android.view.ViewGroup");
    private final By title = androidId("titleTV");
    private final By quantity = androidId("noTV");
    private final By price = androidId("priceTV");
    private final By increaseButton = AppiumBy.accessibilityId("Increase item quantity");
    private final By removeButton = AppiumBy.accessibilityId("Removes product from cart");

    private final String productName;
    private final String color;

    AndroidCartLine(String productName, String color) {
        this.productName = productName;
        this.color = color;
    }

    @Override
    public int quantity() {
        return parseCount(textOf(quantity));
    }

    @Override
    public BigDecimal price() {
        return parsePrice(textOf(price));
    }

    @Override
    public void increaseQuantity() {
        click(increaseButton);
    }

    @Override
    public void remove() {
        click(removeButton);
    }

    @Override
    protected SearchContext context() {
        if (color != null) {
            throw new TestabilityGapException("the Android cart does not expose the colour of a line ('"
                    + productName + "', " + color + ")");
        }
        // Scrolling stops as soon as the title is in view; the controls below it may need further scrolls.
        driver().findElements(Gestures.androidScrollIntoView(
                "new UiSelector().resourceId(\"" + ANDROID_ID_PREFIX + "titleTV\").text(\"" + productName + "\")"));
        for (int swipe = 0; ; swipe++) {
            List<WebElement> rows = Waits.until(driver -> rowsOf(productName), "cart rows of '" + productName + "'");
            if (rows.size() == 1) {
                return rows.getFirst();
            }
            if (rows.size() > 1) {
                throw new TestabilityGapException("no reliable locator for the Android cart line of '" + productName
                        + "': " + rows.size() + " lines differ only by colour, which is not exposed");
            }
            if (swipe == MAX_SCROLLS) {
                throw new NoSuchElementException("Android cart line of '" + productName + "' with its controls in view");
            }
            Gestures.scroll(Gestures.Direction.DOWN);
        }
    }

    private List<WebElement> rowsOf(String name) {
        return driver().findElement(list).findElements(rowCandidates).stream()
                .filter(row -> !row.findElements(quantity).isEmpty()
                        && row.findElements(title).stream().anyMatch(rowTitle -> name.equals(rowTitle.getText())))
                .toList();
    }
}
