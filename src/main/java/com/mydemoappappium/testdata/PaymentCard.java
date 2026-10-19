package com.mydemoappappium.testdata;

/**
 * Empty strings mean "leave the field empty".
 */
public record PaymentCard(String holder, String number, String expiry, String securityCode) {

    public PaymentCard with(CardField field, String value) {
        return new PaymentCard(
                field == CardField.HOLDER ? value : holder,
                field == CardField.NUMBER ? value : number,
                field == CardField.EXPIRY ? value : expiry,
                field == CardField.SECURITY_CODE ? value : securityCode);
    }

    public String lastFourDigits() {
        return number.substring(number.length() - 4);
    }
}
