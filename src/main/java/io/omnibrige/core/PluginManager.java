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
import java.nio.file.Path;
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

    public void installAll() {
        Set<String> managedPlugins = getManagedPlugins();
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();

        for (String pluginName : managedPlugins) {
            if (!isPluginInstalled(pluginName)) {
                futures.add(installPlugin(pluginName));
            } else {
                logger.info(Repository.getDisplayName(pluginName) + " is already installed.");
            }
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        long successCount = futures.stream().filter(f -> f.join()).count();
        logger.info("Installation complete: " + successCount + "/" + futures.size() + " plugins installed.");
    }

    public CompletableFuture<Boolean> installPlugin(String pluginName) {
        return CompletableFuture.supplyAsync(() -> {
            String url = Repository.getUrl(pluginName);
            if (url == null) {
                logger.warning("Unknown plugin: " + pluginName);
                return false;
            }

            File pluginsDir = getPluginsDirectory();
            File destination = new File(pluginsDir, getJarName(pluginName));

            if (destination.exists()) {
                logger.info(Repository.getDisplayName(pluginName) + " JAR already exists.");
                return true;
            }

            boolean downloaded = downloadService.downloadSync(url, destination);
            if (downloaded) {
                configManager.generateConfig(pluginName);
                logger.info("Installed " + Repository.getDisplayName(pluginName));
            }
            return downloaded;
        });
    }

    public void updateAll() {
        Set<String> managedPlugins = getManagedPlugins();
        for (String pluginName : managedPlugins) {
            if (isPluginInstalled(pluginName)) {
                logger.info("Checking updates for " + Repository.getDisplayName(pluginName) + "...");
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
                Files.copy(destination.toPath(), backup.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                logger.warning("Failed to backup " + pluginName + ": " + e.getMessage());
                return false;
            }
        }

        boolean downloaded = downloadService.downloadSync(url, destination);
        if (downloaded) {
            backup.delete();
            logger.info("Updated " + Repository.getDisplayName(pluginName));
            return true;
        } else {
            if (backup.exists()) {
                backup.renameTo(destination);
                logger.info("Restored " + Repository.getDisplayName(pluginName) + " from backup");
            }
            return false;
        }
    }

    public boolean removePlugin(String pluginName) {
        File pluginsDir = getPluginsDirectory();
        File jar = new File(pluginsDir, getJarName(pluginName));
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
            case "geyser" -> "Geyser-Spigot";
            case "floodgate" -> "floodgate";
            case "hurricane" -> "Hurricane";
            case "geyserconnect" -> "GeyserConnect";
            case "thirdpartycosmetics" -> "ThirdPartyCosmetics";
            case "thunderbeta" -> "Thunder";
            case "rainbow" -> "Rainbow";
            default -> pluginName;
        };
    }

    private String getJarName(String pluginName) {
        return switch (pluginName.toLowerCase()) {
            case "geyser" -> "Geyser-Spigot.jar";
            case "floodgate" -> "floodgate-spigot.jar";
            case "viarewind-legacysupport" -> "ViaRewind-Legacy-Support.jar";
            case "thunderbeta" -> "Thunder.jar";
            default -> Repository.getDisplayName(pluginName) + ".jar";
        };
    }

    private File getPluginsDirectory() {
        return new File(plugin.getDataFolder().getParentFile(), "plugins");
    }

    private Set<String> getManagedPlugins() {
        Set<String> managed = new HashSet<>();
        var config = plugin.getConfig();
        var managedSection = config.getConfigurationSection("managed-plugins");
        if (managedSection != null) {
            for (String key : managedSection.getKeys(false)) {
                if (managedSection.getBoolean(key)) {
                    managed.add(key);
                }
            }
        }
        return managed;
    }
}
