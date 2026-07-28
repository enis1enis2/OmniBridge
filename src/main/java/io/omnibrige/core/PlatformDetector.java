/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * OmniBridge - Universal cross-version and cross-platform connectivity
 * Copyright (c) 2026 OmniBridge Contributors
 */

package io.omnibrige.core;

public final class PlatformDetector {

    public enum Platform {
        PAPER,
        SPIGOT,
        VELOCITY,
        FABRIC,
        UNKNOWN
    }

    private PlatformDetector() {}

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

    public static boolean isBukkitBased(Platform platform) {
        return platform == Platform.PAPER || platform == Platform.SPIGOT;
    }

    public static boolean isVelocity(Platform platform) {
        return platform == Platform.VELOCITY;
    }
}
