package io.omnibrige.download;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Repository {

    public enum PluginType {
        VIAMCRAFT,
        GEYSERMC,
        INTEGRATION,
        COMMUNITY
    }

    private static final Map<String, PluginInfo> PLUGINS = new HashMap<>();
    private static final Map<String, List<String>> DEPENDENCIES = new HashMap<>();

    static {
        PLUGINS.put("viaversion", new PluginInfo("ViaVersion", PluginType.VIAMCRAFT,
                "https://hangar.papermc.io/api/v1/plugins/ViaVersion/ViaVersion/versions/latest/download?platform=PAPER"));
        PLUGINS.put("viabackwards", new PluginInfo("ViaBackwards", PluginType.VIAMCRAFT,
                "https://hangar.papermc.io/api/v1/plugins/ViaVersion/ViaBackwards/versions/latest/download?platform=PAPER"));
        PLUGINS.put("viarewind", new PluginInfo("ViaRewind", PluginType.VIAMCRAFT,
                "https://hangar.papermc.io/api/v1/plugins/ViaVersion/ViaRewind/versions/latest/download?platform=PAPER"));
        PLUGINS.put("viarewind-legacysupport", new PluginInfo("ViaRewindLegacySupport", PluginType.VIAMCRAFT,
                "https://hangar.papermc.io/api/v1/plugins/ViaVersion/ViaRewindLegacySupport/versions/latest/download?platform=PAPER"));
        PLUGINS.put("viaprilfools", new PluginInfo("ViaAprilFools", PluginType.VIAMCRAFT,
                "https://hangar.papermc.io/api/v1/plugins/ViaVersion/ViaAprilFools/versions/latest/download?platform=PAPER"));
        PLUGINS.put("viabungee", new PluginInfo("ViaBungee", PluginType.VIAMCRAFT,
                "https://hangar.papermc.io/api/v1/plugins/ViaVersion/ViaBungee/versions/latest/download?platform=WATERFALL"));
        PLUGINS.put("protocolib", new PluginInfo("ProtocolLib", PluginType.COMMUNITY,
                "https://hangar.papermc.io/api/v1/plugins/dmulloy2/ProtocolLib/versions/latest/download?platform=PAPER"));

        PLUGINS.put("geyser", new PluginInfo("Geyser", PluginType.GEYSERMC,
                "https://download.geysermc.org/v2/projects/geyser/versions/latest/builds/latest/downloads/spigot"));
        PLUGINS.put("floodgate", new PluginInfo("Floodgate", PluginType.GEYSERMC,
                "https://download.geysermc.org/v2/projects/floodgate/versions/latest/builds/latest/downloads/spigot"));
        PLUGINS.put("hurricane", new PluginInfo("Hurricane", PluginType.GEYSERMC,
                "https://download.geysermc.org/v2/projects/hurricane/versions/latest/builds/latest/downloads/spigot"));
        PLUGINS.put("geyserconnect", new PluginInfo("GeyserConnect", PluginType.GEYSERMC,
                "https://download.geysermc.org/v2/projects/geyserconnect/versions/latest/builds/latest/downloads/spigot"));
        PLUGINS.put("thirdpartycosmetics", new PluginInfo("ThirdPartyCosmetics", PluginType.GEYSERMC,
                "https://download.geysermc.org/v2/projects/thirdpartycosmetics/versions/latest/builds/latest/downloads/spigot"));
        PLUGINS.put("thunderbeta", new PluginInfo("ThunderBeta", PluginType.GEYSERMC,
                "https://download.geysermc.org/v2/projects/thunderbeta/versions/latest/builds/latest/downloads/spigot"));
        PLUGINS.put("rainbow", new PluginInfo("Rainbow", PluginType.GEYSERMC,
                "https://download.geysermc.org/v2/projects/rainbow/versions/latest/builds/latest/downloads/spigot"));

        PLUGINS.put("authme", new PluginInfo("AuthMe", PluginType.INTEGRATION,
                "https://hangar.papermc.io/api/v1/plugins/AuthMe/AuthMeReloaded/versions/latest/download?platform=PAPER"));
        PLUGINS.put("tab", new PluginInfo("TAB", PluginType.INTEGRATION,
                "https://hangar.papermc.io/api/v1/plugins/NEZNAMY/TAB/versions/latest/download?platform=PAPER"));

        DEPENDENCIES.put("geyser", List.of("floodgate"));
        DEPENDENCIES.put("authme", List.of("floodgate"));
    }

    private Repository() {}

    public static String getUrl(String pluginName) {
        PluginInfo info = PLUGINS.get(pluginName.toLowerCase());
        return info != null ? info.url : null;
    }

    public static String getDisplayName(String pluginName) {
        PluginInfo info = PLUGINS.get(pluginName.toLowerCase());
        return info != null ? info.displayName : pluginName;
    }

    public static PluginType getType(String pluginName) {
        PluginInfo info = PLUGINS.get(pluginName.toLowerCase());
        return info != null ? info.type : null;
    }

    public static Map<String, PluginInfo> getAllPlugins() {
        return Map.copyOf(PLUGINS);
    }

    public static boolean isKnown(String pluginName) {
        return PLUGINS.containsKey(pluginName.toLowerCase());
    }

    public static List<String> getDependencies(String pluginName) {
        return DEPENDENCIES.getOrDefault(pluginName.toLowerCase(), List.of());
    }

    public record PluginInfo(String displayName, PluginType type, String url) {}
}
