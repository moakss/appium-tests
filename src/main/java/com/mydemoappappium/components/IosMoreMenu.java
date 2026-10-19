package com.mydemoappappium.components;

import com.mydemoappappium.core.BaseComponent;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

/**
 * Locators VERIFIED (Appium live search, 2026-10-18, iPhone 17 iOS 27.0).
 * The same menu item logs in or out depending on the state; logging out has no confirmation.
 */
class IosMoreMenu extends BaseComponent implements NavigationMenu {

    private final By moreTab = AppiumBy.accessibilityId("More-tab-item");
    private final By catalogTab = AppiumBy.accessibilityId("Catalog-tab-item");
    private final By loginLogoutItem = AppiumBy.accessibilityId("LogOut-menu-item");

    @Override
    public void openLogin() {
        click(moreTab);
        click(loginLogoutItem);
    }

    @Override
    public void logOut() {
        click(moreTab);
        click(loginLogoutItem);
    }

    @Override
    public void openCatalog() {
        click(catalogTab);
    }
}
