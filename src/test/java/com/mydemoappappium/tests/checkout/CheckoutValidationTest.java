package com.mydemoappappium.tests.checkout;

import com.mydemoappappium.pages.CatalogPage;
import com.mydemoappappium.pages.PaymentPage;
import com.mydemoappappium.pages.ShippingAddressPage;
import com.mydemoappappium.testdata.Address;
import com.mydemoappappium.testdata.AddressField;
import com.mydemoappappium.testdata.CardField;
import com.mydemoappappium.testdata.TestData;
import io.qameta.allure.TmsLink;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CheckoutValidationTest {

    @DataProvider
    public Object[][] requiredAddressFields() {
        return new Object[][]{
                {AddressField.FULL_NAME, "full name"}, {AddressField.ADDRESS_1, "address"},
                {AddressField.CITY, "city"}, {AddressField.ZIP, "zip"}, {AddressField.COUNTRY, "country"}};
    }

    @TmsLink("TC-13")
    @Test(groups = {"regression"}, dataProvider = "requiredAddressFields",
            description = "TC-13 Shipping address validation")
    public void shouldRejectMissingRequiredAddressField(AddressField field, String fieldName) {
        ShippingAddressPage shipping = openShippingAddress();

        shipping.submit(TestData.address("shipping").with(field, ""));

        assertThat(shipping.validationMessages()).as("message for empty " + fieldName)
                .anySatisfy(message -> assertThat(message).contains("Please provide your " + fieldName));
        assertThat(shipping.isDisplayed()).as("still on shipping address").isTrue();
    }

    @TmsLink("TC-13")
    @Test(groups = {"regression"}, description = "TC-13 Shipping address: optional fields may be empty")
    public void shouldAcceptEmptyOptionalAddressFields() {
        Address address = TestData.address("shipping").with(AddressField.ADDRESS_2, "").with(AddressField.STATE, "");

        PaymentPage payment = openShippingAddress().continueToPayment(address);

        assertThat(payment.isDisplayed()).as("payment reached with empty optional fields").isTrue();
    }

    @DataProvider
    public Object[][] cardFields() {
        return new Object[][]{{CardField.HOLDER}, {CardField.NUMBER}, {CardField.EXPIRY}, {CardField.SECURITY_CODE}};
    }

    @TmsLink("TC-14")
    @Test(groups = {"regression"}, dataProvider = "cardFields", description = "TC-14 Payment validation")
    public void shouldRejectMissingCardField(CardField field) {
        PaymentPage payment = openShippingAddress().continueToPayment(TestData.address("shipping"));

        payment.submit(TestData.card("valid").with(field, ""));

        assertThat(payment.validationMessages()).as("message for empty " + field)
                .contains(TestData.message("payment.invalidValue"));
        assertThat(payment.isDisplayed()).as("still on payment, review not reached").isTrue();
    }

    private static ShippingAddressPage openShippingAddress() {
        return CatalogPage.atLaunch().openProduct(TestData.product("backpack").name()).addToCart().openCart()
                .proceedToCheckoutLoggedOut()
                .logInToCheckout(TestData.user("standard"));
    }
}
