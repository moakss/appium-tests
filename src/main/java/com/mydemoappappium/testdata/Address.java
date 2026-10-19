package com.mydemoappappium.testdata;

/**
 * Empty strings mean "leave the field empty".
 */
public record Address(String fullName, String address1, String address2, String city, String state, String zip,
                      String country) {

    public Address with(AddressField field, String value) {
        return new Address(
                field == AddressField.FULL_NAME ? value : fullName,
                field == AddressField.ADDRESS_1 ? value : address1,
                field == AddressField.ADDRESS_2 ? value : address2,
                field == AddressField.CITY ? value : city,
                field == AddressField.STATE ? value : state,
                field == AddressField.ZIP ? value : zip,
                field == AddressField.COUNTRY ? value : country);
    }
}
