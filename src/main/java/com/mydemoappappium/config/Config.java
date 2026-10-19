package com.mydemoappappium.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Immutable run configuration, resolved once: system properties > platform properties > defaults.
 */
public final class Config {

    private static final Config INSTANCE = load();

    private final Platform platform;
    private final Map<String, String> values;

    private Config(Platform platform, Map<String, String> values) {
        this.platform = platform;
        this.values = values;
    }

    public static Config get() {
        return INSTANCE;
    }

    public Platform platform() {
        return platform;
    }

    public String appiumHost() {
        return required("appium.host");
    }

    public int appiumPort() {
        return Integer.parseInt(required("appium.port"));
    }

    public URL appiumUrl() {
        try {
            return URI.create("http://" + appiumHost() + ":" + appiumPort()).toURL();
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid Appium server address", e);
        }
    }

    public Duration serverStartTimeout() {
        return seconds("appium.server.startTimeoutSeconds");
    }

    public Optional<Path> androidHome() {
        return optional("appium.env.androidHome").map(Config::expandHome);
    }

    /**
     * Configured Android SDK, else ANDROID_HOME, else the Android Studio default location.
     */
    public Path androidSdk() {
        return androidHome()
                .or(() -> Optional.ofNullable(System.getenv("ANDROID_HOME")).filter(value -> !value.isBlank()).map(Path::of))
                .orElse(Path.of(System.getProperty("user.home"), "Library", "Android", "sdk"));
    }

    public Optional<Path> javaHome() {
        return optional("appium.env.javaHome").map(Config::expandHome);
    }

    public Duration defaultTimeout() {
        return seconds("timeout.defaultSeconds");
    }

    public Duration launchTimeout() {
        return seconds("timeout.launchSeconds");
    }

    public Duration polling() {
        return Duration.ofMillis(Long.parseLong(required("timeout.pollingMillis")));
    }

    public Duration newCommandTimeout() {
        return seconds("session.newCommandTimeoutSeconds");
    }

    public boolean noReset() {
        return Boolean.parseBoolean(required("session.noReset"));
    }

    public Optional<String> devicesFile() {
        return optional("devices.file");
    }

    public int driverPortBase() {
        return Integer.parseInt(required("device.driverPortBase"));
    }

    public int mjpegPortBase() {
        return Integer.parseInt(required("device.mjpegPortBase"));
    }

    public String appId() {
        return required("app.id");
    }

    public Optional<String> appActivity() {
        return optional("app.activity");
    }

    public Optional<Path> appPath() {
        return optional("app.path").map(Config::expandHome);
    }

    public boolean simulatorConnectHardwareKeyboard() {
        return Boolean.parseBoolean(required("simulator.connectHardwareKeyboard"));
    }

    public String simulatorExpectedLocale() {
        return required("simulator.expectedLocale");
    }

    public int wdaStartupRetries() {
        return Integer.parseInt(required("wda.startupRetries"));
    }

    public Duration wdaStartupRetryInterval() {
        return seconds("wda.startupRetryIntervalSeconds");
    }

    private Duration seconds(String key) {
        return Duration.ofSeconds(Long.parseLong(required(key)));
    }

    private String required(String key) {
        return optional(key).orElseThrow(() -> new IllegalStateException(
                "Missing configuration '" + key + "' for platform " + platform.id()));
    }

    private Optional<String> optional(String key) {
        return Optional.ofNullable(values.get(key)).filter(value -> !value.isBlank());
    }

    private static Path expandHome(String path) {
        return Path.of(path.startsWith("~") ? System.getProperty("user.home") + path.substring(1) : path);
    }

    private static Config load() {
        Platform platform = Platform.from(System.getProperty("platform", Platform.ANDROID.id()));
        Properties merged = new Properties();
        merged.putAll(read("config/default.properties"));
        merged.putAll(read("config/" + platform.id() + ".properties"));
        // Only keys defined in the property files are overridable; unrelated JVM properties are ignored.
        for (String key : merged.stringPropertyNames()) {
            String override = System.getProperty(key);
            if (override != null) {
                merged.setProperty(key, override);
            }
        }
        Map<String, String> values = new HashMap<>();
        merged.stringPropertyNames().forEach(key -> values.put(key, merged.getProperty(key).trim()));
        return new Config(platform, Map.copyOf(values));
    }

    private static Properties read(String resource) {
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("Configuration file not found on classpath: " + resource);
            }
            Properties properties = new Properties();
            properties.load(in);
            return properties;
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + resource, e);
        }
    }
}
