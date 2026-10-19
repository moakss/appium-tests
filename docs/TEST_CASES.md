## Summary

| ID | Title | Pri | Platforms | Group |
|---|---|---|---|---|
| TC-01 | App launches to Catalog | P0 | Both | smoke |
| TC-02 | Open product details from catalog | P0 | Both | smoke |
| TC-03 | Add product to cart updates badge and cart | P0 | Both | smoke |
| TC-04 | End-to-end checkout happy path | P0 | Both | smoke |
| TC-05 | Login with valid user from menu | P0 | Both | smoke |
| TC-06 | Checkout requires login when logged out | P1 | Both | regression |
| TC-07 | Login validation – empty username / password | P1 | Both | regression |
| TC-08 | Logout | P1 | Both | regression |
| TC-09 | Product quantity stepper and Add To Cart disabled at 0 | P1 | Both | regression |
| TC-10 | Cart: change quantity, remove item, totals | P1 | Both | regression |
| TC-11 | Empty cart state and Go Shopping | P1 | Both | regression |
| TC-12 | Catalog sorting (name / price, asc / desc) | P1 | Both | regression |
| TC-13 | Shipping address validation | P1 | Both | regression |
| TC-14 | Payment validation | P1 | Both | regression |
| TC-15 | Checkout with different billing address | P2 | Both | regression |
| TC-16 | Same product + same colour merges in cart; different colours are separate lines | P2 | Both | regression |

---

## P0 – Smoke

### TC-01 App launches to Catalog
- **Precondition:** app installed; fresh launch.
- **Steps:** launch app.
- **Expected:** Catalog screen loaded (Android: `productTV` = "Products" and `productRV` present; iOS: `Catalog-screen`), the Backpack is among the visible product names; cart badge empty. (iOS does not expose catalog prices, so prices are checked on the details page.)

### TC-02 Open product details from catalog
- **Precondition:** on Catalog, default sort.
- **Steps:** tap the Backpack (Android "Sauce Labs Backpack" — tap the product *image*, title is not clickable; iOS "Sauce Labs Backpack - Black").
- **Expected:** Product Details loaded; product name shown and price $29.99 (from test data); quantity = 1; Add To Cart enabled.

### TC-03 Add product to cart updates badge and cart
- **Precondition:** empty cart (established in setup).
- **Steps:** open Backpack → Add To Cart → open cart.
- **Expected:** cart badge = 1; cart contains 1 Backpack line, qty 1, $29.99; 1 item; total $29.99.

### TC-04 End-to-end checkout happy path critical
- **Precondition:** logged out, empty cart.
- **Steps:**
  1. Add Backpack (qty 1) to cart → Proceed To Checkout.
  2. Login with valid user.
  3. Fill shipping address (full name, address 1, city, zip, country) → To Payment.
  4. Fill card (name, number, expiry, CVV), billing same as shipping → Review Order.
  5. Verify review: item, shipping name/address, card, delivery $5.99, total = $29.99 + $5.99 = **$35.98** → Place Order.
  6. Continue Shopping.
- **Expected:** "Checkout Complete" shown; Continue Shopping returns to Catalog; cart is empty.
- **Acceptance:** requires real-device verification on both platforms.

### TC-05 Login with valid user from menu
- **Precondition:** logged out.
- **Steps:** open menu (Android drawer / iOS More) → Log In → enter valid user → Login.
- **Expected:** returns to Catalog; menu now shows Log Out.

## P1 – Regression

### TC-06 Checkout requires login when logged out
- **Steps:** logged out, cart with 1 item → Proceed To Checkout.
- **Expected:** Login screen shown (not Shipping). After valid login → Shipping Address screen.

### TC-07 Login validation
- **Data-driven:** (empty user, valid pwd) and (valid user, empty pwd).
- **Expected:** stays on Login.
  - Android: "Username is required" (`nameErrorTV`) / "Enter Password" (`passwordErrorTV`).
  - iOS: alert "Validation Error!" with "Username is required" / "Password is required".

