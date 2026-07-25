package io.omnibrige.core;

import io.omnibrige.OmniBridge;
import io.omnibrige.config.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

public class ConfigManager {

    private final OmniBridge plugin;
    private final PlatformDetector.Platform platform;
    private final Logger logger;

    public ConfigManager(OmniBridge plugin, PlatformDetector.Platform platform) {
        this.plugin = plugin;
        this.platform = platform;
        this.logger = plugin.getLogger();
    }

    public void generateAllConfigs() {
        File pluginsDir = new File(plugin.getDataFolder().getParentFile(), "plugins");
        new ViaVersionConfig().generate(pluginsDir);
        new ViaBackwardsConfig().generate(pluginsDir);
        new ViaRewindConfig().generate(pluginsDir);
        new GeyserConfig(plugin.getConfig()).generate(pluginsDir);
        new FloodgateConfig().generate(pluginsDir);
        if (platform == PlatformDetector.Platform.PAPER) {
            new ViaRewindLegacySupportConfig().generate(pluginsDir);
        }
        logger.info("All plugin configs generated.");
    }

    public void generateConfig(String pluginName) {
        File pluginsDir = new File(plugin.getDataFolder().getParentFile(), "plugins");
        switch (pluginName.toLowerCase()) {
            case "viaversion" -> new ViaVersionConfig().generate(pluginsDir);
            case "viabackwards" -> new ViaBackwardsConfig().generate(pluginsDir);
            case "viarewind" -> new ViaRewindConfig().generate(pluginsDir);
            case "viarewind-legacysupport" -> new ViaRewindLegacySupportConfig().generate(pluginsDir);
            case "geyser" -> new GeyserConfig(plugin.getConfig()).generate(pluginsDir);
            case "floodgate" -> new FloodgateConfig().generate(pluginsDir);
        }
    }

    public boolean isConfigured(String pluginName) {
        File configDir = getConfigDirectory(pluginName);
        return configDir.exists() && configDir.listFiles() != null && configDir.listFiles().length > 0;
    }

    private File getConfigDirectory(String pluginName) {
        String dirName = switch (pluginName.toLowerCase()) {
            case "geyser" -> "Geyser-Spigot";
            case "floodgate" -> "floodgate";
            case "viarewind-legacysupport" -> "ViaRewindLegacySupport";
            default -> pluginName.substring(0, 1).toUpperCase() + pluginName.substring(1);
        };
        return new File(plugin.getDataFolder().getParentFile(), "plugins/" + dirName);
    }

    public static void writeConfigFile(File file, String content) {
        if (file.exists()) return;
        file.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
