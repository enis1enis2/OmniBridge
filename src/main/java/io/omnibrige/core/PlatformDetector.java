/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * OmniBridge - Universal cross-version and cross-platform connectivity
 * Copyright (c) 2026 OmniBridge Contributors
 */

package io.omnibrige.core;

/**
 * Detects the server platform at runtime using class presence checks.
 *
 * <p>The detection rules are exposed through {@link #detect(ClassProbe)} so they can
 * be unit tested without a running server.</p>
 */
public final class PlatformDetector {

    /** Represents the detected server platform types. */
    public enum Platform {
        PAPER,
        SPIGOT,
        VELOCITY,
        FABRIC,
        UNKNOWN
    }

    /** Probes whether a class is visible on the current class path. */
    @FunctionalInterface
    public interface ClassProbe {

        /**
         * Checks whether the given class is present.
         *
         * @param className the fully qualified class name
         * @return true when the class can be loaded
         */
        boolean isPresent(String className);
    }

    private static final String VELOCITY_MARKER = "com.velocitypowered.api.proxy.ProxyServer";
    private static final String PAPER_MARKER = "io.papermc.paper.plugin.lifecycle.LifecycleEventManager";
    private static final String PAPER_LEGACY_MARKER = "com.destroystokyo.paper.PaperConfig";
    private static final String FABRIC_MARKER = "net.fabricmc.loader.api.FabricLoader";
    private static final String CRAFTBUKKIT_MARKER = "org.bukkit.craftbukkit.CraftServer";
    private static final String BUKKIT_MARKER = "org.bukkit.Bukkit";

    private PlatformDetector() {}

    /**
     * Detects the current server platform by probing for platform-specific classes.
     *
     * @return the detected Platform, or UNKNOWN if unrecognized
     */
    public static Platform detect() {
        ClassLoader loader = PlatformDetector.class.getClassLoader();
        return detect(className -> {
            try {
                Class.forName(className, false, loader);
                return true;
            } catch (ClassNotFoundException | LinkageError e) {
                return false;
            }
        });
    }

    /**
     * Detects the platform using the supplied probe.
     *
     * <p>Velocity is checked first because a proxy may also expose Bukkit classes,
     * then Paper (which is Bukkit-compatible), then Fabric, then Spigot.</p>
     *
     * @param probe the class presence probe
     * @return the detected Platform, or UNKNOWN if no marker matched
     */
    public static Platform detect(ClassProbe probe) {
        if (probe.isPresent(VELOCITY_MARKER)) return Platform.VELOCITY;
        if (probe.isPresent(PAPER_MARKER) || probe.isPresent(PAPER_LEGACY_MARKER)) {
            return Platform.PAPER;
        }
        if (probe.isPresent(FABRIC_MARKER)) return Platform.FABRIC;
        if (probe.isPresent(CRAFTBUKKIT_MARKER) || probe.isPresent(BUKKIT_MARKER)) {
            return Platform.SPIGOT;
        }
        return Platform.UNKNOWN;
    }

    /**
     * Checks whether the given platform is Bukkit-based (Paper or Spigot).
     *
     * @param platform the platform to check
     * @return true if the platform is Paper or Spigot
     */
    public static boolean isBukkitBased(Platform platform) {
        return platform == Platform.PAPER || platform == Platform.SPIGOT;
    }

    /**
     * Checks whether the given platform is Velocity.
     *
     * @param platform the platform to check
     * @return true if the platform is Velocity
     */
    public static boolean isVelocity(Platform platform) {
        return platform == Platform.VELOCITY;
    }
}
