/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * OmniBridge - Universal cross-version and cross-platform connectivity
 * Copyright (c) 2026 OmniBridge Contributors
 */

package io.omnibrige.api;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

/**
 * Integration layer for detecting client protocol versions via ViaVersion.
 *
 * <p>Uses reflection against ViaVersion's <em>public interfaces</em>
 * ({@code Via}, {@code ViaAPI}, {@code UserConnection}, {@code ProtocolInfo},
 * {@code ProtocolVersion}) so that the integration keeps working when ViaVersion
 * relocates or changes its implementation classes.</p>
 */
public class ViaVersionIntegration {

    private static final String VIA_CLASS = "com.viaversion.viaversion.api.Via";
    private static final String VIA_API_INTERFACE = "com.viaversion.viaversion.api.ViaAPI";
    private static final String USER_CONNECTION_INTERFACE =
            "com.viaversion.viaversion.api.connection.UserConnection";
    private static final String PROTOCOL_INFO_INTERFACE =
            "com.viaversion.viaversion.api.connection.ProtocolInfo";
    private static final String PROTOCOL_VERSION_CLASS =
            "com.viaversion.viaversion.api.protocol.version.ProtocolVersion";

    /**
     * Fallback protocol-to-version map used only when ViaVersion does not expose a
     * {@code ProtocolVersion} object (very old builds). Modern ViaVersion provides
     * the display name itself.
     */
    private static final Map<Integer, String> PROTOCOL_VERSIONS = new LinkedHashMap<>();

    static {
        PROTOCOL_VERSIONS.put(4, "1.7.2-1.7.5");
        PROTOCOL_VERSIONS.put(5, "1.7.6-1.7.10");
        PROTOCOL_VERSIONS.put(47, "1.8.x");
        PROTOCOL_VERSIONS.put(107, "1.9");
        PROTOCOL_VERSIONS.put(108, "1.9.1");
        PROTOCOL_VERSIONS.put(109, "1.9.2");
        PROTOCOL_VERSIONS.put(110, "1.9.3-1.9.4");
        PROTOCOL_VERSIONS.put(210, "1.10.x");
        PROTOCOL_VERSIONS.put(315, "1.11");
        PROTOCOL_VERSIONS.put(316, "1.11.1-1.11.2");
        PROTOCOL_VERSIONS.put(335, "1.12");
        PROTOCOL_VERSIONS.put(338, "1.12.1");
        PROTOCOL_VERSIONS.put(340, "1.12.2");
        PROTOCOL_VERSIONS.put(393, "1.13");
        PROTOCOL_VERSIONS.put(401, "1.13.1");
        PROTOCOL_VERSIONS.put(404, "1.13.2");
        PROTOCOL_VERSIONS.put(477, "1.14");
        PROTOCOL_VERSIONS.put(480, "1.14.1");
        PROTOCOL_VERSIONS.put(485, "1.14.2");
        PROTOCOL_VERSIONS.put(490, "1.14.3");
        PROTOCOL_VERSIONS.put(498, "1.14.4");
        PROTOCOL_VERSIONS.put(573, "1.15");
        PROTOCOL_VERSIONS.put(575, "1.15.1");
        PROTOCOL_VERSIONS.put(578, "1.15.2");
        PROTOCOL_VERSIONS.put(735, "1.16");
        PROTOCOL_VERSIONS.put(736, "1.16.1");
        PROTOCOL_VERSIONS.put(751, "1.16.2");
        PROTOCOL_VERSIONS.put(753, "1.16.3");
        PROTOCOL_VERSIONS.put(754, "1.16.4-1.16.5");
        PROTOCOL_VERSIONS.put(755, "1.17");
        PROTOCOL_VERSIONS.put(756, "1.17.1");
        PROTOCOL_VERSIONS.put(757, "1.18-1.18.1");
        PROTOCOL_VERSIONS.put(758, "1.18.2");
        PROTOCOL_VERSIONS.put(759, "1.19");
        PROTOCOL_VERSIONS.put(760, "1.19.1-1.19.2");
        PROTOCOL_VERSIONS.put(761, "1.19.3");
        PROTOCOL_VERSIONS.put(762, "1.19.4");
        PROTOCOL_VERSIONS.put(763, "1.20-1.20.1");
        PROTOCOL_VERSIONS.put(764, "1.20.2");
        PROTOCOL_VERSIONS.put(765, "1.20.3-1.20.4");
        PROTOCOL_VERSIONS.put(766, "1.20.5-1.20.6");
        PROTOCOL_VERSIONS.put(767, "1.21-1.21.1");
        PROTOCOL_VERSIONS.put(768, "1.21.2-1.21.3");
        PROTOCOL_VERSIONS.put(769, "1.21.4");
        PROTOCOL_VERSIONS.put(770, "1.21.5");
        PROTOCOL_VERSIONS.put(771, "1.21.6");
        PROTOCOL_VERSIONS.put(772, "1.21.7-1.21.8");
        PROTOCOL_VERSIONS.put(773, "1.21.9-1.21.10");
        PROTOCOL_VERSIONS.put(774, "1.21.11");
        PROTOCOL_VERSIONS.put(775, "26.1-26.1.2");
        PROTOCOL_VERSIONS.put(776, "26.2");
        PROTOCOL_VERSIONS.put(777, "26.3");
    }

