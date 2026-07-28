/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * OmniBridge - Universal cross-version and cross-platform connectivity
 * Copyright (c) 2026 OmniBridge Contributors
 */

package io.omnibrige.core;

import java.util.LinkedHashMap;
import java.util.Map;

public final class PluginDescriptions {

    private static final Map<String, String> DESCRIPTIONS = new LinkedHashMap<>();

    static {
        DESCRIPTIONS.put("viaversion",
                "Allow newer Java clients (1.9+) to join older servers");
        DESCRIPTIONS.put("viabackwards",
                "Allow older Java clients (1.8-1.20) to join newer servers");
        DESCRIPTIONS.put("viarewind",
                "Allow 1.7.x and 1.8.x clients on 1.9+ servers");
        DESCRIPTIONS.put("viarewind-legacysupport",
                "Extra block/entity fixes for ViaRewind (Paper only)");
        DESCRIPTIONS.put("viaprilfools",
                "Support for April Fools and special snapshot versions");
        DESCRIPTIONS.put("viabungee",
                "ViaVersion loader for BungeeCord/Waterfall proxies");

        DESCRIPTIONS.put("geyser",
                "Bridge Bedrock Edition players to your Java server");
        DESCRIPTIONS.put("floodgate",
                "Bedrock auth bypass — no Java account needed");
        DESCRIPTIONS.put("hurricane",
                "Server-side workarounds for Geyser Bedrock players");
        DESCRIPTIONS.put("geyserconnect",
                "Bedrock players join without a proxy setup");
        DESCRIPTIONS.put("thirdpartycosmetics",
                "Third-party cosmetic support for Bedrock players");
        DESCRIPTIONS.put("thunderbeta",
                "Java-to-Bedrock resource pack converter");
        DESCRIPTIONS.put("rainbow",
                "Custom item name color mapping for Bedrock clients");

        DESCRIPTIONS.put("authme",
                "Login/authentication system with native Bedrock support");
        DESCRIPTIONS.put("tab",
                "Tab list, sidebar scoreboard, and nametag formatting");

        DESCRIPTIONS.put("protocolib",
                "Packet-level API required by many anti-cheat plugins");

        DESCRIPTIONS.put("tuffxplus",
                "Modern blocks & entities for TuffClient (Eaglercraft 1.12)");
    }

    private PluginDescriptions() {}

    public static String get(String pluginKey) {
        return DESCRIPTIONS.getOrDefault(pluginKey, "No description available.");
    }

    public static Map<String, String> getAll() {
        return Map.copyOf(DESCRIPTIONS);
    }
}
