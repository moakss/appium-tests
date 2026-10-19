package com.mydemoappappium.driver;

import io.appium.java_client.AppiumDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public final class DriverHolder {

    public record Session(AppiumDriver driver, Device device) {
    }

    private static final Logger LOG = LoggerFactory.getLogger(DriverHolder.class);
    private static final ThreadLocal<Session> SESSION = new ThreadLocal<>();

    private DriverHolder() {
    }

    public static void set(Session session) {
        SESSION.set(session);
    }

    public static AppiumDriver driver() {
        return current()
                .orElseThrow(() -> new IllegalStateException(
                        "No Appium session on thread " + Thread.currentThread().getName()))
                .driver();
    }

    public static Optional<Session> current() {
        return Optional.ofNullable(SESSION.get());
    }

    /**
     * Quits the session and frees its device; the holder is cleared even if quit() fails.
     */
    public static void end() {
        Session session = SESSION.get();
        if (session == null) {
            return;
        }
        try {
            session.driver().quit();
        } catch (RuntimeException e) {
            LOG.warn("Quitting session on {} failed", session.device().udid(), e);
        } finally {
            SESSION.remove();
            DevicePool.release(session.device());
        }
    }
}
