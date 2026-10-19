package com.mydemoappappium.tests.auth;

import com.mydemoappappium.pages.CatalogPage;
import com.mydemoappappium.pages.LoginPage;
import com.mydemoappappium.testdata.Product;
import com.mydemoappappium.testdata.TestData;
import com.mydemoappappium.testdata.User;
import io.qameta.allure.TmsLink;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LoginTest {

    @TmsLink("TC-05")
    @Test(groups = {"smoke"}, description = "TC-05 Login with valid user from menu")
    public void shouldLogInFromMenu() {
        User user = TestData.user("standard");
        Product backpack = TestData.product("backpack");

        CatalogPage catalog = CatalogPage.atLaunch().openLogin().logIn(user);

        // iOS shows no logged-in indicator, so the login is confirmed by checkout skipping the login step.
        assertThat(catalog.openProduct(backpack.name()).addToCart().openCart().proceedToCheckout().isDisplayed())
                .as("checkout goes straight to the shipping address when logged in").isTrue();
    }

    @DataProvider
    public Object[][] missingCredentials() {
        User user = TestData.user("standard");
        return new Object[][]{
                {"empty username", user.withUsername(""), TestData.message("login.usernameRequired")},
                {"empty password", user.withPassword(""), TestData.message("login.passwordRequired")}};
    }

    @TmsLink("TC-07")
    @Test(groups = {"regression"}, dataProvider = "missingCredentials", description = "TC-07 Login validation")
    public void shouldRejectMissingCredentials(String caseName, User credentials, String expectedMessage) {
        LoginPage login = CatalogPage.atLaunch().openLogin()
                .submitCredentials(credentials.username(), credentials.password());

        assertThat(login.validationMessages()).as("validation message for " + caseName).contains(expectedMessage);
        assertThat(login.isDisplayed()).as("still on login after " + caseName).isTrue();
    }

    @TmsLink("TC-08")
    @Test(groups = {"regression"}, description = "TC-08 Logout")
    public void shouldLogOut() {
        User user = TestData.user("standard");
        Product backpack = TestData.product("backpack");
        CatalogPage catalog = CatalogPage.atLaunch().openLogin().logIn(user);

        LoginPage login = catalog.logOut();
        assertThat(login.isDisplayed()).as("login screen after logout").isTrue();

        LoginPage checkoutLogin = login.openCatalog().openProduct(backpack.name()).addToCart().openCart()
                .proceedToCheckoutLoggedOut();
        assertThat(checkoutLogin.isDisplayed()).as("checkout requires login again after logout").isTrue();
    }
}
