package com.mydemoappappium.pages;

import com.mydemoappappium.components.AddressForm;
import com.mydemoappappium.components.ValidationMessages;
import com.mydemoappappium.core.BasePage;
import com.mydemoappappium.core.PlatformLocator;
import com.mydemoappappium.testdata.Address;
import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

import java.util.List;

/**
 * Locators VERIFIED (Appium live search, 2026-10-18, emulator-5554 / iPhone 17 iOS 27.0).
 */
public class ShippingAddressPage extends BasePage {

    private final AddressForm addressForm = new AddressForm("ShippingAddress-screen");
    private final ValidationMessages validation = ValidationMessages.create();

    private final By screen = PlatformLocator.of(
            androidIdWithText("enterShippingAddressTV", "Enter a shipping address"),
            AppiumBy.accessibilityId("ShippingAddress-screen"));
    // GAP-WORKAROUND (iOS): no accessibilityIdentifier, suggested "ToPayment-button"
    private final By toPaymentButton = PlatformLocator.of(
            AppiumBy.accessibilityId("Saves user info for checkout"),
            AppiumBy.iOSNsPredicateString("type == 'XCUIElementTypeButton' AND label == 'To Payment'"));

    public ShippingAddressPage() {
        waitUntilLoaded();
    }

    @Override
    protected List<By> loadedIndicators() {
        return List.of(screen);
    }

    @Step("Enter shipping address and continue to payment")
    public PaymentPage continueToPayment(Address address) {
        submit(address);
        return new PaymentPage();
    }

    @Step("Submit shipping address")
    public ShippingAddressPage submit(Address address) {
        addressForm.fill(address);
        click(toPaymentButton);
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
}
