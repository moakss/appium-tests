package com.mydemoappappium.components;

import com.mydemoappappium.core.BaseComponent;
import com.mydemoappappium.core.Gestures;
import com.mydemoappappium.core.PlatformLocator;
import com.mydemoappappium.testdata.Address;
import com.mydemoappappium.testdata.AddressField;
import org.openqa.selenium.By;

/**
 * Address fields shared by the shipping screen and the billing part of the payment screen.
 * Locators VERIFIED (Appium live search, 2026-10-18, emulator-5554 / iPhone 17 iOS 27.0).
 */
public class AddressForm extends BaseComponent {

    private final String iosScreenId;

    /**
     * @param iosScreenId the screen identifier the iOS fields are scoped to
     */
    public AddressForm(String iosScreenId) {
        this.iosScreenId = iosScreenId;
    }

    public void fill(Address address) {
        type(field(AddressField.FULL_NAME), address.fullName());
        type(field(AddressField.ADDRESS_1), address.address1());
        type(field(AddressField.ADDRESS_2), address.address2());
        type(field(AddressField.CITY), address.city());
        type(field(AddressField.STATE), address.state());
        type(field(AddressField.ZIP), address.zip());
        type(field(AddressField.COUNTRY), address.country());
    }

    private By field(AddressField field) {
        return switch (field) {
            case FULL_NAME -> field("fullNameET", "Rebecca Winter");
            case ADDRESS_1 -> field("address1ET", "Mandorley 112");
            case ADDRESS_2 -> field("address2ET", "Entrance 1");
            case CITY -> field("cityET", "Truro");
            case STATE -> field("stateET", "Cornwall");
            case ZIP -> field("zipET", "89750");
            case COUNTRY -> field("countryET", "United Kingdom");
        };
    }

    private By field(String androidId, String iosPlaceholder) {
        // Android: scrolled into view because billing fields start below the fold.
        // GAP-WORKAROUND (iOS): no accessibilityIdentifier, suggested "<Field>-input"; matched by placeholder.
        return PlatformLocator.of(
                Gestures.androidScrollIntoView("new UiSelector().resourceId(\"" + ANDROID_ID_PREFIX + androidId + "\")"),
                iosInScreen(iosScreenId, "XCUIElementTypeTextField[`placeholderValue == '" + iosPlaceholder + "'`]"));
    }
}
