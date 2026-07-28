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
 * Integration layer for detecting and querying Geyser Bedrock Edition players.
 * Uses reflection to interact with the Geyser API without a compile-time dependency.
 */
public class GeyserIntegration {

    private final Plugin geyserPlugin;

    /**
     * Constructs a GeyserIntegration instance.
     *
     * @param hostPlugin the host plugin providing the Bukkit runtime context
     */
    public GeyserIntegration(Plugin hostPlugin) {
        this.geyserPlugin = Bukkit.getPluginManager().getPlugin("Geyser-Spigot");
    }

    /**
     * Checks if the Geyser plugin is present and enabled.
     *
     * @return true if Geyser is available for use
     */
    public boolean isGeyserAvailable() {
        return geyserPlugin != null && geyserPlugin.isEnabled();
    }

    /**
     * Checks whether a player is connected via Bedrock Edition through Geyser.
     *
     * @param uuid the player's UUID
     * @return true if the player is a Bedrock client
     */
    public boolean isBedrockPlayer(UUID uuid) {
        if (!isGeyserAvailable()) return false;
        try {
            Class<?> geyserApiClass = Class.forName("org.geysermc.geyser.api.GeyserApi");
            Object geyserApi = geyserApiClass.getMethod("api").invoke(null);
            Object connection = geyserApi.getClass().getMethod("connectionByUuid", UUID.class).invoke(geyserApi, uuid);
            return connection != null;
        } catch (Exception ignored) {}
        return false;
    }

    /**
     * Returns the UUIDs of all online Bedrock players.
     *
     * @return a set of UUIDs for Bedrock-connected players
     */
    public Set<UUID> getBedrockPlayers() {
        Set<UUID> bedrockPlayers = new HashSet<>();
        if (!isGeyserAvailable()) return bedrockPlayers;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isBedrockPlayer(player.getUniqueId())) {
                bedrockPlayers.add(player.getUniqueId());
            }
        }
        return bedrockPlayers;
    }

    /**
     * Returns the device platform string for a Bedrock player (e.g. "WINDOWS", "ANDROID").
     *
     * @param uuid the player's UUID
     * @return the platform string, or null if not a Bedrock player or Geyser unavailable
     */
    public String getPlayerPlatform(UUID uuid) {
        if (!isGeyserAvailable()) return null;
        try {
            Class<?> geyserApiClass = Class.forName("org.geysermc.geyser.api.GeyserApi");
            Object geyserApi = geyserApiClass.getMethod("api").invoke(null);
            Object connection = geyserApi.getClass().getMethod("connectionByUuid", UUID.class).invoke(geyserApi, uuid);
            if (connection != null) {
                Object platform = connection.getClass().getMethod("platform").invoke(connection);
                return platform.toString();
            }
        } catch (Exception ignored) {}
        return null;
    }
}
