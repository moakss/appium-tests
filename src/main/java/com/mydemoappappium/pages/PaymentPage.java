package com.mydemoappappium.pages;

import com.mydemoappappium.components.AddressForm;
import com.mydemoappappium.components.ValidationMessages;
import com.mydemoappappium.core.BasePage;
import com.mydemoappappium.core.PlatformLocator;
import com.mydemoappappium.core.Waits;
import com.mydemoappappium.testdata.Address;
import com.mydemoappappium.testdata.PaymentCard;
import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

import java.util.List;

/**
 * Locators VERIFIED (Appium live search, 2026-10-18, emulator-5554 / iPhone 17 iOS 27.0).
 */
public class PaymentPage extends BasePage {

    private static final String IOS_SCREEN = "Payment-screen";

    private final AddressForm billingForm = new AddressForm(IOS_SCREEN);
    private final ValidationMessages validation = ValidationMessages.create();

    private final By screen = PlatformLocator.of(
            androidIdWithText("enterPaymentMethodTV", "Enter a payment method"),
            AppiumBy.accessibilityId(IOS_SCREEN));
    // GAP-WORKAROUND (iOS): no accessibilityIdentifier, suggested "ReviewOrder-button"
    private final By reviewOrderButton = PlatformLocator.of(
            AppiumBy.accessibilityId("Saves payment info and launches screen to review checkout data"),
            AppiumBy.iOSNsPredicateString("type == 'XCUIElementTypeButton' AND label == 'Review Order'"));
    // GAP-WORKAROUND (iOS): no accessibilityIdentifier, suggested "BillingSame-checkbox"; named after its image
    private final By billingSameCheckbox = PlatformLocator.of(
            androidId("billingAddressCB"),
            iosInScreen(IOS_SCREEN, "XCUIElementTypeButton[`name BEGINSWITH 'CheckBox'`]"));
    // GAP-WORKAROUND (iOS): card fields have no accessibilityIdentifier; matched by placeholder
    private final By holderField = cardField("nameET", "Maxim Winter");
    private final By numberField = cardField("cardNumberET", "3258 1265 7568 7896");
    private final By expiryField = cardField("expirationDateET", "03/25");
    private final By securityCodeField = cardField("securityCodeET", "123");

    public PaymentPage() {
        waitUntilLoaded();
    }

    @Override
    protected List<By> loadedIndicators() {
        return List.of(screen);
    }

    @Step("Enter card and review order")
    public ReviewOrderPage reviewOrder(PaymentCard card) {
        submit(card);
        return new ReviewOrderPage();
    }

    @Step("Enter card and a different billing address, then review order")
    public ReviewOrderPage reviewOrder(PaymentCard card, Address billingAddress) {
        enterCard(card);
        if (isBillingSameAsShipping()) {
            clickEnabled(billingSameCheckbox);
            Waits.until(driver -> !isBillingSameAsShipping(), "billing address checkbox to be unchecked");
        }
        billingForm.fill(billingAddress);
        click(reviewOrderButton);
        return new ReviewOrderPage();
    }

    @Step("Submit card")
    public PaymentPage submit(PaymentCard card) {
        enterCard(card);
        click(reviewOrderButton);
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

    private void enterCard(PaymentCard card) {
        type(holderField, card.holder());
        type(numberField, card.number());
        type(expiryField, card.expiry());
        type(securityCodeField, card.securityCode());
    }

    private boolean isBillingSameAsShipping() {
        // Android exposes the CheckBox "checked" state; the iOS image button reports value "1" when selected.
        String state = Waits.enabled(driver(), billingSameCheckbox).getAttribute(PlatformLocator.of("checked", "value"));
        return PlatformLocator.of("true", "1").equals(state);
    }

    private static By cardField(String androidId, String iosPlaceholder) {
        return PlatformLocator.of(
                androidId(androidId),
                iosInScreen(IOS_SCREEN, "XCUIElementTypeTextField[`placeholderValue == '" + iosPlaceholder + "'`]"));
    }
}
