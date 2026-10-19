package com.mydemoappappium.driver;

import com.mydemoappappium.config.Config;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

/**
 * Starts one Appium server per run, or reuses a server that is already running (e.g. scripts/start-appium.sh).
 * A reused server is never stopped by the tests.
 */
public final class AppiumServerManager {

    private static final Logger LOG = LoggerFactory.getLogger(AppiumServerManager.class);
    private static final Path ARTIFACTS_DIR = Path.of("target", "artifacts");

    private AppiumDriverLocalService service;

    /**
     * @return the server log file, or empty when an already running server is reused (its log is its own)
     */
    public Optional<Path> start() {
        Config config = Config.get();
        if (isRunning(config)) {
            LOG.info("Reusing Appium server at {} (its log is not captured by this run)", config.appiumUrl());
            return Optional.empty();
        }
        Path logFile = ARTIFACTS_DIR.resolve("appium-server_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".log");
        createDirectories(ARTIFACTS_DIR);
        service = AppiumDriverLocalService.buildService(new AppiumServiceBuilder()
                .withIPAddress(config.appiumHost())
                .usingPort(config.appiumPort())
                .withEnvironment(environment(config))
                .withArgument(GeneralServerFlag.LOG_TIMESTAMP)
                .withLogFile(logFile.toFile())
                .withTimeout(config.serverStartTimeout()));
        service.start();
        LOG.info("Appium server started at {}, log: {}", config.appiumUrl(), logFile.toAbsolutePath());
        return Optional.of(logFile.toAbsolutePath());
    }

    public void stop() {
        if (service != null && service.isRunning()) {
            service.stop();
            LOG.info("Appium server stopped");
        }
    }

    private static Map<String, String> environment(Config config) {
        Path androidHome = config.androidSdk();
        Path javaHome = config.javaHome()
                .or(() -> envPath("JAVA_HOME"))
                .orElse(Path.of(System.getProperty("java.home")));
        return Map.of("ANDROID_HOME", androidHome.toString(), "JAVA_HOME", javaHome.toString());
    }

    private static Optional<Path> envPath(String name) {
        return Optional.ofNullable(System.getenv(name)).filter(value -> !value.isBlank()).map(Path::of);
    }

    private static boolean isRunning(Config config) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(config.appiumUrl() + "/status"))
                .timeout(Duration.ofSeconds(2))
                .build();
        try (HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build()) {
            return client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode() == 200;
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private static void createDirectories(Path dir) {
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create " + dir, e);
        }
    }
}
