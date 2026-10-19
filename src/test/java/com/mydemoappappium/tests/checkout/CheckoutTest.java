package com.mydemoappappium.tests.checkout;

import com.mydemoappappium.pages.CatalogPage;
import com.mydemoappappium.pages.LoginPage;
import com.mydemoappappium.pages.ReviewOrderPage;
import com.mydemoappappium.testdata.Address;
import com.mydemoappappium.testdata.PaymentCard;
import com.mydemoappappium.testdata.Product;
import com.mydemoappappium.testdata.TestData;
import com.mydemoappappium.testdata.User;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CheckoutTest {

    @TmsLink("TC-04")
    @Test(groups = {"smoke"}, description = "TC-04 End-to-end checkout happy path")
    public void shouldCompleteCheckout() {
        Product backpack = TestData.product("backpack");
        Address shipping = TestData.address("shipping");
        PaymentCard card = TestData.card("valid");
        CatalogPage catalog = CatalogPage.atLaunch();
        assertThat(catalog.cartItemCount()).as("precondition: empty cart").isZero();

        ReviewOrderPage review = catalog.openProduct(backpack.name()).addToCart().openCart()
                .proceedToCheckoutLoggedOut()
                .logInToCheckout(TestData.user("standard"))
                .continueToPayment(shipping)
                .reviewOrder(card);

        assertThat(review.shows(backpack.name())).as("review shows product").isTrue();
        assertThat(review.shows(shipping.fullName())).as("review shows recipient").isTrue();
        assertThat(review.shows(shipping.address1())).as("review shows address").isTrue();
        assertThat(review.shows(shipping.city())).as("review shows city").isTrue();
        assertThat(review.shows(card.holder())).as("review shows card holder").isTrue();
        assertThat(review.shows(card.lastFourDigits())).as("review shows card number").isTrue();
        assertThat(review.showsDeliveryFee(TestData.deliveryFee())).as("review shows delivery fee").isTrue();
        assertThat(review.total()).as("total incl. delivery")
                .isEqualByComparingTo(backpack.price().add(TestData.deliveryFee()));

        CatalogPage afterOrder = review.placeOrder().continueShopping();
        assertThat(afterOrder.cartItemCount()).as("cart after completed order").isZero();
    }

    @TmsLink("TC-06")
    @Test(groups = {"regression"}, description = "TC-06 Checkout requires login when logged out")
    public void shouldRequireLoginForCheckout() {
        Product backpack = TestData.product("backpack");

        LoginPage login = CatalogPage.atLaunch().openProduct(backpack.name()).addToCart().openCart()
                .proceedToCheckoutLoggedOut();
        assertThat(login.isDisplayed()).as("login shown instead of shipping").isTrue();

        assertThat(login.logInToCheckout(TestData.user("standard")).isDisplayed())
                .as("shipping address after login").isTrue();
    }

    @TmsLink("TC-15")
    @Test(groups = {"regression"}, description = "TC-15 Checkout with different billing address")
    public void shouldCompleteCheckoutWithDifferentBillingAddress() {
        Product backpack = TestData.product("backpack");
        Address billing = TestData.address("billing");
        PaymentCard card = TestData.card("valid");

        ReviewOrderPage review = CatalogPage.atLaunch().openProduct(backpack.name()).addToCart().openCart()
                .proceedToCheckoutLoggedOut()
                .logInToCheckout(TestData.user("standard"))
                .continueToPayment(TestData.address("shipping"))
                .reviewOrder(card, billing);

        assertThat(review.shows(billing.address1())).as("review shows billing address").isTrue();
        assertThat(review.shows(billing.city())).as("review shows billing city").isTrue();
        assertThat(review.placeOrder().isDisplayed()).as("order completed").isTrue();
    }
}
