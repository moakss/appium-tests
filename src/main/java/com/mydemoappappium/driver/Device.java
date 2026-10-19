package com.mydemoappappium.driver;

/**
 * A detected device or one from the devices file. {@code driverPort} is the UiAutomator2 systemPort (Android)
 * or the WebDriverAgent wdaLocalPort (iOS).
 */
public record Device(String name, String udid, int driverPort, int mjpegServerPort) {
}
