package com.mydemoappappium.testdata;

import java.math.BigDecimal;
import java.util.List;

/**
 * @param colors colour names offered on the product details screen, as shown in the app
 */
public record Product(String name, BigDecimal price, List<String> colors) {
}
