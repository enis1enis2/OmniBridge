/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * OmniBridge - Universal cross-version and cross-platform connectivity
 * Copyright (c) 2026 OmniBridge Contributors
 */

package io.omnibrige.core;

import io.omnibrige.OmniBridge;
import io.omnibrige.api.events.PluginInstalledEvent;
import io.omnibrige.api.events.PluginRemovedEvent;
import io.omnibrige.api.events.PluginUpdatedEvent;
import io.omnibrige.download.DownloadService;
import io.omnibrige.download.Repository;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.Locale;

/**
 * Manages the installation, update, removal, and status of all managed plugins.
 * Handles dependency resolution and platform compatibility checks.
 */
public class PluginManager {

    /**
     * Represents the runtime status of a single managed plugin.
     *
     * @param name the display name of the plugin
     * @param installed whether the plugin JAR is present on the server
     * @param version the installed version string
     * @param enabled whether the plugin is currently loaded and enabled
     */
    public record PluginStatus(String name, boolean installed, String version, boolean enabled) {}

    private final OmniBridge plugin;
    private final PlatformDetector.Platform platform;
    private final ConfigManager configManager;
    private final DownloadService downloadService;
    private final Logger logger;
    private final Map<String, PluginStatus> statusCache = new ConcurrentHashMap<>();

    /**
     * Constructs the plugin manager.
     *
     * @param plugin the OmniBridge plugin instance
     * @param platform the detected server platform
     * @param configManager the configuration manager for generating plugin configs
     */
    public PluginManager(OmniBridge plugin, PlatformDetector.Platform platform, ConfigManager configManager) {
        this.plugin = plugin;
        this.platform = platform;
        this.configManager = configManager;
        this.downloadService = new DownloadService(plugin.getLogger());
        this.logger = plugin.getLogger();
    }

