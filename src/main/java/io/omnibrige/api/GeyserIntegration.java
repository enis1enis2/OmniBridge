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

public class GeyserIntegration {

    private final Plugin geyserPlugin;

    public GeyserIntegration(Plugin hostPlugin) {
        this.geyserPlugin = Bukkit.getPluginManager().getPlugin("Geyser-Spigot");
    }

    public boolean isGeyserAvailable() {
        return geyserPlugin != null && geyserPlugin.isEnabled();
    }

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
