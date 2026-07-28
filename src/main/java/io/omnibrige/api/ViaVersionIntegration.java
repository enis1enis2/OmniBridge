/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * OmniBridge - Universal cross-version and cross-platform connectivity
 * Copyright (c) 2026 OmniBridge Contributors
 */

package io.omnibrige.api;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

/**
 * Integration layer for detecting client protocol versions via ViaVersion.
 * Uses reflection to interact with the ViaVersion API without a compile-time dependency.
 */
public class ViaVersionIntegration {

    private static final Map<Integer, String> PROTOCOL_VERSIONS = new HashMap<>();

    static {
        PROTOCOL_VERSIONS.put(4, "1.7.2");
        PROTOCOL_VERSIONS.put(5, "1.7.4-1.7.5");
        PROTOCOL_VERSIONS.put(47, "1.8.x");
        PROTOCOL_VERSIONS.put(107, "1.9");
        PROTOCOL_VERSIONS.put(108, "1.9.1");
        PROTOCOL_VERSIONS.put(109, "1.9.2");
        PROTOCOL_VERSIONS.put(110, "1.9.4");
        PROTOCOL_VERSIONS.put(210, "1.10.x");
        PROTOCOL_VERSIONS.put(315, "1.11");
        PROTOCOL_VERSIONS.put(316, "1.11.2");
        PROTOCOL_VERSIONS.put(335, "1.12");
        PROTOCOL_VERSIONS.put(336, "1.12.1");
        PROTOCOL_VERSIONS.put(338, "1.12.2");
        PROTOCOL_VERSIONS.put(340, "1.13");
        PROTOCOL_VERSIONS.put(341, "1.13.1");
        PROTOCOL_VERSIONS.put(342, "1.13.2");
        PROTOCOL_VERSIONS.put(393, "1.14");
        PROTOCOL_VERSIONS.put(401, "1.14.1");
        PROTOCOL_VERSIONS.put(404, "1.14.2-1.14.4");
        PROTOCOL_VERSIONS.put(477, "1.15");
        PROTOCOL_VERSIONS.put(480, "1.15.1");
        PROTOCOL_VERSIONS.put(485, "1.15.2");
        PROTOCOL_VERSIONS.put(554, "1.16");
        PROTOCOL_VERSIONS.put(560, "1.16.1");
        PROTOCOL_VERSIONS.put(566, "1.16.2");
        PROTOCOL_VERSIONS.put(567, "1.16.3");
        PROTOCOL_VERSIONS.put(573, "1.16.4-1.16.5");
        PROTOCOL_VERSIONS.put(735, "1.17");
        PROTOCOL_VERSIONS.put(736, "1.17.1");
        PROTOCOL_VERSIONS.put(754, "1.18-1.18.1");
        PROTOCOL_VERSIONS.put(756, "1.18.2");
        PROTOCOL_VERSIONS.put(757, "1.19");
        PROTOCOL_VERSIONS.put(758, "1.19.1-1.19.2");
        PROTOCOL_VERSIONS.put(759, "1.19.3");
        PROTOCOL_VERSIONS.put(760, "1.19.4");
        PROTOCOL_VERSIONS.put(761, "1.20");
        PROTOCOL_VERSIONS.put(762, "1.20.1");
        PROTOCOL_VERSIONS.put(763, "1.20.2");
        PROTOCOL_VERSIONS.put(764, "1.20.3-1.20.4");
        PROTOCOL_VERSIONS.put(765, "1.20.5-1.20.6");
        PROTOCOL_VERSIONS.put(766, "1.21-1.21.1");
        PROTOCOL_VERSIONS.put(767, "1.21.2-1.21.3");
        PROTOCOL_VERSIONS.put(768, "1.21.4");
        PROTOCOL_VERSIONS.put(769, "1.21.5");
    }

    private final Plugin viaVersionPlugin;

    /**
     * Constructs a ViaVersionIntegration instance.
     *
     * @param hostPlugin the host plugin providing the Bukkit runtime context
     */
    public ViaVersionIntegration(Plugin hostPlugin) {
        this.viaVersionPlugin = Bukkit.getPluginManager().getPlugin("ViaVersion");
    }

    /**
     * Checks if the ViaVersion plugin is present and enabled.
     *
     * @return true if ViaVersion is available for use
     */
    public boolean isAvailable() {
        return viaVersionPlugin != null && viaVersionPlugin.isEnabled();
    }

    /**
     * Returns the protocol version strings for all connected players.
     *
     * @return a map of player UUIDs to their Minecraft version strings
     */
    public Map<UUID, String> getConnectedVersions() {
        Map<UUID, String> versions = new HashMap<>();
        if (!isAvailable()) return versions;

        for (Player player : Bukkit.getOnlinePlayers()) {
            String version = getPlayerVersion(player.getUniqueId());
            if (version != null) {
                versions.put(player.getUniqueId(), version);
            }
        }
        return versions;
    }

    /**
     * Returns the Minecraft version string for a specific player.
     *
     * @param uuid the player's UUID
     * @return the version string (e.g. "1.21.4"), or null if ViaVersion unavailable
     */
    public String getPlayerVersion(UUID uuid) {
        if (!isAvailable()) return null;
        try {
            Class<?> viaApiClass = Class.forName("com.viaversion.viaversion.api.Via");
            Object viaApi = viaApiClass.getMethod("getAPI").invoke(null);
            Object connection = viaApi.getClass().getMethod("getConnection", UUID.class).invoke(viaApi, uuid);
            if (connection != null) {
                Object protocolVersion = connection.getClass().getMethod("getProtocolVersion").invoke(connection);
                int protocol = (int) protocolVersion;
                return PROTOCOL_VERSIONS.getOrDefault(protocol, "Unknown (" + protocol + ")");
            }
        } catch (Exception ignored) {}
        return null;
    }

    /**
     * Returns all Minecraft version strings supported by the ViaVersion protocol map.
     *
     * @return an immutable list of version strings
     */
    public List<String> getSupportedVersions() {
        return List.copyOf(PROTOCOL_VERSIONS.values());
    }
}
