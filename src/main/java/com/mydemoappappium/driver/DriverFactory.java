package com.mydemoappappium.driver;

import com.mydemoappappium.config.Config;
import com.mydemoappappium.config.Platform;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;

/**
 * The only place where Appium sessions are created and capabilities are configured.
 */
public final class DriverFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DriverFactory.class);

    private DriverFactory() {
    }

    public static AppiumDriver create(Device device, Map<String, Object> capabilityOverrides) {
        Config config = Config.get();
        AppiumDriver driver = switch (config.platform()) {
            case ANDROID -> new AndroidDriver(config.appiumUrl(), androidOptions(config, device, capabilityOverrides));
            case IOS -> new IOSDriver(config.appiumUrl(), iosOptions(config, device, capabilityOverrides));
        };
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        LOG.info("Session {} started on {} ({})", driver.getSessionId(), device.name(), device.udid());
        if (config.platform() == Platform.IOS) {
            try {
                verifySimulatorLocale(driver, config.simulatorExpectedLocale(), device);
            } catch (RuntimeException e) {
                // The caller never receives this driver, so it is quit here or it would hold WebDriverAgent.
                try {
                    driver.quit();
                } catch (RuntimeException quitFailure) {
                    e.addSuppressed(quitFailure);
                }
                throw e;
            }
        }
        return driver;
    }

    private static void verifySimulatorLocale(AppiumDriver driver, String expected, Device device) {
        Object info = driver.executeScript("mobile: deviceInfo", Map.of());
        Object actual = info instanceof Map<?, ?> map ? map.get("currentLocale") : null;
        if (!expected.equals(actual)) {
            throw new IllegalStateException("Simulator " + device.name() + " has locale '" + actual + "', expected '"
                    + expected + "'. Set it in the simulator: Settings > General > Language & Region.");
        }
    }

    private static UiAutomator2Options androidOptions(Config config, Device device, Map<String, Object> overrides) {
        UiAutomator2Options options = new UiAutomator2Options()
                .setUdid(device.udid())
                .setSystemPort(device.driverPort())
                .setMjpegServerPort(device.mjpegServerPort())
                .setAppPackage(config.appId())
                .setAppWaitActivity("*")
                .setNoReset(config.noReset())
                .setNewCommandTimeout(config.newCommandTimeout())
                .setUiautomator2ServerLaunchTimeout(config.launchTimeout());
        config.appActivity().ifPresent(options::setAppActivity);
        config.appPath().ifPresent(path -> options.setApp(path.toString()));
        overrides.forEach(options::setCapability);
        return options;
    }

    private static XCUITestOptions iosOptions(Config config, Device device, Map<String, Object> overrides) {
        XCUITestOptions options = new XCUITestOptions()
                .setUdid(device.udid())
                .setWdaLocalPort(device.driverPort())
                .setMjpegServerPort(device.mjpegServerPort())
                .setBundleId(config.appId())
                .setNoReset(config.noReset())
                .setNewCommandTimeout(config.newCommandTimeout())
                .setWdaLaunchTimeout(config.launchTimeout())
                .setWdaStartupRetries(config.wdaStartupRetries())
                .setWdaStartupRetryInterval(config.wdaStartupRetryInterval())
                .setConnectHardwareKeyboard(config.simulatorConnectHardwareKeyboard())
                .setForceSimulatorSoftwareKeyboardPresence(!config.simulatorConnectHardwareKeyboard());
        config.appPath().ifPresent(path -> options.setApp(path.toString()));
        overrides.forEach(options::setCapability);
        return options;
    }
}
