/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * OmniBridge - Universal cross-version and cross-platform connectivity
 * Copyright (c) 2026 OmniBridge Contributors
 */

package io.omnibrige.download;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Central registry of all managed plugins, their download URLs, and dependency mappings.
 * Provides lookup methods for plugin metadata by key.
 *
 * <p>Every entry describes exactly one downloadable artifact and where it has to be
 * installed. Two kinds of artifacts are supported:</p>
 * <ul>
 *   <li>{@link InstallTarget#PLUGINS} - a regular Bukkit plugin, installed as
 *       {@code plugins/<jarName>}.</li>
 *   <li>{@link InstallTarget#GEYSER_EXTENSIONS} - a Geyser extension, installed as
 *       {@code plugins/Geyser-Spigot/extensions/<jarName>}.</li>
 * </ul>
 */
public final class Repository {

    /** Categorizes managed plugins by their project family. */
    public enum PluginType {
        VIAMCRAFT,
        GEYSERMC,
        INTEGRATION,
        COMMUNITY
    }

    /** Describes where a downloaded artifact must be placed. */
    public enum InstallTarget {

        /** A regular Bukkit plugin placed directly in the {@code plugins/} directory. */
        PLUGINS,

        /** A Geyser extension placed in {@code plugins/Geyser-Spigot/extensions/}. */
        GEYSER_EXTENSIONS;

        /**
         * Resolves the final path of an artifact.
         *
         * @param pluginsDir the server's root {@code plugins/} directory
         * @param jarName the artifact filename
         * @return the absolute destination file
         */
        public File resolve(File pluginsDir, String jarName) {
            File base = pluginsDir;
            if (this == GEYSER_EXTENSIONS) {
                base = new File(new File(pluginsDir, "Geyser-Spigot"), "extensions");
            }
            return new File(base, jarName);
        }
    }

    /**
     * Describes a single plugin entry in the repository.
     *
     * @param displayName the human-readable plugin name
     * @param type the plugin category
     * @param url the download URL for the pinned version, or the project's "latest" endpoint
     * @param bukkitName the internal Bukkit plugin name (ignored for extensions)
     * @param jarName the expected JAR filename
     * @param version the version the {@code url} points at, or {@code null} when the URL is a
     *                rolling "latest" endpoint whose version cannot be known up front
     * @param target where the artifact has to be installed
     */
    public record PluginInfo(String displayName, PluginType type, String url,
                             String bukkitName, String jarName,
                             String version, InstallTarget target) {

        /** @return true when this artifact is a Geyser extension rather than a Bukkit plugin */
        public boolean isExtension() {
            return target == InstallTarget.GEYSER_EXTENSIONS;
        }
    }

    private static final String GEYSER_LATEST =
            "https://download.geysermc.org/v2/projects/%s/versions/latest/builds/latest/downloads/%s";

    private static final Map<String, PluginInfo> PLUGINS = new LinkedHashMap<>();
    private static final Map<String, List<String>> DEPENDENCIES = new LinkedHashMap<>();

    static {
        // ---------------------------------------------------------------- ViaVersion family
        PLUGINS.put("viaversion", new PluginInfo("ViaVersion", PluginType.VIAMCRAFT,
                "https://hangarcdn.papermc.io/plugins/ViaVersion/ViaVersion/versions/5.11.1-SNAPSHOT%2B1046/PAPER/ViaVersion-5.11.1-SNAPSHOT.jar",
                "ViaVersion", "ViaVersion.jar", "5.11.1", InstallTarget.PLUGINS));
        PLUGINS.put("viabackwards", new PluginInfo("ViaBackwards", PluginType.VIAMCRAFT,
                "https://hangarcdn.papermc.io/plugins/ViaVersion/ViaBackwards/versions/5.11.1-SNAPSHOT%2B618/PAPER/ViaBackwards-5.11.1-SNAPSHOT.jar",
                "ViaBackwards", "ViaBackwards.jar", "5.11.1", InstallTarget.PLUGINS));
        PLUGINS.put("viarewind", new PluginInfo("ViaRewind", PluginType.VIAMCRAFT,
                "https://hangarcdn.papermc.io/plugins/ViaVersion/ViaRewind/versions/4.1.4-SNAPSHOT%2B382/PAPER/ViaRewind-4.1.4-SNAPSHOT.jar",
                "ViaRewind", "ViaRewind.jar", "4.1.4", InstallTarget.PLUGINS));
        PLUGINS.put("viarewind-legacysupport", new PluginInfo("ViaRewindLegacySupport", PluginType.VIAMCRAFT,
                "https://hangarcdn.papermc.io/plugins/ViaVersion/ViaRewindLegacySupport/versions/1.5.5-SNAPSHOT%2B60/PAPER/ViaRewind-Legacy-Support-1.5.5-SNAPSHOT.jar",
                "ViaRewindLegacySupport", "ViaRewind-Legacy-Support.jar", "1.5.5", InstallTarget.PLUGINS));
        PLUGINS.put("viaprilfools", new PluginInfo("ViaAprilFools", PluginType.VIAMCRAFT,
                "https://hangarcdn.papermc.io/plugins/ViaVersion/ViaAprilFools/versions/4.2.3-SNAPSHOT%2B163/PAPER/ViaAprilFools-4.2.3-SNAPSHOT.jar",
                "ViaAprilFools", "ViaAprilFools.jar", "4.2.3", InstallTarget.PLUGINS));
        PLUGINS.put("viabungee", new PluginInfo("ViaBungee", PluginType.VIAMCRAFT,
                "https://hangarcdn.papermc.io/plugins/ViaVersion/ViaBungee/versions/0.4.0/WATERFALL/ViaBungee-0.4.0.jar",
                "ViaBungee", "ViaBungee.jar", "0.4.0", InstallTarget.PLUGINS));

        // ---------------------------------------------------------------- GeyserMC family
        PLUGINS.put("geyser", new PluginInfo("Geyser", PluginType.GEYSERMC,
                String.format(GEYSER_LATEST, "geyser", "spigot"),
                "Geyser-Spigot", "Geyser-Spigot.jar", null, InstallTarget.PLUGINS));
        PLUGINS.put("floodgate", new PluginInfo("Floodgate", PluginType.GEYSERMC,
                String.format(GEYSER_LATEST, "floodgate", "spigot"),
                "floodgate", "floodgate-spigot.jar", null, InstallTarget.PLUGINS));
        PLUGINS.put("hurricane", new PluginInfo("Hurricane", PluginType.GEYSERMC,
                String.format(GEYSER_LATEST, "hurricane", "spigot"),
                "Hurricane", "Hurricane.jar", null, InstallTarget.PLUGINS));
        // GeyserConnect and ThirdPartyCosmetics are Geyser *extensions*, not standalone plugins.
        PLUGINS.put("geyserconnect", new PluginInfo("GeyserConnect", PluginType.GEYSERMC,
                String.format(GEYSER_LATEST, "geyserconnect", "geyserconnect"),
                "GeyserConnect", "GeyserConnect.jar", null, InstallTarget.GEYSER_EXTENSIONS));
        PLUGINS.put("thirdpartycosmetics", new PluginInfo("ThirdPartyCosmetics", PluginType.GEYSERMC,
                String.format(GEYSER_LATEST, "thirdpartycosmetics", "thirdpartycosmetics"),
                "ThirdPartyCosmetics", "ThirdPartyCosmetics.jar", null, InstallTarget.GEYSER_EXTENSIONS));

        // ---------------------------------------------------------------- Integration
        PLUGINS.put("authme", new PluginInfo("AuthMe", PluginType.INTEGRATION,
                "https://github.com/AuthMe/AuthMeReloaded/releases/download/6.0.0/AuthMe-6.0.0-Paper.jar",
                "AuthMe", "AuthMe.jar", "6.0.0", InstallTarget.PLUGINS));
        PLUGINS.put("tab", new PluginInfo("TAB", PluginType.INTEGRATION,
                "https://hangarcdn.papermc.io/plugins/NEZNAMY/TAB/versions/5.0.7/PAPER/TAB%20v5.0.7.jar",
                "TAB", "TAB.jar", "5.0.7", InstallTarget.PLUGINS));

        PLUGINS.put("luckperms", new PluginInfo("LuckPerms", PluginType.INTEGRATION,
                "https://download.luckperms.net/latest/bukkit/loader/LuckPerms-Bukkit.jar",
                "LuckPerms", "LuckPerms.jar", null, InstallTarget.PLUGINS));
        PLUGINS.put("essentialsx", new PluginInfo("EssentialsX", PluginType.INTEGRATION,
                "https://cdn.modrinth.com/data/hXiIvTyT/versions/nY6VN1XH/EssentialsX-2.22.0.jar",
                "EssentialsX", "EssentialsX.jar", "2.22.0", InstallTarget.PLUGINS));
        PLUGINS.put("placeholderapi", new PluginInfo("PlaceholderAPI", PluginType.INTEGRATION,
                "https://hangarcdn.papermc.io/plugins/HelpChat/PlaceholderAPI/versions/2.12.3/PAPER/PlaceholderAPI-2.12.3.jar",
                "PlaceholderAPI", "PlaceholderAPI.jar", "2.12.3", InstallTarget.PLUGINS));
        PLUGINS.put("worldguard", new PluginInfo("WorldGuard", PluginType.INTEGRATION,
                "https://cdn.modrinth.com/data/DKY9btbd/versions/pI4UHLJL/worldguard-bukkit-7.0.17.jar",
                "WorldGuard", "WorldGuard.jar", "7.0.17", InstallTarget.PLUGINS));
        PLUGINS.put("coreprotect", new PluginInfo("CoreProtect", PluginType.INTEGRATION,
                "https://cdn.modrinth.com/data/Lu3KuzdV/versions/Kma0kBsY/CoreProtect-CE-24.0.jar",
                "CoreProtect", "CoreProtect.jar", "24.0", InstallTarget.PLUGINS));

        // ---------------------------------------------------------------- Community
        PLUGINS.put("protocolib", new PluginInfo("ProtocolLib", PluginType.COMMUNITY,
                "https://github.com/dmulloy2/ProtocolLib/releases/download/5.4.0/ProtocolLib.jar",
                "ProtocolLib", "ProtocolLib.jar", "5.4.0", InstallTarget.PLUGINS));
        PLUGINS.put("tuffxplus", new PluginInfo("TuffXPlus", PluginType.COMMUNITY,
                "https://github.com/TuffNetwork/TuffXPlus/releases/download/1.1.1/TuffXPlus-1.1.1.jar",
                "TuffXPlus", "TuffXPlus.jar", "1.1.1", InstallTarget.PLUGINS));
        PLUGINS.put("spark", new PluginInfo("spark", PluginType.COMMUNITY,
                "https://ci.lucko.me/job/spark/lastSuccessfulBuild/artifact/spark-bukkit/build/libs/spark-1.10.194-bukkit.jar",
                "spark", "spark.jar", "1.10.194", InstallTarget.PLUGINS));
        PLUGINS.put("discordsrv", new PluginInfo("DiscordSRV", PluginType.INTEGRATION,
                "https://github.com/DiscordSRV/DiscordSRV/releases/download/v1.30.5/DiscordSRV-Build-1.30.5.jar",
                "DiscordSRV", "DiscordSRV.jar", "1.30.5", InstallTarget.PLUGINS));
        PLUGINS.put("chunky", new PluginInfo("Chunky", PluginType.COMMUNITY,
                "https://hangarcdn.papermc.io/plugins/pop4959/Chunky/versions/1.5.3/PAPER/Chunky-Bukkit-1.5.3.jar",
                "Chunky", "Chunky.jar", "1.5.3", InstallTarget.PLUGINS));
        PLUGINS.put("bluemap", new PluginInfo("BlueMap", PluginType.COMMUNITY,
                "https://hangarcdn.papermc.io/plugins/Blue/BlueMap/versions/5.22/PAPER/bluemap-5.22-paper.jar",
                "BlueMap", "BlueMap.jar", "5.22", InstallTarget.PLUGINS));
        PLUGINS.put("griefprevention", new PluginInfo("GriefPrevention", PluginType.COMMUNITY,
                "https://hangarcdn.papermc.io/plugins/GriefPrevention/GriefPrevention/versions/16.18.4/PAPER/GriefPrevention%2016.18.4.jar",
                "GriefPrevention", "GriefPrevention.jar", "16.18.4", InstallTarget.PLUGINS));

        PLUGINS.put("vault", new PluginInfo("Vault", PluginType.INTEGRATION,
                "https://github.com/MilkBowl/Vault/releases/download/1.7.3/Vault.jar",
                "Vault", "Vault.jar", "1.7.3", InstallTarget.PLUGINS));
        PLUGINS.put("commandapi", new PluginInfo("CommandAPI", PluginType.COMMUNITY,
                "https://github.com/JorelAli/CommandAPI/releases/download/12.1.0/CommandAPI-12.1.0-Paper.jar",
                "CommandAPI", "CommandAPI.jar", "12.1.0", InstallTarget.PLUGINS));

        // ---------------------------------------------------------------- Dependencies
        // Hard requirements only: enabling the key auto-enables (and installs) the values.
        DEPENDENCIES.put("viabackwards", List.of("viaversion"));
        DEPENDENCIES.put("viarewind", List.of("viaversion", "viabackwards"));
        DEPENDENCIES.put("viarewind-legacysupport", List.of("viarewind"));
        DEPENDENCIES.put("viaprilfools", List.of("viaversion"));
        DEPENDENCIES.put("geyser", List.of("floodgate"));
        DEPENDENCIES.put("hurricane", List.of("geyser", "floodgate"));
        DEPENDENCIES.put("geyserconnect", List.of("geyser"));
        DEPENDENCIES.put("thirdpartycosmetics", List.of("geyser"));
        DEPENDENCIES.put("authme", List.of("floodgate"));
        DEPENDENCIES.put("tuffxplus", List.of("viaversion", "viabackwards"));
        DEPENDENCIES.put("discordsrv", List.of("placeholderapi"));
    }

    private Repository() {}

    /**
     * Returns the download URL for the given plugin.
     *
     * @param pluginName the internal plugin key (case-insensitive)
     * @return the URL string, or null if not found
     */
    public static String getUrl(String pluginName) {
        PluginInfo info = get(pluginName);
        return info != null ? info.url() : null;
    }

    /**
     * Returns the human-readable display name for the given plugin.
     *
     * @param pluginName the internal plugin key (case-insensitive)
     * @return the display name, or the input key if not found
     */
    public static String getDisplayName(String pluginName) {
        PluginInfo info = get(pluginName);
        return info != null ? info.displayName() : pluginName;
    }

    /**
     * Returns the plugin type for the given plugin.
     *
     * @param pluginName the internal plugin key (case-insensitive)
     * @return the PluginType enum value, or null if not found
     */
    public static PluginType getType(String pluginName) {
        PluginInfo info = get(pluginName);
        return info != null ? info.type() : null;
    }

    /**
     * Returns the version the pinned download URL points at.
     *
     * @param pluginName the internal plugin key (case-insensitive)
     * @return the version string, or null when the entry uses a rolling "latest" endpoint
     */
    public static String getVersion(String pluginName) {
        PluginInfo info = get(pluginName);
        return info != null ? info.version() : null;
    }

    /**
     * Returns where the artifact for the given plugin has to be installed.
     *
     * @param pluginName the internal plugin key (case-insensitive)
     * @return the install target, or {@link InstallTarget#PLUGINS} for unknown keys
     */
    public static InstallTarget getInstallTarget(String pluginName) {
        PluginInfo info = get(pluginName);
        return info != null ? info.target() : InstallTarget.PLUGINS;
    }

    /**
     * Resolves the absolute destination file for a managed plugin.
     *
     * @param pluginsDir the server's root {@code plugins/} directory
     * @param pluginName the internal plugin key (case-insensitive)
     * @return the destination file the artifact is (or would be) installed at
     */
    public static File resolveInstallPath(File pluginsDir, String pluginName) {
        return getInstallTarget(pluginName).resolve(pluginsDir, getJarName(pluginName));
    }

    /**
     * Returns all registered plugin entries.
     *
     * @return an unmodifiable map of plugin keys to their PluginInfo, in registration order
     */
    public static Map<String, PluginInfo> getAllPlugins() {
        return Collections.unmodifiableMap(PLUGINS);
    }

    /**
     * Checks whether a plugin key is registered in the repository.
     *
     * @param pluginName the internal plugin key (case-insensitive)
     * @return true if the plugin is known
     */
    public static boolean isKnown(String pluginName) {
        return get(pluginName) != null;
    }

    /**
     * Returns the dependency list for the given plugin.
     *
     * @param pluginName the internal plugin key (case-insensitive)
     * @return an immutable list of dependency plugin keys
     */
    public static List<String> getDependencies(String pluginName) {
        String key = keyOf(pluginName);
        if (key == null) return List.of();
        return DEPENDENCIES.getOrDefault(key, List.of());
    }

    /**
     * Returns the Bukkit/plugin manager name for the given plugin.
     *
     * @param pluginName the internal plugin key (case-insensitive)
     * @return the Bukkit name, or the input key if not found
     */
    public static String getBukkitName(String pluginName) {
        PluginInfo info = get(pluginName);
        return info != null ? info.bukkitName() : pluginName;
    }

    /**
     * Returns the expected JAR filename for the given plugin.
     *
     * @param pluginName the internal plugin key (case-insensitive)
     * @return the JAR filename, or a default based on the input key if not found
     */
    public static String getJarName(String pluginName) {
        PluginInfo info = get(pluginName);
        return info != null ? info.jarName() : pluginName + ".jar";
    }

    /**
     * Returns the full metadata record for the given plugin.
     *
     * @param pluginName the internal plugin key or display name (case-insensitive)
     * @return the plugin metadata, or null if the plugin is not registered
     */
    public static PluginInfo getPluginInfo(String pluginName) {
        return get(pluginName);
    }

    /**
     * Resolves the canonical registry key for a plugin key or display name.
     *
     * <p>Useful for normalizing user input (e.g. {@code "ViaRewindLegacySupport"})
     * before looking up config values or descriptions.</p>
     *
     * @param pluginName the internal plugin key or display name (case-insensitive)
     * @return the canonical lowercase registry key, or null if the plugin is unknown
     */
    public static String getKey(String pluginName) {
        return keyOf(pluginName);
    }

    private static PluginInfo get(String pluginName) {
        String key = keyOf(pluginName);
        return key != null ? PLUGINS.get(key) : null;
    }

    /**
     * Resolves a registry key from a key or display name.
     *
     * @param pluginName the internal key or display name (case-insensitive)
     * @return the canonical lowercase registry key, or null if unknown
     */
    private static String keyOf(String pluginName) {
        if (pluginName == null) return null;
        String lower = pluginName.toLowerCase(Locale.ROOT);
        if (PLUGINS.containsKey(lower)) return lower;
        // Fall back to a display-name match so callers may pass "ViaVersion" or "Geyser".
        for (var entry : PLUGINS.entrySet()) {
            if (entry.getValue().displayName().equalsIgnoreCase(pluginName)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
