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

public final class Repository {

    public enum PluginType {
        VIAMCRAFT,
        GEYSERMC,
        INTEGRATION,
        COMMUNITY
    }

    public record PluginInfo(String displayName, PluginType type, String url,
                              String bukkitName, String jarName) {}

    private static final Map<String, PluginInfo> PLUGINS = new HashMap<>();
    private static final Map<String, List<String>> DEPENDENCIES = new HashMap<>();

    static {
        PLUGINS.put("viaversion", new PluginInfo("ViaVersion", PluginType.VIAMCRAFT,
                "https://hangar.papermc.io/api/v1/plugins/ViaVersion/ViaVersion/versions/latest/download?platform=PAPER",
                "ViaVersion", "ViaVersion.jar"));
        PLUGINS.put("viabackwards", new PluginInfo("ViaBackwards", PluginType.VIAMCRAFT,
                "https://hangar.papermc.io/api/v1/plugins/ViaVersion/ViaBackwards/versions/latest/download?platform=PAPER",
                "ViaBackwards", "ViaBackwards.jar"));
        PLUGINS.put("viarewind", new PluginInfo("ViaRewind", PluginType.VIAMCRAFT,
                "https://hangar.papermc.io/api/v1/plugins/ViaVersion/ViaRewind/versions/latest/download?platform=PAPER",
                "ViaRewind", "ViaRewind.jar"));
        PLUGINS.put("viarewind-legacysupport", new PluginInfo("ViaRewindLegacySupport", PluginType.VIAMCRAFT,
                "https://hangar.papermc.io/api/v1/plugins/ViaVersion/ViaRewindLegacySupport/versions/latest/download?platform=PAPER",
                "ViaRewindLegacySupport", "ViaRewind-Legacy-Support.jar"));
        PLUGINS.put("viaprilfools", new PluginInfo("ViaAprilFools", PluginType.VIAMCRAFT,
                "https://hangar.papermc.io/api/v1/plugins/ViaVersion/ViaAprilFools/versions/latest/download?platform=PAPER",
                "ViaAprilFools", "ViaAprilFools.jar"));
        PLUGINS.put("viabungee", new PluginInfo("ViaBungee", PluginType.VIAMCRAFT,
                "https://hangar.papermc.io/api/v1/plugins/ViaVersion/ViaBungee/versions/latest/download?platform=WATERFALL",
                "ViaBungee", "ViaBungee.jar"));
        PLUGINS.put("protocolib", new PluginInfo("ProtocolLib", PluginType.COMMUNITY,
                "https://hangar.papermc.io/api/v1/plugins/dmulloy2/ProtocolLib/versions/latest/download?platform=PAPER",
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
                "https://hangar.papermc.io/api/v1/plugins/AuthMe/AuthMeReloaded/versions/latest/download?platform=PAPER",
                "AuthMe", "AuthMe.jar"));
        PLUGINS.put("tab", new PluginInfo("TAB", PluginType.INTEGRATION,
                "https://hangar.papermc.io/api/v1/plugins/NEZNAMY/TAB/versions/latest/download?platform=PAPER",
                "TAB", "TAB.jar"));

        PLUGINS.put("tuffxplus", new PluginInfo("TuffXPlus", PluginType.COMMUNITY,
                "https://api.spigotmc.org/legacy/resource.php?id=136847",
                "TuffXPlus", "TuffXPlus.jar"));

        DEPENDENCIES.put("geyser", List.of("floodgate"));
        DEPENDENCIES.put("authme", List.of("floodgate"));
        DEPENDENCIES.put("tuffxplus", List.of("viaversion", "viabackwards"));
    }

    private Repository() {}

    public static String getUrl(String pluginName) {
        PluginInfo info = PLUGINS.get(pluginName.toLowerCase(Locale.ROOT));
        return info != null ? info.url : null;
    }

    public static String getDisplayName(String pluginName) {
        PluginInfo info = PLUGINS.get(pluginName.toLowerCase(Locale.ROOT));
        return info != null ? info.displayName : pluginName;
    }

    public static PluginType getType(String pluginName) {
        PluginInfo info = PLUGINS.get(pluginName.toLowerCase(Locale.ROOT));
        return info != null ? info.type : null;
    }

    public static Map<String, PluginInfo> getAllPlugins() {
        return Collections.unmodifiableMap(PLUGINS);
    }

    public static boolean isKnown(String pluginName) {
        return PLUGINS.containsKey(pluginName.toLowerCase(Locale.ROOT));
    }

    public static List<String> getDependencies(String pluginName) {
        return DEPENDENCIES.getOrDefault(pluginName.toLowerCase(Locale.ROOT), List.of());
    }

    public static String getBukkitName(String pluginName) {
        PluginInfo info = PLUGINS.get(pluginName.toLowerCase(Locale.ROOT));
        return info != null ? info.bukkitName : pluginName;
    }

    public static String getJarName(String pluginName) {
        PluginInfo info = PLUGINS.get(pluginName.toLowerCase(Locale.ROOT));
        return info != null ? info.jarName : pluginName + ".jar";
    }
}