### TC-08 Logout
- **Precondition:** logged in (established in setup).
- **Steps:** menu → Log Out (Android: confirm "LOGOUT" in dialog).
- **Expected:** Login screen shown; menu shows Log In; checkout requires login again.

### TC-09 Product quantity stepper
- **Steps:** product details → plus ×2 → expect 3 → minus ×3 → expect 0.
- **Expected:** quantity reflects taps; at 0 Add To Cart is disabled (iOS: minus disabled too). Adding at qty 2 puts 2 items in cart.

### TC-10 Cart manipulation
- **Precondition (set up in the test via UI, then verified):** cart with Sauce Labs Backpack ×1 and Sauce Labs Fleece Jacket ×1:
  1. Default sort (Name ascending) → open **Sauce Labs Backpack** (position 1) → Add To Cart.
  2. Catalog → sort **Price descending** → open **Sauce Labs Fleece Jacket** (now position 1) → Add To Cart.
  3. Verify cart badge = 2.
- **Why the sort (workaround):** on Android the app crashes when a product after the 6th catalog position is opened (bug already reported). In default name order the Fleece Jacket is far below position 6; sorting by price descending moves it to the top because it is the most expensive product. The same steps run on iOS to keep one cross-platform flow. The test code must carry a comment explaining this; remove the workaround when the crash is fixed.
- **Steps:** open cart → increase Backpack to 2 → verify totals → remove Fleece Jacket → verify totals.
- **Expected:** item count and total recalculate after each action ($29.99×2 + $49.99 = $109.97, then $59.98).
- **Emulator check (2026-10-18, manual via adb, Android):** Price descending puts Fleece Jacket at position 1; opening it and adding to cart works (badge 1). iOS sort order not yet checked.

### TC-11 Empty cart state
- **Steps:** open cart with no items (or remove the last item) → Go Shopping.
- **Expected:** "No Items" empty state; Go Shopping returns to Catalog.

### TC-12 Catalog sorting
- **Data-driven:** Name asc, Name desc, Price asc, Price desc.
- **Expected:** visible product list is ordered accordingly (compare names / numeric prices of visible cards, scrolling via UiScrollable / scroll gesture if needed).

### TC-13 Shipping address validation
- **Precondition (set up in the test via UI, then verified):** cart with 1 item (Backpack), Proceed To Checkout, logged in with valid user → Shipping Address page loaded.
- **Data-driven:** a complete valid address with exactly one mandatory field left empty (full name, address 1, city, zip, country).
- **Steps:** fill the form → tap To Payment.
- **Expected:** stays on Shipping; message "Please provide your full name." / "…address." / "…city." / "…zip" / "…country". Address line 2 and state are optional (empty → proceeds to Payment).

### TC-14 Payment validation
- **Precondition (set up in the test via UI, then verified):** cart with 1 item (Backpack) → Proceed To Checkout → login with valid user → fill a **valid** shipping address → To Payment → Payment page loaded. ("Empty" in this test means an empty field on the payment *form*, the cart is never empty — Proceed To Checkout is not available with an empty cart.)
- **Data-driven:** a complete valid card with exactly one field left empty: card holder name, card number, expiration date, security code.
- **Steps:** fill the form → tap Review Order.
- **Expected:** validation error is shown and the user stays on Payment (Review page not reached).
  - Android: error text "Value looks invalid." shown under the empty input.
  - iOS: error shown ("Validation Error!" alert), user cannot continue.
- **Observed manually (user, 2026-10-18):** behaves as expected on both platforms — no known bug.
  Exact error text/element per field and the security-code case on iOS still to be confirmed in Appium Inspector.

## P2 – Extended

### TC-15 Different billing address
- **Steps:** as TC-04 but uncheck "billing address same as shipping" and fill billing fields.
- **Expected:** order completes; review shows correct data.

### TC-16 Cart line merging
- **Steps:** add Backpack (colour A) twice, then Backpack (colour B).
- **Expected:** colour A line qty 2; colour B separate line qty 1.
