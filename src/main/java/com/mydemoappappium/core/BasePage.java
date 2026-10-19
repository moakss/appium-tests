package com.mydemoappappium.core;

import com.mydemoappappium.config.Config;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;

import java.time.Duration;
import java.util.List;

/**
 * Subclasses call {@link #waitUntilLoaded()} at the end of their constructor, after their locator
 * fields are initialized.
 */
public abstract class BasePage extends UiObject {

    /**
     * Elements that together uniquely identify this screen.
     */
    protected abstract List<By> loadedIndicators();

    protected final void waitUntilLoaded() {
        waitUntilLoaded(Config.get().defaultTimeout());
    }

    protected final void waitUntilLoaded(Duration timeout) {
        try {
            loadedIndicators().forEach(locator -> visible(locator, timeout));
        } catch (TimeoutException e) {
            // A crash also shows up as a timeout; report it as a crash instead.
            AppStateGuard.ensureInForeground();
            throw e;
        }
        AppStateGuard.ensureInForeground();
    }

    public boolean isDisplayed() {
        return loadedIndicators().stream().allMatch(this::isVisible);
    }
}
