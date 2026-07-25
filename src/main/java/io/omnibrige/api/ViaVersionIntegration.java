package io.omnibrige.api;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class ViaVersionIntegration {

    private final Plugin viaVersionPlugin;

    public ViaVersionIntegration(Plugin hostPlugin) {
        this.viaVersionPlugin = Bukkit.getPluginManager().getPlugin("ViaVersion");
    }

    public boolean isAvailable() {
        return viaVersionPlugin != null && viaVersionPlugin.isEnabled();
    }

    public Map<UUID, String> getConnectedVersions() {
        Map<UUID, String> versions = new HashMap<>();
        if (!isAvailable()) return versions;

        for (Player player : Bukkit.getOnlinePlayers()) {
            String version = getPlayerVersion(player.getUniqueId());
            if (version != null) {
                versions.put(player.getUniqueId(), version);
            }
        }
        return versions;
    }

    public String getPlayerVersion(UUID uuid) {
        if (!isAvailable()) return null;
        try {
            Class<?> viaApiClass = Class.forName("com.viaversion.viaversion.api.Via");
            Object viaApi = viaApiClass.getMethod("getAPI").invoke(null);
            Object connection = viaApi.getClass().getMethod("getConnection", UUID.class).invoke(viaApi, uuid);
            if (connection != null) {
                Object protocolVersion = connection.getClass().getMethod("getProtocolVersion").invoke(connection);
                return protocolVersion.toString();
            }
        } catch (Exception ignored) {}
        return null;
    }

    public List<String> getSupportedVersions() {
        if (!isAvailable()) return List.of();
        try {
            Class<?> viaApiClass = Class.forName("com.viaversion.viaversion.api.Via");
            Object viaApi = viaApiClass.getMethod("getAPI").invoke(null);
            Object protocolVersion = Class.forName("com.viaversion.viaversion.api.protocol.version.ProtocolVersion");
            // Fallback to known versions
        } catch (Exception ignored) {}
        return List.of("1.7.x", "1.8.x", "1.9.x", "1.10.x", "1.11.x", "1.12.x",
                "1.13.x", "1.14.x", "1.15.x", "1.16.x", "1.17.x", "1.18.x",
                "1.19.x", "1.20.x", "1.21.x", "1.22.x+");
    }
}