    /**
     * Asynchronously installs all enabled managed plugins.
     *
     * @param onComplete optional callback invoked on the main thread after installation
     */
    public void installAllAsync(Runnable onComplete) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            installAll();
            if (onComplete != null) {
                Bukkit.getScheduler().runTask(plugin, onComplete);
            }
        });
    }

    /**
     * Asynchronously updates all installed managed plugins to their latest versions.
     *
     * @param onComplete optional callback invoked on the main thread after updates
     */
    public void updateAllAsync(Runnable onComplete) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            updateAll();
            if (onComplete != null) {
                Bukkit.getScheduler().runTask(plugin, onComplete);
            }
        });
    }

    /** Installs all enabled managed plugins synchronously. */
    public void installAll() {
        Set<String> managedPlugins = getManagedPlugins();
        List<String> sorted = sortPluginsByDependencies(managedPlugins);
        int installed = 0;

        for (String pluginName : sorted) {
            if (!isPluginInstalled(pluginName)) {
                boolean ok = downloadService.downloadSync(
                        Repository.getUrl(pluginName),
                        new File(getPluginsDirectory(), Repository.getJarName(pluginName)));
                if (ok) {
                    configManager.generateConfig(pluginName);
                    logger.info("Installed " + Repository.getDisplayName(pluginName));
                    Bukkit.getScheduler().runTask(plugin, () ->
                            Bukkit.getPluginManager().callEvent(new PluginInstalledEvent(
                                    pluginName, Repository.getDisplayName(pluginName),
                                    Repository.getType(pluginName))));
                    installed++;
                }
            }
        }
        logger.info("Installation complete: " + installed + " new plugin(s) downloaded.");
    }

    /** Updates all installed managed plugins synchronously. */
    public void updateAll() {
        Set<String> managedPlugins = getManagedPlugins();
        for (String pluginName : managedPlugins) {
            if (isPluginInstalled(pluginName)) {
                updatePlugin(pluginName);
            }
        }
    }

    /**
     * Updates a single plugin to its latest version, creating a backup before download.
     *
     * @param pluginName the internal plugin key
     * @return true if the update was successful, false otherwise
     */
    public boolean updatePlugin(String pluginName) {
        String url = Repository.getUrl(pluginName);
        if (url == null) return false;

        String oldVersion = getPluginVersion(pluginName);

        File pluginsDir = getPluginsDirectory();
        File destination = new File(pluginsDir, Repository.getJarName(pluginName));
        File backup = new File(pluginsDir, Repository.getJarName(pluginName) + ".backup");

        if (destination.exists()) {
            try {
                Files.copy(destination.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                logger.warning("Failed to backup " + pluginName + ": " + e.getMessage());
                return false;
            }
        }

        boolean downloaded = downloadService.downloadSync(url, destination);
        if (downloaded) {
            try { Files.deleteIfExists(backup.toPath()); } catch (IOException ignored) {}
            logger.info("Updated " + Repository.getDisplayName(pluginName)
                    + " (restart/reload to apply new version)");
            Bukkit.getScheduler().runTask(plugin, () ->
                    Bukkit.getPluginManager().callEvent(new PluginUpdatedEvent(
                            pluginName, Repository.getDisplayName(pluginName),
                            Repository.getType(pluginName), oldVersion, "pending")));
            return true;
        } else {
            if (backup.exists()) {
                try {
                    Files.move(backup.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    logger.info("Restored " + Repository.getDisplayName(pluginName) + " from backup");
                } catch (IOException e) {
                    logger.warning("Failed to restore " + pluginName + " from backup: " + e.getMessage());
                }
            }
            return false;
        }
    }

    /**
     * Removes a managed plugin by deleting its JAR file from the plugins directory.
     *
     * @param pluginName the internal plugin key
     * @return true if the JAR was found and deleted
     */
    public boolean removePlugin(String pluginName) {
        File jar = new File(getPluginsDirectory(), Repository.getJarName(pluginName));
        if (jar.exists()) {
            jar.delete();
            logger.info("Removed " + Repository.getDisplayName(pluginName));
            Bukkit.getScheduler().runTask(plugin, () ->
                    Bukkit.getPluginManager().callEvent(new PluginRemovedEvent(
                            pluginName, Repository.getDisplayName(pluginName),
                            Repository.getType(pluginName))));
            return true;
        }
        return false;
    }

    /**
     * Returns the status of all known managed plugins.
     *
     * @return an immutable map of plugin keys to their status
     */
    public Map<String, PluginStatus> getStatus() {
        statusCache.clear();
        for (String pluginName : Repository.getAllPlugins().keySet()) {
            boolean installed = isPluginInstalled(pluginName);
            String version = installed ? getPluginVersion(pluginName) : "N/A";
            boolean enabled = installed && isPluginEnabled(pluginName);
            statusCache.put(pluginName, new PluginStatus(
                    Repository.getDisplayName(pluginName), installed, version, enabled));
        }
        return Map.copyOf(statusCache);
    }

    /**
     * Returns the installed version of a specific plugin.
     *
     * @param pluginName the internal plugin key
     * @return the version string, or null if not installed
     */
    public String getInstalledVersion(String pluginName) {
        if (!isPluginInstalled(pluginName)) return null;
        return getPluginVersion(pluginName);
    }

    /** Reloads all enabled managed plugins by disabling and re-enabling them. */
    public void reloadAll() {
        for (String pluginName : getManagedPlugins()) {
            Plugin bukkitPlugin = Bukkit.getPluginManager().getPlugin(Repository.getBukkitName(pluginName));
            if (bukkitPlugin != null && bukkitPlugin.isEnabled()) {
                Bukkit.getPluginManager().disablePlugin(bukkitPlugin);
                Bukkit.getPluginManager().enablePlugin(bukkitPlugin);
            }
        }
        logger.info("All managed plugins reloaded.");
    }

    private boolean isPluginInstalled(String pluginName) {
        String bukkitName = Repository.getBukkitName(pluginName);
        Plugin p = Bukkit.getPluginManager().getPlugin(bukkitName);
        if (p != null) return true;
        File jar = new File(getPluginsDirectory(), Repository.getJarName(pluginName));
        return jar.exists();
    }

    private boolean isPluginEnabled(String pluginName) {
        Plugin p = Bukkit.getPluginManager().getPlugin(Repository.getBukkitName(pluginName));
        return p != null && p.isEnabled();
    }

    private String getPluginVersion(String pluginName) {
        Plugin p = Bukkit.getPluginManager().getPlugin(Repository.getBukkitName(pluginName));
        return p != null ? p.getDescription().getVersion() : "unknown";
    }

    private File getPluginsDirectory() {
        return plugin.getDataFolder().getParentFile();
    }

    private Set<String> getManagedPlugins() {
        Set<String> managed = new HashSet<>();
        var config = plugin.getConfig();
        var managedSection = config.getConfigurationSection("managed-plugins");
        if (managedSection != null) {
            for (String key : managedSection.getKeys(false)) {
                if (managedSection.getBoolean(key) && isPluginCompatibleWithPlatform(key)) {
                    managed.add(key);
                }
            }
        }
        resolveDependencies(managed);
        return managed;
    }

    private void resolveDependencies(Set<String> managed) {
        Set<String> toAdd = new HashSet<>();
        boolean changed = true;
        while (changed) {
            changed = false;
            Set<String> current = new HashSet<>(managed);
            current.addAll(toAdd);
            for (String plugin : current) {
                for (String dep : Repository.getDependencies(plugin)) {
                    if (!managed.contains(dep) && !toAdd.contains(dep) && isPluginCompatibleWithPlatform(dep)) {
                        logger.info("Auto-enabling " + Repository.getDisplayName(dep)
                                + " (required by " + Repository.getDisplayName(plugin) + ")");
                        toAdd.add(dep);
                        changed = true;
                    }
                }
            }
        }
        managed.addAll(toAdd);
    }

    private boolean isPluginCompatibleWithPlatform(String pluginName) {
        return switch (pluginName.toLowerCase(Locale.ROOT)) {
            case "viabungee" -> platform == PlatformDetector.Platform.VELOCITY;
            case "protocolib" -> PlatformDetector.isBukkitBased(platform);
            case "tuffxplus" -> PlatformDetector.isBukkitBased(platform);
            default -> true;
        };
    }

    private List<String> sortPluginsByDependencies(Set<String> plugins) {
        List<String> sorted = new ArrayList<>();
        Set<String> added = new HashSet<>();

        for (String plugin : plugins) {
            addWithDeps(plugin, plugins, sorted, added);
        }
        return sorted;
    }

    private void addWithDeps(String plugin, Set<String> all, List<String> sorted, Set<String> added) {
        if (added.contains(plugin)) return;
        for (String dep : Repository.getDependencies(plugin)) {
            if (all.contains(dep)) {
                addWithDeps(dep, all, sorted, added);
            }
        }
        sorted.add(plugin);
        added.add(plugin);
    }
}
