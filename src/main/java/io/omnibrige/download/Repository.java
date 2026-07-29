/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * OmniBridge - Universal cross-version and cross-platform connectivity
 * Copyright (c) 2026 OmniBridge Contributors
 */

package io.omnibrige.download;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Central registry of all managed plugins, their download URLs, and dependency mappings.
 * Provides lookup methods for plugin metadata by key.
 */
public final class Repository {

    /** Categorizes managed plugins by their project family. */
    public enum PluginType {
        VIAMCRAFT,
        GEYSERMC,
        INTEGRATION,
        COMMUNITY
    }

    /**
     * Describes a single plugin entry in the repository.
     *
     * @param displayName the human-readable plugin name
     * @param type the plugin category
     * @param url the download URL for the latest version
     * @param bukkitName the internal Bukkit plugin name
     * @param jarName the expected JAR filename
     */
    public record PluginInfo(String displayName, PluginType type, String url,
                              String bukkitName, String jarName) {}

    private static final Map<String, PluginInfo> PLUGINS = new HashMap<>();
    private static final Map<String, List<String>> DEPENDENCIES = new HashMap<>();

    static {
        PLUGINS.put("viaversion", new PluginInfo("ViaVersion", PluginType.VIAMCRAFT,
                "https://hangarcdn.papermc.io/plugins/ViaVersion/ViaVersion/versions/5.11.1-SNAPSHOT%2B1046/PAPER/ViaVersion-5.11.1-SNAPSHOT.jar",
                "ViaVersion", "ViaVersion.jar"));
        PLUGINS.put("viabackwards", new PluginInfo("ViaBackwards", PluginType.VIAMCRAFT,
                "https://hangarcdn.papermc.io/plugins/ViaVersion/ViaBackwards/versions/5.11.1-SNAPSHOT%2B618/PAPER/ViaBackwards-5.11.1-SNAPSHOT.jar",
                "ViaBackwards", "ViaBackwards.jar"));
        PLUGINS.put("viarewind", new PluginInfo("ViaRewind", PluginType.VIAMCRAFT,
                "https://hangarcdn.papermc.io/plugins/ViaVersion/ViaRewind/versions/4.1.4-SNAPSHOT%2B382/PAPER/ViaRewind-4.1.4-SNAPSHOT.jar",
                "ViaRewind", "ViaRewind.jar"));
        PLUGINS.put("viarewind-legacysupport", new PluginInfo("ViaRewindLegacySupport", PluginType.VIAMCRAFT,
                "https://hangarcdn.papermc.io/plugins/ViaVersion/ViaRewindLegacySupport/versions/1.5.5-SNAPSHOT%2B60/PAPER/ViaRewind-Legacy-Support-1.5.5-SNAPSHOT.jar",
                "ViaRewindLegacySupport", "ViaRewind-Legacy-Support.jar"));
        PLUGINS.put("viaprilfools", new PluginInfo("ViaAprilFools", PluginType.VIAMCRAFT,
                "https://hangarcdn.papermc.io/plugins/ViaVersion/ViaAprilFools/versions/4.2.3-SNAPSHOT%2B163/PAPER/ViaAprilFools-4.2.3-SNAPSHOT.jar",
                "ViaAprilFools", "ViaAprilFools.jar"));
        PLUGINS.put("viabungee", new PluginInfo("ViaBungee", PluginType.VIAMCRAFT,
                "https://hangarcdn.papermc.io/plugins/ViaVersion/ViaBungee/versions/0.4.0/WATERFALL/ViaBungee-0.4.0.jar",
                "ViaBungee", "ViaBungee.jar"));
        PLUGINS.put("protocolib", new PluginInfo("ProtocolLib", PluginType.COMMUNITY,
                "https://github.com/dmulloy2/ProtocolLib/releases/download/5.4.0/ProtocolLib.jar",
                "ProtocolLib", "ProtocolLib.jar"));

        PLUGINS.put("geyser", new PluginInfo("Geyser", PluginType.GEYSERMC,
                "https://download.geysermc.org/v2/projects/geyser/versions/latest/builds/latest/downloads/spigot",
                "Geyser-Spigot", "Geyser-Spigot.jar"));
        PLUGINS.put("floodgate", new PluginInfo("Floodgate", PluginType.GEYSERMC,
                "https://download.geysermc.org/v2/projects/floodgate/versions/latest/builds/latest/downloads/spigot",
                "floodgate", "floodgate-spigot.jar"));
        PLUGINS.put("hurricane", new PluginInfo("Hurricane", PluginType.GEYSERMC,
                "https://download.geysermc.org/v2/projects/hurricane/versions/latest/builds/latest/downloads/spigot",
                "Hurricane", "Hurricane.jar"));
        PLUGINS.put("geyserconnect", new PluginInfo("GeyserConnect", PluginType.GEYSERMC,
                "https://download.geysermc.org/v2/projects/geyserconnect/versions/latest/builds/latest/downloads/spigot",
                "GeyserConnect", "GeyserConnect.jar"));
        PLUGINS.put("thirdpartycosmetics", new PluginInfo("ThirdPartyCosmetics", PluginType.GEYSERMC,
                "https://download.geysermc.org/v2/projects/thirdpartycosmetics/versions/latest/builds/latest/downloads/spigot",
                "ThirdPartyCosmetics", "ThirdPartyCosmetics.jar"));
        PLUGINS.put("thunderbeta", new PluginInfo("ThunderBeta", PluginType.GEYSERMC,
                "https://download.geysermc.org/v2/projects/thunderbeta/versions/latest/builds/latest/downloads/spigot",
                "Thunder", "Thunder.jar"));
        PLUGINS.put("rainbow", new PluginInfo("Rainbow", PluginType.GEYSERMC,
                "https://download.geysermc.org/v2/projects/rainbow/versions/latest/builds/latest/downloads/spigot",
                "Rainbow", "Rainbow.jar"));

        PLUGINS.put("authme", new PluginInfo("AuthMe", PluginType.INTEGRATION,
                "https://github.com/AuthMe/AuthMeReloaded/releases/download/6.0.0/AuthMe-6.0.0-Paper.jar",
                "AuthMe", "AuthMe.jar"));
        PLUGINS.put("tab", new PluginInfo("TAB", PluginType.INTEGRATION,
                "https://hangarcdn.papermc.io/plugins/NEZNAMY/TAB/versions/5.0.7/PAPER/TAB%20v5.0.7.jar",
                "TAB", "TAB.jar"));

        PLUGINS.put("luckperms", new PluginInfo("LuckPerms", PluginType.INTEGRATION,
                "https://download.luckperms.net/latest/bukkit/loader/LuckPerms-Bukkit.jar",
                "LuckPerms", "LuckPerms.jar"));
        PLUGINS.put("essentialsx", new PluginInfo("EssentialsX", PluginType.INTEGRATION,
                "https://cdn.modrinth.com/data/hXiIvTyT/versions/nY6VN1XH/EssentialsX-2.22.0.jar",
                "EssentialsX", "EssentialsX.jar"));
        PLUGINS.put("placeholderapi", new PluginInfo("PlaceholderAPI", PluginType.INTEGRATION,
                "https://hangarcdn.papermc.io/plugins/HelpChat/PlaceholderAPI/versions/2.12.3/PAPER/PlaceholderAPI-2.12.3.jar",
                "PlaceholderAPI", "PlaceholderAPI.jar"));
        PLUGINS.put("worldguard", new PluginInfo("WorldGuard", PluginType.INTEGRATION,
                "https://cdn.modrinth.com/data/DKY9btbd/versions/pI4UHLJL/worldguard-bukkit-7.0.17.jar",
                "WorldGuard", "WorldGuard.jar"));
        PLUGINS.put("coreprotect", new PluginInfo("CoreProtect", PluginType.INTEGRATION,
                "https://cdn.modrinth.com/data/Lu3KuzdV/versions/Kma0kBsY/CoreProtect-CE-24.0.jar",
                "CoreProtect", "CoreProtect.jar"));

        PLUGINS.put("tuffxplus", new PluginInfo("TuffXPlus", PluginType.COMMUNITY,
                "https://github.com/TuffNetwork/TuffXPlus/releases/download/1.1.1/TuffXPlus-1.1.1.jar",
                "TuffXPlus", "TuffXPlus.jar"));

        PLUGINS.put("spark", new PluginInfo("spark", PluginType.COMMUNITY,
                "https://ci.lucko.me/job/spark/lastSuccessfulBuild/artifact/spark-bukkit/build/libs/spark-1.10.175-bukkit.jar",
                "spark", "spark.jar"));
        PLUGINS.put("discordsrv", new PluginInfo("DiscordSRV", PluginType.INTEGRATION,
                "https://github.com/DiscordSRV/DiscordSRV/releases/download/v1.30.5/DiscordSRV-Build-1.30.5.jar",
                "DiscordSRV", "DiscordSRV.jar"));
        PLUGINS.put("chunky", new PluginInfo("Chunky", PluginType.COMMUNITY,
                "https://hangarcdn.papermc.io/plugins/pop4959/Chunky/versions/1.5.3/PAPER/Chunky-Bukkit-1.5.3.jar",
                "Chunky", "Chunky.jar"));
        PLUGINS.put("bluemap", new PluginInfo("BlueMap", PluginType.COMMUNITY,
                "https://hangarcdn.papermc.io/plugins/Blue/BlueMap/versions/5.22/PAPER/bluemap-5.22-paper.jar",
                "BlueMap", "BlueMap.jar"));
        PLUGINS.put("griefprevention", new PluginInfo("GriefPrevention", PluginType.COMMUNITY,
                "https://hangarcdn.papermc.io/plugins/GriefPrevention/GriefPrevention/versions/16.18.4/PAPER/GriefPrevention%2016.18.4.jar",
                "GriefPrevention", "GriefPrevention.jar"));

        PLUGINS.put("vault", new PluginInfo("Vault", PluginType.INTEGRATION,
                "https://github.com/MilkBowl/Vault/releases/download/1.7.3/Vault.jar",
                "Vault", "Vault.jar"));
        PLUGINS.put("commandapi", new PluginInfo("CommandAPI", PluginType.COMMUNITY,
                "https://github.com/JorelAli/CommandAPI/releases/download/9.7.0/CommandAPI-Bukkit-9.7.0.jar",
                "CommandAPI", "CommandAPI.jar"));

        DEPENDENCIES.put("geyser", List.of("floodgate"));
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
        PluginInfo info = PLUGINS.get(pluginName.toLowerCase(Locale.ROOT));
        return info != null ? info.url : null;
    }

