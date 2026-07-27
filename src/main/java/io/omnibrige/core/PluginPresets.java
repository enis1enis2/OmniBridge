package io.omnibrige.core;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class PluginPresets {

    private static final Map<String, Preset> PRESETS = new LinkedHashMap<>();

    static {
        PRESETS.put("bedrock", new Preset(
                "Bedrock Support",
                "Geyser + Floodgate for Bedrock Edition players",
                List.of("geyser", "floodgate", "hurricane")));

        PRESETS.put("full-version", new Preset(
                "Full Version Support",
                "ViaVersion + ViaBackwards + ViaRewind for all Java versions",
                List.of("viaversion", "viabackwards", "viarewind", "viarewind-legacysupport")));

        PRESETS.put("essentials", new Preset(
                "Server Essentials",
                "AuthMe login + TAB formatting + ProtocolLib packet API",
                List.of("authme", "tab", "protocolib")));

        PRESETS.put("max-compat", new Preset(
                "Maximum Compatibility",
                "All version + Bedrock + essential plugins",
                List.of("viaversion", "viabackwards", "viarewind", "geyser", "floodgate",
                        "authme", "tab", "protocolib")));
    }

    private PluginPresets() {}

    public static Preset get(String key) {
        return PRESETS.get(key.toLowerCase(Locale.ROOT));
    }

    public static Map<String, Preset> getAll() {
        return Map.copyOf(PRESETS);
    }

    public static boolean isKnown(String key) {
        return PRESETS.containsKey(key.toLowerCase(Locale.ROOT));
    }

    public static List<String> getPluginKeys(String presetKey) {
        Preset preset = PRESETS.get(presetKey.toLowerCase(Locale.ROOT));
        return preset != null ? List.copyOf(preset.plugins()) : List.of();
    }

    public record Preset(String displayName, String description, List<String> plugins) {}
}
