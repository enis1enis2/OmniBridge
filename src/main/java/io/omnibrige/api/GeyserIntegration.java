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
 *
 * <p>Uses reflection against Geyser's public interfaces ({@code GeyserApi},
 * {@code GeyserConnection}) so that the integration does not depend on Geyser's
 * implementation classes, which are relocated between builds.</p>
 */
public class GeyserIntegration {

    private static final String GEYSER_API_CLASS = "org.geysermc.geyser.api.GeyserApi";
    private static final String GEYSER_CONNECTION_INTERFACE =
            "org.geysermc.geyser.api.connection.GeyserConnection";

    /** Plugin names used by the different Geyser platform builds. */
    private static final List<String> GEYSER_PLUGIN_NAMES = List.of(
            "Geyser-Spigot", "Geyser-Paper", "Geyser", "Geyser-Velocity", "Geyser-BungeeCord");

    private final Plugin geyserPlugin;

    /**
     * Constructs a GeyserIntegration instance.
     *
     * @param hostPlugin the host plugin providing the Bukkit runtime context
     */
    public GeyserIntegration(Plugin hostPlugin) {
        this.geyserPlugin = findGeyserPlugin();
    }

    private static Plugin findGeyserPlugin() {
        for (String name : GEYSER_PLUGIN_NAMES) {
            Plugin candidate = Bukkit.getPluginManager().getPlugin(name);
            if (candidate != null) return candidate;
        }
        return null;
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
        return getConnection(uuid) != null;
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
        Object connection = getConnection(uuid);
        if (connection == null) return null;
        try {
            Class<?> connectionInterface = Class.forName(GEYSER_CONNECTION_INTERFACE);
            Object platform = connectionInterface.getMethod("platform").invoke(connection);
            return platform != null ? platform.toString() : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private Object getConnection(UUID uuid) {
        if (!isGeyserAvailable() || uuid == null) return null;
        try {
            Class<?> geyserApiClass = Class.forName(GEYSER_API_CLASS);
            Object geyserApi = geyserApiClass.getMethod("api").invoke(null);
            return geyserApiClass
                    .getMethod("connectionByUuid", UUID.class)
                    .invoke(geyserApi, uuid);
        } catch (Exception ignored) {
            return null;
        }
    }
}
