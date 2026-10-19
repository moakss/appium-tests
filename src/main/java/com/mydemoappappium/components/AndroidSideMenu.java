package com.mydemoappappium.components;

import com.mydemoappappium.core.BaseComponent;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

/**
 * Locators VERIFIED (Appium live search, 2026-10-18, emulator-5554).
 */
class AndroidSideMenu extends BaseComponent implements NavigationMenu {

    private final By menuButton = AppiumBy.accessibilityId("View menu");
    // Menu rows share the id itemTV; Catalog has no content-desc, so it is matched by id + text.
    private final By catalogItem = androidIdWithText("itemTV", "Catalog");
    private final By loginItem = AppiumBy.accessibilityId("Login Menu Item");
    private final By logoutItem = AppiumBy.accessibilityId("Logout Menu Item");
    private final By confirmLogout = androidSelector(
            "new UiSelector().resourceId(\"android:id/button1\").text(\"LOGOUT\")");

    @Override
    public void openLogin() {
        click(menuButton);
        click(loginItem);
    }

    @Override
    public void logOut() {
        click(menuButton);
        click(logoutItem);
        click(confirmLogout);
    }

    @Override
    public void openCatalog() {
        click(menuButton);
        click(catalogItem);
    }
}
