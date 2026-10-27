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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manages the installation, update, removal, and status of all managed plugins.
 * Handles dependency resolution and platform compatibility checks.
 */
public class PluginManager {

    /** Result of comparing an installed plugin against its repository entry. */
    public enum UpdateStatus {

        /** A newer build is available. */
        UPDATE_AVAILABLE,

        /** The installed build is current (or newer than the pinned one). */
        UP_TO_DATE,

        /** The version could not be determined (missing metadata or network failure). */
        UNKNOWN
    }

    /**
     * Represents the runtime status of a single managed plugin.
     *
     * @param name the display name of the plugin
     * @param installed whether the plugin JAR is present on the server
     * @param version the installed version string
     * @param enabled whether the plugin is currently loaded and enabled
     */
    public record PluginStatus(String name, boolean installed, String version, boolean enabled) {}

    private static final Pattern NUMERIC_VERSION = Pattern.compile("(\\d+(?:\\.\\d+)*)");

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
     * Asynchronously updates installed managed plugins.
     *
     * @param force when true, update even if the update check reports "up to date"
     *              or cannot determine a version
     * @param onComplete optional callback invoked on the main thread after updates
     */
    public void updateAllAsync(boolean force, Runnable onComplete) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            updateAll(force);
            if (onComplete != null) {
                Bukkit.getScheduler().runTask(plugin, onComplete);
            }
        });
    }

    /** Asynchronously updates installed managed plugins without forcing. */
    public void updateAllAsync(Runnable onComplete) {
        updateAllAsync(false, onComplete);
    }

    /** Installs all enabled managed plugins synchronously. */
    public void installAll() {
        List<String> sorted = sortPluginsByDependencies(getManagedPlugins());
        int installed = 0;

        for (String pluginName : sorted) {
            if (isPluginInstalled(pluginName)) continue;

            Repository.PluginInfo info = Repository.getPluginInfo(pluginName);
            if (info == null || info.url() == null) continue;

            File destination = Repository.resolveInstallPath(getPluginsDirectory(), pluginName);
            if (!downloadService.downloadSync(info.url(), destination)) continue;

            configManager.generateConfig(pluginName);
            logger.info("Installed " + info.displayName());
            fireEvent(new PluginInstalledEvent(pluginName, info.displayName(), info.type()));
            installed++;
        }
        logger.info("Installation complete: " + installed + " new plugin(s) downloaded.");
    }

    /** Updates all installed managed plugins synchronously without forcing. */
    public void updateAll() {
        updateAll(false);
    }

    /**
     * Updates all installed managed plugins synchronously.
     *
     * @param force when true, refresh every installed plugin even if the update
     *              check reports "up to date" or cannot determine a version
     */
    public void updateAll(boolean force) {
        for (String pluginName : getManagedPlugins()) {
            if (!isPluginInstalled(pluginName)) continue;

            UpdateStatus status = checkUpdate(pluginName);
            if (force || status == UpdateStatus.UPDATE_AVAILABLE) {
                updatePlugin(pluginName);
            } else if (status == UpdateStatus.UNKNOWN) {
                logger.fine("Skipping " + Repository.getDisplayName(pluginName)
                        + ": update status unknown (use /ob update to force)");
            }
        }
    }

    /**
     * Updates a single plugin to its latest version.
     *
     * <p>The download is validated and atomically moved into place, so an existing
     * JAR is never replaced by a partial or invalid file.</p>
     *
     * @param pluginName the internal plugin key
     * @return true if the update was successful, false otherwise
     */
    public boolean updatePlugin(String pluginName) {
        Repository.PluginInfo info = Repository.getPluginInfo(pluginName);
        if (info == null || info.url() == null) return false;

        File destination = Repository.resolveInstallPath(getPluginsDirectory(), pluginName);
        String oldVersion = getInstalledVersion(pluginName);
        if (!downloadService.downloadSync(info.url(), destination)) return false;

        configManager.generateConfig(pluginName);
        String newVersion = info.version() != null ? info.version() : "latest";
        logger.info("Updated " + info.displayName() + " to " + newVersion
                + " (restart or /ob reload to apply)");
        fireEvent(new PluginUpdatedEvent(pluginName, info.displayName(), info.type(),
                oldVersion, newVersion));
        return true;
    }

    /**
     * Compares an installed plugin against its repository entry.
     *
     * @param pluginName the internal plugin key
     * @return the update status, never null
     */
    public UpdateStatus checkUpdate(String pluginName) {
        Repository.PluginInfo info = Repository.getPluginInfo(pluginName);
        if (info == null || info.url() == null) return UpdateStatus.UNKNOWN;
        if (!isPluginInstalled(pluginName)) return UpdateStatus.UNKNOWN;

        if (info.version() != null) {
            String installed = getInstalledVersion(pluginName);
            if (installed == null || installed.isBlank() || "unknown".equalsIgnoreCase(installed)) {
                return UpdateStatus.UNKNOWN;
            }
            int comparison = compareVersions(installed, info.version());
            if (comparison == Integer.MIN_VALUE) return UpdateStatus.UNKNOWN;
            return comparison < 0 ? UpdateStatus.UPDATE_AVAILABLE : UpdateStatus.UP_TO_DATE;
        }

        // Rolling "latest" endpoint: compare the remote artifact timestamp with the local JAR.
        OptionalLong remote = downloadService.fetchLastModified(info.url());
        if (remote.isEmpty()) return UpdateStatus.UNKNOWN;
        File jar = Repository.resolveInstallPath(getPluginsDirectory(), pluginName);
        if (!jar.exists()) return UpdateStatus.UNKNOWN;
        return remote.getAsLong() > jar.lastModified()
                ? UpdateStatus.UPDATE_AVAILABLE
                : UpdateStatus.UP_TO_DATE;
    }

    /**
     * Removes a managed plugin by deleting its JAR file from the plugins directory.
     *
     * @param pluginName the internal plugin key
     * @return true if the JAR was found and deleted
     */
    public boolean removePlugin(String pluginName) {
        Repository.PluginInfo info = Repository.getPluginInfo(pluginName);
        if (info == null) return false;

        File jar = Repository.resolveInstallPath(getPluginsDirectory(), pluginName);
        if (!jar.exists()) return false;

        Plugin bukkitPlugin = Bukkit.getPluginManager().getPlugin(Repository.getBukkitName(pluginName));
        if (bukkitPlugin != null && bukkitPlugin.isEnabled()) {
            Bukkit.getPluginManager().disablePlugin(bukkitPlugin);
        }

        try {
            Files.deleteIfExists(jar.toPath());
        } catch (IOException e) {
            logger.warning("Could not delete " + jar.getName() + ": " + e.getMessage()
                    + " (stop the server and remove the file manually)");
            return false;
        }
        if (jar.exists()) {
            logger.warning("Could not delete " + jar.getName()
                    + " because the file is still locked (restart the server)");
            return false;
        }

        logger.info("Removed " + info.displayName());
        fireEvent(new PluginRemovedEvent(pluginName, info.displayName(), info.type()));
        return true;
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
            String version = installed ? getInstalledVersion(pluginName) : null;
            boolean enabled = installed && isPluginEnabled(pluginName);
            statusCache.put(pluginName, new PluginStatus(
                    Repository.getDisplayName(pluginName), installed,
                    version != null ? version : "unknown", enabled));
        }
        return Map.copyOf(statusCache);
    }

    /**
     * Returns the installed version of a specific plugin.
     *
     * @param pluginName the internal plugin key
     * @return the version string, or null if not installed or not exposed at runtime
     */
    public String getInstalledVersion(String pluginName) {
        if (!isPluginInstalled(pluginName)) return null;
        return readVersion(pluginName);
    }

    /** Reloads all enabled managed Bukkit plugins by disabling and re-enabling them. */
    public void reloadAll() {
        for (String pluginName : getManagedPlugins()) {
            if (Repository.getInstallTarget(pluginName) == Repository.InstallTarget.GEYSER_EXTENSIONS) {
                // Extensions are loaded by Geyser, not by the Bukkit plugin manager.
                continue;
            }
            Plugin bukkitPlugin = Bukkit.getPluginManager().getPlugin(Repository.getBukkitName(pluginName));
            if (bukkitPlugin != null && bukkitPlugin.isEnabled()) {
                Bukkit.getPluginManager().disablePlugin(bukkitPlugin);
                Bukkit.getPluginManager().enablePlugin(bukkitPlugin);
            }
        }
        logger.info("All managed plugins reloaded.");
    }

    private boolean isPluginInstalled(String pluginName) {
        if (Repository.getInstallTarget(pluginName) == Repository.InstallTarget.PLUGINS) {
            Plugin bukkitPlugin = Bukkit.getPluginManager().getPlugin(Repository.getBukkitName(pluginName));
            if (bukkitPlugin != null) return true;
        }
        return Repository.resolveInstallPath(getPluginsDirectory(), pluginName).exists();
    }

    private boolean isPluginEnabled(String pluginName) {
        Plugin bukkitPlugin = Bukkit.getPluginManager().getPlugin(Repository.getBukkitName(pluginName));
        return bukkitPlugin != null && bukkitPlugin.isEnabled();
    }

    private String readVersion(String pluginName) {
        Plugin bukkitPlugin = Bukkit.getPluginManager().getPlugin(Repository.getBukkitName(pluginName));
        return bukkitPlugin != null ? bukkitPlugin.getDescription().getVersion() : null;
    }

    private File getPluginsDirectory() {
        return plugin.getDataFolder().getParentFile();
    }

    private Set<String> getManagedPlugins() {
        Set<String> managed = new LinkedHashSet<>();
        var config = plugin.getConfig();
        var managedSection = config.getConfigurationSection("managed-plugins");
        if (managedSection != null) {
            for (String key : managedSection.getKeys(false)) {
                if (!Repository.isKnown(key)) {
                    logger.warning("Ignoring unknown managed-plugins key: " + key);
                    continue;
                }
                String normalized = key.toLowerCase(Locale.ROOT);
                if (managedSection.getBoolean(key) && isPluginCompatibleWithPlatform(normalized)) {
                    managed.add(normalized);
                }
            }
        }
        resolveDependencies(managed);
        return managed;
    }

    private void resolveDependencies(Set<String> managed) {
        Set<String> toAdd = new LinkedHashSet<>();
        boolean changed = true;
        while (changed) {
            changed = false;
            Set<String> current = new LinkedHashSet<>(managed);
            current.addAll(toAdd);
            for (String pluginName : current) {
                for (String dep : Repository.getDependencies(pluginName)) {
                    if (!managed.contains(dep) && !toAdd.contains(dep)
                            && isPluginCompatibleWithPlatform(dep)) {
                        logger.info("Auto-enabling " + Repository.getDisplayName(dep)
                                + " (required by " + Repository.getDisplayName(pluginName) + ")");
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

        for (String pluginName : plugins) {
            addWithDeps(pluginName, plugins, sorted, added);
        }
        return sorted;
    }

    private void addWithDeps(String pluginName, Set<String> all, List<String> sorted, Set<String> added) {
        if (added.contains(pluginName)) return;
        for (String dep : Repository.getDependencies(pluginName)) {
            if (all.contains(dep)) {
                addWithDeps(dep, all, sorted, added);
            }
        }
        sorted.add(pluginName);
        added.add(pluginName);
    }

    private void fireEvent(org.bukkit.event.Event event) {
        if (!plugin.isEnabled()) return;
        try {
            Bukkit.getScheduler().runTask(plugin,
                    () -> Bukkit.getPluginManager().callEvent(event));
        } catch (IllegalStateException e) {
            logger.fine("Skipping event dispatch during shutdown: " + e.getMessage());
        }
    }

    /**
     * Compares two version strings numerically, ignoring prefixes and suffixes.
     *
     * <p>Examples: {@code "5.11.1-SNAPSHOT+1046"} equals {@code "5.11.1"},
     * {@code "v1.30.5"} equals {@code "1.30.5"}, and {@code "1.20.4"} is older than
     * {@code "1.21"}.</p>
     *
     * @return a negative number when {@code installed} is older, zero when equal,
     *         a positive number when newer, or {@link Integer#MIN_VALUE} when either
     *         string contains no numeric version
     */
    static int compareVersions(String installed, String target) {
        int[] left = numericParts(installed);
        int[] right = numericParts(target);
        if (left == null || right == null) return Integer.MIN_VALUE;

        int length = Math.max(left.length, right.length);
        for (int i = 0; i < length; i++) {
            int a = i < left.length ? left[i] : 0;
            int b = i < right.length ? right[i] : 0;
            if (a != b) return Integer.compare(a, b);
        }
        return 0;
    }

    private static int[] numericParts(String version) {
        if (version == null) return null;
        Matcher matcher = NUMERIC_VERSION.matcher(version);
        if (!matcher.find()) return null;
        String[] parts = matcher.group(1).split("\\.");
        int[] result = new int[parts.length];
        try {
            for (int i = 0; i < parts.length; i++) {
                result[i] = Integer.parseInt(parts[i]);
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return result;
    }
}
