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

    public static void init(OmniBridge plugin) {
        instance = new OmniBridgeAPI(plugin);
    }

    public static void shutdown() {
        instance = null;
    }

    public static OmniBridgeAPI getInstance() {
        return instance;
    }

    public static boolean isAvailable() {
        return instance != null;
    }

    public PlatformDetector.Platform getPlatform() {
        return plugin.getPlatform();
    }

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

    public PlayerPlatformInfo getPlayerInfo(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return null;
        return buildPlayerInfo(player);
    }

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

    public CompletableFuture<Boolean> updatePlugin(String name) {
        return CompletableFuture.supplyAsync(() -> plugin.getPluginManager().updatePlugin(name));
    }

    public void removePlugin(String name) {
        plugin.getPluginManager().removePlugin(name);
    }

    private File getPluginsDirectory() {
        return plugin.getDataFolder().getParentFile();
    }
}
