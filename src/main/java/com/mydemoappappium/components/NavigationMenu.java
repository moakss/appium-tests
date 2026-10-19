package com.mydemoappappium.components;

import com.mydemoappappium.core.PlatformLocator;

/**
 * App navigation that differs structurally: Android side drawer vs iOS "More" screen and tab bar.
 */
public interface NavigationMenu {

    void openLogin();

    void logOut();

    void openCatalog();

    static NavigationMenu create() {
        return PlatformLocator.of((NavigationMenu) new AndroidSideMenu(), new IosMoreMenu());
    }
}
