package com.mydemoappappium.components;

import com.mydemoappappium.core.PlatformLocator;

import java.math.BigDecimal;

/**
 * One product line of the cart: an Android list row without id vs an iOS table cell.
 */
public interface CartLine {

    int quantity();

    BigDecimal price();

    void increaseQuantity();

    void remove();

    static CartLine of(String productName) {
        return of(productName, null);
    }

    /**
     * @param color colour name as shown in the cart, e.g. "Blue"; null matches a line of any colour
     */
    static CartLine of(String productName, String color) {
        return PlatformLocator.of((CartLine) new AndroidCartLine(productName, color), new IosCartLine(productName, color));
    }
}
