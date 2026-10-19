package com.mydemoappappium.driver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mydemoappappium.config.Config;
import com.mydemoappappium.config.Platform;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Hands out each device to at most one session at a time. Devices come from the devices file when one is
 * configured, otherwise the running emulators/simulators are detected.
 */
public final class DevicePool {

    private static final Map<String, List<Device>> DEVICES = Config.get().devicesFile()
            .map(DevicePool::load)
            .orElseGet(() -> detected(Config.get().platform()));
    private static final Set<String> IN_USE = new HashSet<>();

    private DevicePool() {
    }

    public static synchronized Device acquire(Platform platform) {
        List<Device> devices = DEVICES.getOrDefault(platform.id(), List.of());
        if (devices.isEmpty()) {
            throw new IllegalStateException("No " + platform.id() + " device found. "
                    + (Config.get().devicesFile().isPresent()
                    ? "Add one to " + Config.get().devicesFile().get()
                    : "Start an Android emulator / boot an iOS simulator, or set devices.file"));
        }
        Device device = devices.stream()
                .filter(candidate -> !IN_USE.contains(candidate.udid()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No free " + platform.id() + " device: " + devices.size() + " available, all in use. "
                                + "Lower the thread count or start more devices"));
        IN_USE.add(device.udid());
        return device;
    }

    public static synchronized void release(Device device) {
        IN_USE.remove(device.udid());
    }

    private static Map<String, List<Device>> detected(Platform platform) {
        List<Device> devices = DeviceDiscovery.detect(platform);
        validateUnique(Map.of(platform.id(), devices));
        return Map.of(platform.id(), devices);
    }

    private static Map<String, List<Device>> load(String resource) {
        try (InputStream in = DevicePool.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("Device file not found on classpath: " + resource);
            }
            Map<String, List<Device>> devices = new ObjectMapper().readValue(in, new TypeReference<>() {
            });
            validateUnique(devices);
            return Map.copyOf(devices);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + resource, e);
        }
    }

    private static void validateUnique(Map<String, List<Device>> devices) {
        Set<String> udids = new HashSet<>();
        Set<Integer> ports = new HashSet<>();
        devices.values().stream().flatMap(List::stream).forEach(device -> {
            if (!udids.add(device.udid())) {
                throw new IllegalStateException("Duplicate device UDID in devices file: " + device.udid());
            }
            Stream.of(device.driverPort(), device.mjpegServerPort()).forEach(port -> {
                if (!ports.add(port)) {
                    throw new IllegalStateException("Duplicate port " + port + " in devices file (" + device.udid() + ")");
                }
            });
        });
    }
}