    private final Plugin viaVersionPlugin;

    /**
     * Constructs a ViaVersionIntegration instance.
     *
     * @param hostPlugin the host plugin providing the Bukkit runtime context
     */
    public ViaVersionIntegration(Plugin hostPlugin) {
        this.viaVersionPlugin = Bukkit.getPluginManager().getPlugin("ViaVersion");
    }

    /**
     * Checks if the ViaVersion plugin is present and enabled.
     *
     * @return true if ViaVersion is available for use
     */
    public boolean isAvailable() {
        return viaVersionPlugin != null && viaVersionPlugin.isEnabled();
    }

    /**
     * Returns the protocol version strings for all connected players.
     *
     * @return a map of player UUIDs to their Minecraft version strings
     */
    public Map<UUID, String> getConnectedVersions() {
        Map<UUID, String> versions = new LinkedHashMap<>();
        if (!isAvailable()) return versions;

        for (Player player : Bukkit.getOnlinePlayers()) {
            String version = getPlayerVersion(player.getUniqueId());
            if (version != null) {
                versions.put(player.getUniqueId(), version);
            }
        }
        return versions;
    }

    /**
     * Returns the Minecraft version string for a specific player.
     *
     * @param uuid the player's UUID
     * @return the version string (e.g. "1.21.4"), or null if ViaVersion is unavailable
     *         or does not report a protocol version for the player
     */
    public String getPlayerVersion(UUID uuid) {
        if (!isAvailable() || uuid == null) return null;
        try {
            Object viaApi = Class.forName(VIA_CLASS).getMethod("getAPI").invoke(null);
            Class<?> viaApiInterface = Class.forName(VIA_API_INTERFACE);

            // ViaVersion 5+: ViaAPI#getPlayerProtocolVersion(UUID) returns ProtocolVersion.
            try {
                Object protocolVersion = viaApiInterface
                        .getMethod("getPlayerProtocolVersion", UUID.class)
                        .invoke(viaApi, uuid);
                String name = protocolVersionName(protocolVersion);
                if (name != null) return name;
            } catch (NoSuchMethodException ignored) {
                // Older ViaVersion - fall through to the connection based lookup.
            }

            // Older ViaVersion: ViaAPI#getConnection(UUID) -> ProtocolInfo.
            Object connection = viaApiInterface
                    .getMethod("getConnection", UUID.class)
                    .invoke(viaApi, uuid);
            if (connection == null) return null;

            Class<?> userConnectionInterface = Class.forName(USER_CONNECTION_INTERFACE);
            Object protocolInfo = userConnectionInterface
                    .getMethod("getProtocolInfo")
                    .invoke(connection);
            if (protocolInfo == null) return null;

            Class<?> protocolInfoInterface = Class.forName(PROTOCOL_INFO_INTERFACE);
            try {
                Object protocolVersion = protocolInfoInterface
                        .getMethod("protocolVersion")
                        .invoke(protocolInfo);
                String name = protocolVersionName(protocolVersion);
                if (name != null) return name;
            } catch (NoSuchMethodException ignored) {
                // Fall through to the deprecated int accessor.
            }

            Object raw = protocolInfoInterface.getMethod("getProtocolVersion").invoke(protocolInfo);
            if (raw instanceof Number number) {
                return displayName(number.intValue());
            }
        } catch (Exception ignored) {
            // ViaVersion not fully initialized or API changed - report as unknown.
        }
        return null;
    }

    /**
     * Returns all Minecraft version strings supported by the ViaVersion protocol map.
     *
     * @return an immutable list of version strings
     */
    public List<String> getSupportedVersions() {
        return List.copyOf(PROTOCOL_VERSIONS.values());
    }

    private static String protocolVersionName(Object protocolVersion) {
        if (protocolVersion == null) return null;
        try {
            Class<?> protocolVersionClass = Class.forName(PROTOCOL_VERSION_CLASS);
            Object name = protocolVersionClass.getMethod("getName").invoke(protocolVersion);
            if (name instanceof String text && !text.isBlank() && !"UNKNOWN".equalsIgnoreCase(text)) {
                return text;
            }
            Object version = protocolVersionClass.getMethod("getVersion").invoke(protocolVersion);
            if (version instanceof Number number) {
                return displayName(number.intValue());
            }
        } catch (Exception ignored) {
            // Unknown implementation detail - fall back to the static map by caller.
        }
        return null;
    }

    private static String displayName(int protocol) {
        return PROTOCOL_VERSIONS.getOrDefault(protocol, "Unknown (" + protocol + ")");
    }
}
