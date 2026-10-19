package com.mydemoappappium.tests.product;

import com.mydemoappappium.pages.CatalogPage;
import com.mydemoappappium.pages.ProductDetailsPage;
import com.mydemoappappium.testdata.Product;
import com.mydemoappappium.testdata.TestData;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ProductDetailsTest {

    @TmsLink("TC-02")
    @Test(groups = {"smoke"}, description = "TC-02 Open product details from catalog")
    public void shouldShowProductDetailsFromCatalog() {
        Product backpack = TestData.product("backpack");

        ProductDetailsPage details = CatalogPage.atLaunch().openProduct(backpack.name());

        assertThat(details.showsProduct(backpack.name())).as("product name shown").isTrue();
        assertThat(details.price()).as("price").isEqualByComparingTo(backpack.price());
        assertThat(details.quantity()).as("default quantity").isEqualTo(1);
        assertThat(details.isAddToCartEnabled()).as("Add To Cart enabled").isTrue();
    }

    @TmsLink("TC-09")
    @Test(groups = {"regression"}, description = "TC-09 Product quantity stepper and Add To Cart disabled at 0")
    public void shouldChangeQuantityAndDisableAddToCartAtZero() {
        Product backpack = TestData.product("backpack");
        ProductDetailsPage details = CatalogPage.atLaunch().openProduct(backpack.name());

        details.increaseQuantity().increaseQuantity();
        assertThat(details.quantity()).as("quantity after + x2").isEqualTo(3);

        details.decreaseQuantity().decreaseQuantity().decreaseQuantity();
        assertThat(details.quantity()).as("quantity after - x3").isZero();
        assertThat(details.isAddToCartEnabled()).as("Add To Cart enabled at quantity 0").isFalse();

        details.increaseQuantity().increaseQuantity().addToCart();
        assertThat(details.cartItemCount()).as("cart badge after adding quantity 2").isEqualTo(2);
    }
}
