package com.mydemoappappium.core;

import com.mydemoappappium.config.Config;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;

/**
 * A reusable UI part, optionally scoped to a root element. Used by pages only, never by tests.
 */
public abstract class BaseComponent extends UiObject {

    private final By root;

    /**
     * For components whose elements have no common container with an identifier.
     */
    protected BaseComponent() {
        this(null);
    }

    protected BaseComponent(By root) {
        this.root = root;
    }

    @Override
    protected SearchContext context() {
        return root == null ? driver() : Waits.visible(driver(), root, Config.get().defaultTimeout());
    }
}
