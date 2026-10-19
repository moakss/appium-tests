package com.mydemoappappium.tests.cart;

import com.mydemoappappium.pages.CartPage;
import com.mydemoappappium.pages.CatalogPage;
import com.mydemoappappium.pages.ProductDetailsPage;
import com.mydemoappappium.pages.SortOrder;
import com.mydemoappappium.testdata.Product;
import com.mydemoappappium.testdata.TestData;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class CartTest {

    @TmsLink("TC-10")
    @Test(groups = {"regression"}, description = "TC-10 Cart: change quantity, remove item, totals")
    public void shouldRecalculateCartWhenChangingLines() {
        Product backpack = TestData.product("backpack");
        Product fleece = TestData.product("fleeceJacket");
        CatalogPage catalog = CatalogPage.atLaunch();
        assertThat(catalog.cartItemCount()).as("precondition: empty cart").isZero();

        ProductDetailsPage details = catalog.openProduct(backpack.name()).addToCart();
        // Workaround: on Android the app crashes when opening a product after the 6th catalog position
        // (bug reported). Price descending moves the Fleece Jacket, the most expensive product, to the top.
        // Remove the sort once the crash is fixed.
        details.openCatalog().sortBy(SortOrder.PRICE_DESCENDING).openProduct(fleece.name()).addToCart();

        CartPage cart = details.openCart();
        assertThat(cart.itemCount()).as("precondition: items").isEqualTo(2);
        assertThat(cart.total()).as("precondition: total").isEqualByComparingTo(backpack.price().add(fleece.price()));

        cart.increaseQuantity(backpack.name());
        assertThat(cart.itemCount()).as("items after increasing backpack").isEqualTo(3);
        assertThat(cart.total()).as("total after increasing backpack")
                .isEqualByComparingTo(backpack.price().multiply(BigDecimal.TWO).add(fleece.price()));

        cart.remove(fleece.name());
        assertThat(cart.itemCount()).as("items after removing fleece").isEqualTo(2);
        assertThat(cart.total()).as("total after removing fleece")
                .isEqualByComparingTo(backpack.price().multiply(BigDecimal.TWO));
    }

    @TmsLink("TC-11")
    @Test(groups = {"regression"}, description = "TC-11 Empty cart state and Go Shopping")
    public void shouldShowEmptyCartAndGoShopping() {
        CartPage cart = CatalogPage.atLaunch().openCart();

        assertThat(cart.isEmpty()).as("empty cart state shown").isTrue();
        assertThat(cart.goShopping().isDisplayed()).as("catalog after Go Shopping").isTrue();
    }

    @TmsLink("TC-16")
    @Test(groups = {"regression"},
            description = "TC-16 Same product + same colour merges in cart; different colours are separate lines")
    public void shouldMergeSameColourAndSeparateDifferentColours() {
        Product backpack = TestData.product("backpack");
        String colorA = backpack.colors().get(0);
        String colorB = backpack.colors().get(1);

        ProductDetailsPage details = CatalogPage.atLaunch().openProduct(backpack.name());
        details.selectColor(colorA).addToCart().addToCart();
        details.selectColor(colorB).addToCart();
        CartPage cart = details.openCart();

        assertThat(cart.lineCount()).as("cart lines").isEqualTo(2);
        assertThat(cart.lineQuantity(backpack.name(), colorA)).as(colorA + " line quantity").isEqualTo(2);
        assertThat(cart.lineQuantity(backpack.name(), colorB)).as(colorB + " line quantity").isEqualTo(1);
    }
}