    /**
     * Returns the human-readable display name for the given plugin.
     *
     * @param pluginName the internal plugin key (case-insensitive)
     * @return the display name, or the input key if not found
     */
    public static String getDisplayName(String pluginName) {
        PluginInfo info = PLUGINS.get(pluginName.toLowerCase(Locale.ROOT));
        return info != null ? info.displayName : pluginName;
    }

    /**
     * Returns the plugin type for the given plugin.
     *
     * @param pluginName the internal plugin key (case-insensitive)
     * @return the PluginType enum value, or null if not found
     */
    public static PluginType getType(String pluginName) {
        PluginInfo info = PLUGINS.get(pluginName.toLowerCase(Locale.ROOT));
        return info != null ? info.type : null;
    }

    /**
     * Returns all registered plugin entries.
     *
     * @return an unmodifiable map of plugin keys to their PluginInfo
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
        return PLUGINS.containsKey(pluginName.toLowerCase(Locale.ROOT));
    }

    /**
     * Returns the dependency list for the given plugin.
     *
     * @param pluginName the internal plugin key (case-insensitive)
     * @return an immutable list of dependency plugin keys
     */
    public static List<String> getDependencies(String pluginName) {
        return DEPENDENCIES.getOrDefault(pluginName.toLowerCase(Locale.ROOT), List.of());
    }

    /**
     * Returns the Bukkit/plugin manager name for the given plugin.
     *
     * @param pluginName the internal plugin key (case-insensitive)
     * @return the Bukkit name, or the input key if not found
     */
    public static String getBukkitName(String pluginName) {
        PluginInfo info = PLUGINS.get(pluginName.toLowerCase(Locale.ROOT));
        return info != null ? info.bukkitName : pluginName;
    }

    /**
     * Returns the expected JAR filename for the given plugin.
     *
     * @param pluginName the internal plugin key (case-insensitive)
     * @return the JAR filename, or a default based on the input key if not found
     */
    public static String getJarName(String pluginName) {
        PluginInfo info = PLUGINS.get(pluginName.toLowerCase(Locale.ROOT));
        return info != null ? info.jarName : pluginName + ".jar";
    }
}
