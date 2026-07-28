/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * OmniBridge - Universal cross-version and cross-platform connectivity
 * Copyright (c) 2026 OmniBridge Contributors
 */

package io.omnibrige.core;

/**
 * Detects the server platform (Paper, Spigot, Velocity, Fabric) at runtime
 * using class presence checks.
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

    private PlatformDetector() {}

    /**
     * Detects the current server platform by probing for platform-specific classes.
     *
     * @return the detected Platform, or UNKNOWN if unrecognized
     */
    public static Platform detect() {
        try {
            Class.forName("com.velocitypowered.api.proxy.ProxyServer");
            return Platform.VELOCITY;
        } catch (ClassNotFoundException ignored) {}

        try {
            Class.forName("io.papermc.paper.plugin.lifecycle.LifecycleEventManager");
            return Platform.PAPER;
        } catch (ClassNotFoundException ignored) {}

        try {
            Class.forName("net.fabricmc.loader.api.FabricLoader");
            return Platform.FABRIC;
        } catch (ClassNotFoundException ignored) {}

        try {
            Class.forName("org.bukkit.craftbukkit.CraftServer");
            return Platform.SPIGOT;
        } catch (ClassNotFoundException ignored) {}

        try {
            Class.forName("org.bukkit.Bukkit");
            return Platform.SPIGOT;
        } catch (ClassNotFoundException ignored) {}

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
