package com.mydemoappappium.core;

import com.mydemoappappium.config.Config;
import com.mydemoappappium.driver.DriverHolder;
import io.appium.java_client.InteractsWithApps;
import io.appium.java_client.appmanagement.ApplicationState;

public final class AppStateGuard {

    private AppStateGuard() {
    }

    public static void ensureInForeground() {
        String appId = Config.get().appId();
        ApplicationState state = ((InteractsWithApps) DriverHolder.driver()).queryAppState(appId);
        if (state != ApplicationState.RUNNING_IN_FOREGROUND) {
            throw new AppNotInForegroundException(appId, state);
        }
    }

    public static class AppNotInForegroundException extends RuntimeException {
        public AppNotInForegroundException(String appId, ApplicationState state) {
            super("App " + appId + " is not in the foreground (state: " + state + ") - it probably crashed");
        }
    }
}
