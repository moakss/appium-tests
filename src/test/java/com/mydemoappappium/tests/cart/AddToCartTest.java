package com.mydemoappappium.tests.cart;

import com.mydemoappappium.pages.CartPage;
import com.mydemoappappium.pages.CatalogPage;
import com.mydemoappappium.pages.ProductDetailsPage;
import com.mydemoappappium.testdata.Product;
import com.mydemoappappium.testdata.TestData;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AddToCartTest {

    @TmsLink("TC-03")
    @Test(groups = {"smoke"}, description = "TC-03 Add product to cart updates badge and cart")
    public void shouldAddProductToCart() {
        Product backpack = TestData.product("backpack");
        CatalogPage catalog = CatalogPage.atLaunch();
        assertThat(catalog.cartItemCount()).as("precondition: empty cart").isZero();

        ProductDetailsPage details = catalog.openProduct(backpack.name()).addToCart();
        assertThat(details.cartItemCount()).as("cart badge").isEqualTo(1);

        CartPage cart = details.openCart();
        assertThat(cart.hasLine(backpack.name())).as("cart line for " + backpack.name()).isTrue();
        assertThat(cart.lineQuantity(backpack.name())).as("line quantity").isEqualTo(1);
        assertThat(cart.linePrice(backpack.name())).as("line price").isEqualByComparingTo(backpack.price());
        assertThat(cart.itemCount()).as("item count").isEqualTo(1);
        assertThat(cart.total()).as("total").isEqualByComparingTo(backpack.price());
    }
}
