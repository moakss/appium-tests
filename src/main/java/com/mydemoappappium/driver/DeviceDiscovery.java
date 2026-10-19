package com.mydemoappappium.driver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mydemoappappium.config.Config;
import com.mydemoappappium.config.Platform;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Finds the running Android devices/emulators ({@code adb devices}) and booted iOS simulators
 * ({@code xcrun simctl}), so no device has to be configured. Ports are assigned from the configured base ports.
 */
final class DeviceDiscovery {

    private static final long COMMAND_TIMEOUT_SECONDS = 30;

    private DeviceDiscovery() {
    }

    static List<Device> detect(Platform platform) {
        List<Map.Entry<String, String>> found = switch (platform) {
            case ANDROID -> androidDevices();
            case IOS -> bootedSimulators();
        };
        Config config = Config.get();
        List<Device> devices = new ArrayList<>();
        for (Map.Entry<String, String> udidAndName : found) {
            int offset = devices.size();
            devices.add(new Device(udidAndName.getValue(), udidAndName.getKey(),
                    config.driverPortBase() + offset, config.mjpegPortBase() + offset));
        }
        return devices;
    }

    private static List<Map.Entry<String, String>> androidDevices() {
        String adb = adb();
        List<Map.Entry<String, String>> devices = new ArrayList<>();
        for (String line : run(adb, "devices").split("\\R")) {
            String[] columns = line.trim().split("\\s+");
            // Skips the header and devices that are offline or unauthorized.
            if (columns.length == 2 && columns[1].equals("device")) {
                String model = run(adb, "-s", columns[0], "shell", "getprop", "ro.product.model").trim();
                devices.add(Map.entry(columns[0], model.isEmpty() ? columns[0] : model));
            }
        }
        return devices;
    }

    private static List<Map.Entry<String, String>> bootedSimulators() {
        JsonNode runtimes;
        try {
            runtimes = new ObjectMapper().readTree(run("xcrun", "simctl", "list", "devices", "booted", "-j")).path("devices");
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot parse the simulator list of xcrun simctl", e);
        }
        List<Map.Entry<String, String>> devices = new ArrayList<>();
        runtimes.properties().forEach(runtime -> {
            // e.g. com.apple.CoreSimulator.SimRuntime.iOS-27-0 -> iOS 27.0
            String os = runtime.getKey().substring(runtime.getKey().lastIndexOf('.') + 1).replaceFirst("-", " ").replace('-', '.');
            runtime.getValue().forEach(simulator ->
                    devices.add(Map.entry(simulator.get("udid").asText(), simulator.get("name").asText() + " (" + os + ")")));
        });
        return devices;
    }

    private static String adb() {
        Path adb = Config.get().androidSdk().resolve("platform-tools").resolve("adb");
        return Files.isExecutable(adb) ? adb.toString() : "adb";
    }

    private static String run(String... command) {
        try {
            Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            if (!process.waitFor(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS) || process.exitValue() != 0) {
                process.destroyForcibly();
                throw new IllegalStateException("Device detection failed: '" + String.join(" ", command) + "'\n" + output);
            }
            return output;
        } catch (IOException e) {
            throw new IllegalStateException("Device detection failed: cannot run '" + command[0] + "'", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Device detection interrupted", e);
        }
    }
}
