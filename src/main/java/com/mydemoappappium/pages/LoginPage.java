package com.mydemoappappium.pages;

import com.mydemoappappium.components.NavigationMenu;
import com.mydemoappappium.components.ValidationMessages;
import com.mydemoappappium.core.BasePage;
import com.mydemoappappium.core.PlatformLocator;
import com.mydemoappappium.core.TestabilityGapException;
import com.mydemoappappium.testdata.User;
import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

import java.util.List;

/**
 * Locators VERIFIED (Appium live search, 2026-10-18, emulator-5554 / iPhone 17 iOS 27.0).
 * Valid logins use the saved-user shortcut, which fills username and password on both platforms.
 */
public class LoginPage extends BasePage {

    private final NavigationMenu navigationMenu = NavigationMenu.create();
    private final ValidationMessages validation = ValidationMessages.create();

    // GAP-WORKAROUND (iOS): no accessibilityIdentifier, suggested "Login-screen"
    private final By screen = PlatformLocator.of(
            androidIdWithText("loginTV", "Login"),
            AppiumBy.iOSNsPredicateString(
                    "type == 'XCUIElementTypeStaticText' AND label == 'Select a username from the list below'"));
    // GAP-WORKAROUND (iOS): no accessibilityIdentifier, suggested "Login-button"
    private final By loginButton = PlatformLocator.of(
            AppiumBy.accessibilityId("Tap to login with given credentials"),
            AppiumBy.iOSNsPredicateString("type == 'XCUIElementTypeButton' AND label == 'Login'"));

    public LoginPage() {
        waitUntilLoaded();
    }

    @Override
    protected List<By> loadedIndicators() {
        return List.of(screen, loginButton);
    }

    @Step("Log in as {user.username}")
    public CatalogPage logIn(User user) {
        submitSavedUser(user);
        return new CatalogPage();
    }

    @Step("Log in as {user.username} to continue checkout")
    public ShippingAddressPage logInToCheckout(User user) {
        submitSavedUser(user);
        return new ShippingAddressPage();
    }

    @Step("Submit username \"{username}\" and password")
    public LoginPage submitCredentials(String username, String password) {
        type(credentialField("nameET", "username"), username);
        type(credentialField("passwordET", "password"), password);
        click(loginButton);
        return this;
    }

    /**
     * Reads the validation messages shown after a submit and dismisses them (iOS alert).
     */
    public List<String> validationMessages() {
        List<String> messages = validation.read();
        validation.dismiss();
        return messages;
    }

    @Step("Open catalog")
    public CatalogPage openCatalog() {
        navigationMenu.openCatalog();
        return new CatalogPage();
    }

    private void submitSavedUser(User user) {
        click(PlatformLocator.of(
                androidSelector("new UiSelector().resourceIdMatches(\".*:id/username[0-9]TV\").text(\""
                        + user.username() + "\")"),
                AppiumBy.accessibilityId(user.username())));
        click(loginButton);
    }

    private By credentialField(String androidId, String name) {
        // GAP (iOS): the username/password fields have no identifier, label, value or placeholder,
        // so only their type is left, which CLAUDE.md §4.1 does not allow on its own.
        By field = PlatformLocator.of(androidId(androidId), null);
        if (field == null) {
            throw new TestabilityGapException("no reliable locator for the iOS login " + name
                    + " field (no identifier, label or placeholder; suggested \"" + name + "-input\")");
        }
        return field;
    }
}
