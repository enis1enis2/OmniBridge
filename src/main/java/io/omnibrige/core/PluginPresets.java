/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * OmniBridge - Universal cross-version and cross-platform connectivity
 * Copyright (c) 2026 OmniBridge Contributors
 */

package io.omnibrige.core;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Predefined plugin bundles that can be activated with a single command.
 * Each preset groups related plugins for common server configurations.
 */
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

        PRESETS.put("server-essentials", new Preset(
                "Server Essentials",
                "Permissions + commands + placeholders + protection + rollback",
                List.of("luckperms", "essentialsx", "placeholderapi", "worldguard", "coreprotect")));

        PRESETS.put("max-compat", new Preset(
                "Maximum Compatibility",
                "All version + Bedrock + essential + utility plugins",
                List.of("viaversion", "viabackwards", "viarewind", "geyser", "floodgate",
                        "authme", "tab", "protocolib", "luckperms", "essentialsx",
                        "placeholderapi", "worldguard", "coreprotect",
                        "spark", "discordsrv", "chunky", "bluemap", "griefprevention")));

        PRESETS.put("performance", new Preset(
                "Performance & Monitoring",
                "Profiling, chunk pre-generation, and 3D maps",
                List.of("spark", "chunky", "bluemap")));

        PRESETS.put("chat", new Preset(
                "Chat & Discord",
                "DiscordSRV bridge with PlaceholderAPI dependency",
                List.of("discordsrv", "placeholderapi")));
    }

    private PluginPresets() {}

    /**
     * Returns the preset for the given key.
     *
     * @param key the preset key (case-insensitive)
     * @return the Preset record, or null if not found
     */
    public static Preset get(String key) {
        return PRESETS.get(key.toLowerCase(Locale.ROOT));
    }

    /**
     * Returns all available presets.
     *
     * @return an immutable map of preset keys to Preset records
     */
    public static Map<String, Preset> getAll() {
        return Map.copyOf(PRESETS);
    }

    /**
     * Checks whether a preset key is known.
     *
     * @param key the preset key (case-insensitive)
     * @return true if the preset exists
     */
    public static boolean isKnown(String key) {
        return PRESETS.containsKey(key.toLowerCase(Locale.ROOT));
    }

    /**
     * Returns the plugin keys included in the specified preset.
     *
     * @param presetKey the preset key (case-insensitive)
     * @return an immutable list of plugin keys, or an empty list if not found
     */
    public static List<String> getPluginKeys(String presetKey) {
        Preset preset = PRESETS.get(presetKey.toLowerCase(Locale.ROOT));
        return preset != null ? List.copyOf(preset.plugins()) : List.of();
    }

    /**
     * Represents a predefined plugin bundle.
     *
     * @param displayName the human-readable preset name
     * @param description a short description of the preset's purpose
     * @param plugins the list of plugin keys included in this preset
     */
    public record Preset(String displayName, String description, List<String> plugins) {}
}
