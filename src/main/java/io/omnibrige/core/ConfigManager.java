/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * OmniBridge - Universal cross-version and cross-platform connectivity
 * Copyright (c) 2026 OmniBridge Contributors
 */

package io.omnibrige.core;

import io.omnibrige.OmniBridge;
import io.omnibrige.config.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Generates default configuration files for managed plugins that require them.
 * Delegates to plugin-specific config generators in the config package.
 */
public class ConfigManager {

    private final OmniBridge plugin;
    private final PlatformDetector.Platform platform;
    private final Logger logger;

    /**
     * Constructs the configuration manager.
     *
     * @param plugin the OmniBridge plugin instance
     * @param platform the detected server platform
     */
    public ConfigManager(OmniBridge plugin, PlatformDetector.Platform platform) {
        this.plugin = plugin;
        this.platform = platform;
        this.logger = plugin.getLogger();
    }

    private File getPluginsDirectory() {
        return plugin.getDataFolder().getParentFile();
    }

    /** Generates default configuration files for all supported managed plugins. */
    public void generateAllConfigs() {
        File pluginsDir = getPluginsDirectory();
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

    /**
     * Generates the configuration file for a specific plugin if one is supported.
     *
     * @param pluginName the internal plugin key
     */
    public void generateConfig(String pluginName) {
        File pluginsDir = getPluginsDirectory();
        switch (pluginName.toLowerCase(Locale.ROOT)) {
            case "viaversion" -> new ViaVersionConfig().generate(pluginsDir);
            case "viabackwards" -> new ViaBackwardsConfig().generate(pluginsDir);
            case "viarewind" -> new ViaRewindConfig().generate(pluginsDir);
            case "viarewind-legacysupport" -> new ViaRewindLegacySupportConfig().generate(pluginsDir);
            case "geyser" -> new GeyserConfig(plugin.getConfig()).generate(pluginsDir);
            case "floodgate" -> new FloodgateConfig().generate(pluginsDir);
        }
    }

    /**
     * Writes a config file only if it does not already exist.
     *
     * @param file the target config file
     * @param content the file content to write
     */
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
