package com.mydemoappappium.listeners;

import com.mydemoappappium.config.Config;
import com.mydemoappappium.core.TestabilityGapException;
import com.mydemoappappium.driver.Device;
import com.mydemoappappium.driver.DevicePool;
import com.mydemoappappium.driver.DriverFactory;
import com.mydemoappappium.driver.DriverHolder;
import io.appium.java_client.AppiumDriver;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

import java.util.Map;

/**
 * Starts a new Appium session (with screen recording) before every test method and always ends it afterwards
 * (CLAUDE.md §7). Failure artifacts are collected before the session ends (§8). Tests stopped by a missing
 * reliable locator are reported as skipped (blocked), not failed.
 */
public class SessionListener implements IInvokedMethodListener {

    private final ArtifactCollector artifacts = new ArtifactCollector();

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult result) {
        if (!method.isTestMethod()) {
            return;
        }
        Device device = DevicePool.acquire(Config.get().platform());
        AppiumDriver driver;
        try {
            driver = DriverFactory.create(device, Map.of());
        } catch (RuntimeException e) {
            DevicePool.release(device);
            throw e;
        }
        DriverHolder.set(new DriverHolder.Session(driver, device));
        artifacts.startRecording(driver);
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult result) {
        if (!method.isTestMethod()) {
            return;
        }
        if (result.getThrowable() instanceof TestabilityGapException) {
            result.setStatus(ITestResult.SKIP);
        }
        try {
            DriverHolder.current().ifPresent(session -> {
                if (result.getStatus() == ITestResult.FAILURE) {
                    artifacts.collect(result, session);
                } else {
                    artifacts.discardRecording(session.driver());
                }
            });
        } finally {
            DriverHolder.end();
        }
    }
}
