package io.omnibrige.core;

import io.omnibrige.OmniBridge;
import io.omnibrige.download.DownloadService;
import io.omnibrige.download.Repository;
import io.omnibrige.download.Repository.PluginInfo;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class PluginManager {

    public record PluginStatus(String name, boolean installed, String version, boolean enabled) {}

    private final OmniBridge plugin;
    private final PlatformDetector.Platform platform;
    private final ConfigManager configManager;
    private final DownloadService downloadService;
    private final Logger logger;
    private final Map<String, PluginStatus> statusCache = new ConcurrentHashMap<>();

    public PluginManager(OmniBridge plugin, PlatformDetector.Platform platform, ConfigManager configManager) {
        this.plugin = plugin;
        this.platform = platform;
        this.configManager = configManager;
        this.downloadService = new DownloadService(plugin.getLogger());
        this.logger = plugin.getLogger();
    }

    public void installAllAsync(Runnable onComplete) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            installAll();
            if (onComplete != null) {
                Bukkit.getScheduler().runTask(plugin, onComplete);
            }
        });
    }

    public void updateAllAsync(Runnable onComplete) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            updateAll();
            if (onComplete != null) {
                Bukkit.getScheduler().runTask(plugin, onComplete);
            }
        });
    }

    public void installAll() {
        Set<String> managedPlugins = getManagedPlugins();
        List<String> sorted = sortPluginsByDependencies(managedPlugins);
        int installed = 0;

        for (String pluginName : sorted) {
            if (!isPluginInstalled(pluginName)) {
                boolean ok = downloadService.downloadSync(
                        Repository.getUrl(pluginName),
                        new File(getPluginsDirectory(), getJarName(pluginName)));
                if (ok) {
                    configManager.generateConfig(pluginName);
                    logger.info("Installed " + Repository.getDisplayName(pluginName));
                    installed++;
                }
            }
        }
        logger.info("Installation complete: " + installed + "/" + sorted.size() + " plugins installed.");
    }

    public void updateAll() {
        Set<String> managedPlugins = getManagedPlugins();
        for (String pluginName : managedPlugins) {
            if (isPluginInstalled(pluginName)) {
                updatePlugin(pluginName);
            }
        }
    }

    public boolean updatePlugin(String pluginName) {
        String url = Repository.getUrl(pluginName);
        if (url == null) return false;

        File pluginsDir = getPluginsDirectory();
        File destination = new File(pluginsDir, getJarName(pluginName));
        File backup = new File(pluginsDir, getJarName(pluginName) + ".backup");

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
            logger.info("Updated " + Repository.getDisplayName(pluginName));
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

    public boolean removePlugin(String pluginName) {
        File jar = new File(getPluginsDirectory(), getJarName(pluginName));
        if (jar.exists()) {
            jar.delete();
            logger.info("Removed " + Repository.getDisplayName(pluginName));
            return true;
        }
        return false;
    }

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

    public void reloadAll() {
        for (String pluginName : getManagedPlugins()) {
            Plugin bukkitPlugin = Bukkit.getPluginManager().getPlugin(getBukkitName(pluginName));
            if (bukkitPlugin != null && bukkitPlugin.isEnabled()) {
                Bukkit.getPluginManager().disablePlugin(bukkitPlugin);
                Bukkit.getPluginManager().enablePlugin(bukkitPlugin);
            }
        }
        logger.info("All managed plugins reloaded.");
    }

    private boolean isPluginInstalled(String pluginName) {
        String bukkitName = getBukkitName(pluginName);
        Plugin p = Bukkit.getPluginManager().getPlugin(bukkitName);
        if (p != null) return true;
        File jar = new File(getPluginsDirectory(), getJarName(pluginName));
        return jar.exists();
    }

    private boolean isPluginEnabled(String pluginName) {
        Plugin p = Bukkit.getPluginManager().getPlugin(getBukkitName(pluginName));
        return p != null && p.isEnabled();
    }

    private String getPluginVersion(String pluginName) {
        Plugin p = Bukkit.getPluginManager().getPlugin(getBukkitName(pluginName));
        return p != null ? p.getDescription().getVersion() : "unknown";
    }

    private String getBukkitName(String pluginName) {
        return switch (pluginName.toLowerCase()) {
            case "viaversion" -> "ViaVersion";
            case "viabackwards" -> "ViaBackwards";
            case "viarewind" -> "ViaRewind";
            case "viarewind-legacysupport" -> "ViaRewindLegacySupport";
            case "viaprilfools" -> "ViaAprilFools";
            case "viabungee" -> "ViaBungee";
            case "geyser" -> "Geyser-Spigot";
            case "floodgate" -> "floodgate";
            case "hurricane" -> "Hurricane";
            case "geyserconnect" -> "GeyserConnect";
            case "thirdpartycosmetics" -> "ThirdPartyCosmetics";
            case "thunderbeta" -> "Thunder";
            case "rainbow" -> "Rainbow";
            case "protocolib" -> "ProtocolLib";
            case "authme" -> "AuthMe";
            case "tab" -> "TAB";
            default -> pluginName;
        };
    }

    private String getJarName(String pluginName) {
        return switch (pluginName.toLowerCase()) {
            case "geyser" -> "Geyser-Spigot.jar";
            case "floodgate" -> "floodgate-spigot.jar";
            case "viarewind-legacysupport" -> "ViaRewind-Legacy-Support.jar";
            case "viabungee" -> "ViaBungee.jar";
            case "thunderbeta" -> "Thunder.jar";
            default -> Repository.getDisplayName(pluginName) + ".jar";
        };
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
        for (String plugin : Set.copyOf(managed)) {
            for (String dep : Repository.getDependencies(plugin)) {
                if (!managed.contains(dep) && !toAdd.contains(dep) && isPluginCompatibleWithPlatform(dep)) {
                    logger.info("Auto-enabling " + Repository.getDisplayName(dep)
                            + " (required by " + Repository.getDisplayName(plugin) + ")");
                    toAdd.add(dep);
                }
            }
        }
        managed.addAll(toAdd);
    }

    private boolean isPluginCompatibleWithPlatform(String pluginName) {
        return switch (pluginName.toLowerCase()) {
            case "viabungee" -> platform == PlatformDetector.Platform.VELOCITY;
            case "protocolib" -> PlatformDetector.isBukkitBased(platform);
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
