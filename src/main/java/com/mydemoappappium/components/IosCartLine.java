package com.mydemoappappium.components;

import com.mydemoappappium.core.BaseComponent;
import org.openqa.selenium.By;

import java.math.BigDecimal;

/**
 * Locators VERIFIED (Appium live search, 2026-10-18, iPhone 17 iOS 27.0).
 * Anchored to the existing "Cart-screen" identifier; the cell is located by product name (and colour).
 */
class IosCartLine extends BaseComponent implements CartLine {

    private final By quantity;
    private final By price;
    private final By increaseButton;
    private final By removeButton;

    IosCartLine(String productName, String color) {
        quantity = child(productName, color, "XCUIElementTypeStaticText[`label MATCHES '^[0-9]+$'`]");
        price = child(productName, color, "XCUIElementTypeStaticText[`label BEGINSWITH '$'`]");
        increaseButton = child(productName, color, "XCUIElementTypeButton[`name == 'AddPlus Icons'`]");
        removeButton = child(productName, color, "XCUIElementTypeButton[`name == 'Remove Item'`]");
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

    // GAP-WORKAROUND: cart lines have no accessibilityIdentifier, suggested "CartItem-<part>"
    private static By child(String productName, String color, String chainTail) {
        String cell = "XCUIElementTypeCell[$label == '" + productName + "'$]"
                + (color == null ? "" : "[$label == '" + color + "'$]");
        return iosInScreen("Cart-screen", cell + "/" + chainTail);
    }
}
