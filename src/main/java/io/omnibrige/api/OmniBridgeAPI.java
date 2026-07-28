/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * OmniBridge - Universal cross-version and cross-platform connectivity
 * Copyright (c) 2026 OmniBridge Contributors
 */

package io.omnibrige.api;

import io.omnibrige.OmniBridge;
import io.omnibrige.core.PlatformDetector;
import io.omnibrige.download.Repository;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.*;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * Public API for interacting with OmniBridge from external plugins.
 * Provides plugin management, player platform info, and installation operations.
 */
public final class OmniBridgeAPI {

    private static OmniBridgeAPI instance;

    private final OmniBridge plugin;
    private final ViaVersionIntegration viaVersion;
    private final GeyserIntegration geyser;
    private final Logger logger;

    OmniBridgeAPI(OmniBridge plugin) {
        this.plugin = plugin;
        this.viaVersion = plugin.getViaVersionIntegration();
        this.geyser = plugin.getGeyserIntegration();
        this.logger = plugin.getLogger();
    }

    /**
     * Initializes the API singleton. Called internally during plugin startup.
     *
     * @param plugin the OmniBridge plugin instance
     */
    public static void init(OmniBridge plugin) {
        instance = new OmniBridgeAPI(plugin);
    }

    /** Shuts down the API singleton. Called internally during plugin disable. */
    public static void shutdown() {
        instance = null;
    }

    /**
     * Returns the singleton API instance.
     *
     * @return the OmniBridgeAPI instance, or null if not initialized
     */
    public static OmniBridgeAPI getInstance() {
        return instance;
    }

    /**
     * Checks whether the API is available for use.
     *
     * @return true if the API singleton has been initialized
     */
    public static boolean isAvailable() {
        return instance != null;
    }

    /**
     * Returns the detected server platform type.
     *
     * @return the current platform enum value
     */
    public PlatformDetector.Platform getPlatform() {
        return plugin.getPlatform();
    }

    /**
     * Returns all known managed plugins with their current status.
     *
     * @return an unmodifiable map of plugin keys to their status information
     */
    public Map<String, ManagedPlugin> getManagedPlugins() {
        Map<String, ManagedPlugin> result = new LinkedHashMap<>();
        for (var entry : Repository.getAllPlugins().entrySet()) {
            String key = entry.getKey();
            Repository.PluginInfo info = entry.getValue();
            Plugin bukkitPlugin = Bukkit.getPluginManager().getPlugin(Repository.getBukkitName(key));
            File[] files = getPluginsDirectory().listFiles();
            boolean installed = bukkitPlugin != null || (files != null
                    && Arrays.stream(files).anyMatch(f -> f.getName().equals(Repository.getJarName(key))));
            boolean enabled = bukkitPlugin != null && bukkitPlugin.isEnabled();
            String version = bukkitPlugin != null ? bukkitPlugin.getDescription().getVersion() : "N/A";
            List<String> deps = Repository.getDependencies(key);
            result.put(key, new ManagedPlugin(
                    key, info.displayName(), info.type(),
                    installed, enabled, version, List.copyOf(deps)));
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Returns the status of a specific managed plugin.
     *
     * @param name the plugin key (case-insensitive)
     * @return the plugin's status, or null if not found
     */
    public ManagedPlugin getPlugin(String name) {
        if (name == null) return null;
        Repository.PluginInfo info = Repository.getAllPlugins().get(name.toLowerCase(Locale.ROOT));
        if (info == null) return null;
        Plugin bukkitPlugin = Bukkit.getPluginManager().getPlugin(Repository.getBukkitName(name));
        boolean installed = bukkitPlugin != null || new File(getPluginsDirectory(), Repository.getJarName(name)).exists();
        boolean enabled = bukkitPlugin != null && bukkitPlugin.isEnabled();
        String version = bukkitPlugin != null ? bukkitPlugin.getDescription().getVersion() : "N/A";
        List<String> deps = Repository.getDependencies(name);
        return new ManagedPlugin(name, info.displayName(), info.type(),
                installed, enabled, version, List.copyOf(deps));
    }

    /**
     * Returns platform information for a specific player.
     *
     * @param uuid the player's UUID
     * @return the player's platform info, or null if offline
     */
    public PlayerPlatformInfo getPlayerInfo(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return null;
        return buildPlayerInfo(player);
    }

    /**
     * Returns platform information for all online players.
     *
     * @return an unmodifiable collection of player platform info
     */
    public Collection<PlayerPlatformInfo> getOnlinePlayerInfo() {
        List<PlayerPlatformInfo> result = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            result.add(buildPlayerInfo(player));
        }
        return Collections.unmodifiableList(result);
    }

    private PlayerPlatformInfo buildPlayerInfo(Player player) {
        UUID uuid = player.getUniqueId();
        String protocolVersion = viaVersion.isAvailable() ? viaVersion.getPlayerVersion(uuid) : null;
        boolean bedrock = geyser.isBedrockPlayer(uuid);
        String bedrockPlatform = bedrock ? geyser.getPlayerPlatform(uuid) : null;
        return new PlayerPlatformInfo(uuid, player.getName(), protocolVersion, bedrock, bedrockPlatform);
    }

    /**
     * Asynchronously installs a plugin by name.
     *
     * @param name the plugin key (case-insensitive)
     * @return a future that completes with true if installation succeeded
     */
    public CompletableFuture<Boolean> installPlugin(String name) {
        return CompletableFuture.supplyAsync(() -> {
            if (!Repository.isKnown(name)) return false;
            String url = Repository.getUrl(name);
            File dest = new File(getPluginsDirectory(), Repository.getJarName(name));
            io.omnibrige.download.DownloadService ds = new io.omnibrige.download.DownloadService(logger);
            boolean ok = ds.downloadSync(url, dest);
            if (ok) {
                plugin.getConfigManager().generateConfig(name);
                logger.info("API: Installed " + Repository.getDisplayName(name));
            }
            return ok;
        });
    }

    /**
     * Asynchronously updates a plugin by name.
     *
     * @param name the plugin key (case-insensitive)
     * @return a future that completes with true if the update succeeded
     */
    public CompletableFuture<Boolean> updatePlugin(String name) {
        return CompletableFuture.supplyAsync(() -> plugin.getPluginManager().updatePlugin(name));
    }

    /**
     * Removes a managed plugin by deleting its JAR file.
     *
     * @param name the plugin key (case-insensitive)
     */
    public void removePlugin(String name) {
        plugin.getPluginManager().removePlugin(name);
    }

    private File getPluginsDirectory() {
        return plugin.getDataFolder().getParentFile();
    }
}
