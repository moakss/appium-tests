package com.mydemoappappium.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mydemoappappium.config.Config;
import com.mydemoappappium.config.Platform;
import com.mydemoappappium.driver.DriverHolder;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.screenrecording.CanRecordScreen;
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.logging.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Failure evidence (CLAUDE.md §8). Every artifact is attempted independently and never throws, so the
 * original test failure is never replaced or masked. Missing artifacts are reported with the reason.
 */
final class ArtifactCollector {

    static final String SERVER_LOG_ATTRIBUTE = "appium.server.log";

    private static final Logger LOG = LoggerFactory.getLogger(ArtifactCollector.class);
    private static final Path ROOT = Path.of("target", "artifacts");
    private static final ObjectMapper JSON = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private final ThreadLocal<String> recordingNotStarted = new ThreadLocal<>();

    void startRecording(AppiumDriver driver) {
        recordingNotStarted.remove();
        try {
            ((CanRecordScreen) driver).startRecordingScreen();
        } catch (RuntimeException e) {
            recordingNotStarted.set(firstLine(e.getMessage()));
            LOG.warn("Screen recording not started: {}", e.getMessage());
        }
    }

    /**
     * Passing and skipped tests keep nothing; the recording is stopped and dropped.
     */
    void discardRecording(AppiumDriver driver) {
        recordingNotStarted.remove();
        try {
            ((CanRecordScreen) driver).stopRecordingScreen();
        } catch (RuntimeException e) {
            LOG.debug("No recording to discard: {}", e.getMessage());
        }
    }

    void collect(ITestResult result, DriverHolder.Session session) {
        AppiumDriver driver = session.driver();
        Platform platform = Config.get().platform();
        String sessionId = String.valueOf(driver.getSessionId());
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS"));
        Path dir = ROOT.resolve(result.getTestClass().getRealClass().getSimpleName() + "." + result.getMethod().getMethodName());
        String prefix = String.join("_", result.getMethod().getMethodName(), platform.id(),
                session.device().udid(), sessionId.substring(0, Math.min(8, sessionId.length())), timestamp);

        Map<String, String> status = new LinkedHashMap<>();
        attempt(status, "screenshot", () -> save(dir, prefix + "_screenshot.png", "image/png",
                driver.getScreenshotAs(OutputType.BYTES)));
        attempt(status, "screen recording", () -> save(dir, prefix + "_recording.mp4", "video/mp4", recording(driver)));
        attempt(status, "page source", () -> save(dir, prefix + "_page-source.xml", "text/xml",
                driver.getPageSource().getBytes(StandardCharsets.UTF_8)));
        String logType = platform == Platform.ANDROID ? "logcat" : "syslog";
        attempt(status, "device log (" + logType + ")", () -> save(dir, prefix + "_" + logType + ".txt", "text/plain",
                driver.manage().logs().get(logType).getAll().stream()
                        .map(LogEntry::toString)
                        .collect(Collectors.joining(System.lineSeparator()))
                        .getBytes(StandardCharsets.UTF_8)));
        Object serverLog = result.getTestContext().getSuite().getAttribute(SERVER_LOG_ATTRIBUTE);
        attempt(status, "session info", () -> save(dir, prefix + "_session-info.json", "application/json",
                JSON.writeValueAsBytes(sessionInfo(result, session, sessionId, serverLog))));
        status.put("appium server log", serverLog == null ? "missing: suite listener did not run" : String.valueOf(serverLog));

        String summary = status.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(System.lineSeparator()));
        attempt(new LinkedHashMap<>(), "summary", () -> save(dir, prefix + "_artifacts.txt", "text/plain",
                summary.getBytes(StandardCharsets.UTF_8)));
        LOG.info("Failure artifacts for {} in {}:{}{}", result.getName(), dir.toAbsolutePath(),
                System.lineSeparator(), summary);
    }

    private byte[] recording(AppiumDriver driver) {
        String notStarted = recordingNotStarted.get();
        recordingNotStarted.remove();
        if (notStarted != null) {
            throw new IllegalStateException("recording was not started: " + notStarted);
        }
        byte[] video = Base64.getMimeDecoder().decode(((CanRecordScreen) driver).stopRecordingScreen());
        if (video.length == 0) {
            throw new IllegalStateException("the driver returned an empty recording");
        }
        return video;
    }

    private static Map<String, Object> sessionInfo(ITestResult result, DriverHolder.Session session, String sessionId,
                                                   Object serverLog) {
        Config config = Config.get();
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("test", result.getTestClass().getName() + "." + result.getMethod().getMethodName());
        info.put("parameters", result.getParameters());
        info.put("failure", String.valueOf(result.getThrowable()));
        info.put("platform", config.platform().id());
        info.put("deviceName", session.device().name());
        info.put("udid", session.device().udid());
        info.put("platformVersion", session.driver().getCapabilities().getCapability("platformVersion"));
        info.put("appId", config.appId());
        info.put("sessionId", sessionId);
        info.put("appiumServer", config.appiumUrl().toString());
        info.put("appiumServerLog", String.valueOf(serverLog));
        info.put("capabilities", session.driver().getCapabilities().asMap());
        return info;
    }

    private static void attempt(Map<String, String> status, String name, ArtifactWriter writer) {
        try {
            status.put(name, "saved " + writer.write().toAbsolutePath());
        } catch (Exception e) {
            status.put(name, "missing: " + e.getClass().getSimpleName() + " - " + firstLine(e.getMessage()));
        }
    }

    private static Path save(Path dir, String fileName, String mimeType, byte[] content) throws IOException {
        Files.createDirectories(dir);
        Path file = Files.write(dir.resolve(fileName), content);
        try {
            Allure.addAttachment(fileName, mimeType, new ByteArrayInputStream(content),
                    fileName.substring(fileName.lastIndexOf('.') + 1));
        } catch (RuntimeException e) {
            LOG.warn("Could not attach {} to Allure: {}", fileName, e.getMessage());
        }
        return file;
    }

    private static String firstLine(String message) {
        return message == null ? "" : message.lines().findFirst().orElse("");
    }

    @FunctionalInterface
    private interface ArtifactWriter {
        Path write() throws Exception;
    }
}
