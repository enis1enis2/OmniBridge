package io.omnibrige.commands;

import io.omnibrige.OmniBridge;
import io.omnibrige.core.MessageManager;
import io.omnibrige.core.PluginDescriptions;
import io.omnibrige.core.PluginPresets;
import io.omnibrige.download.Repository;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class ChatConfigMenu {

    private final OmniBridge plugin;

    public ChatConfigMenu(OmniBridge plugin) {
        this.plugin = plugin;
    }

    private MessageManager msg() {
        return MessageManager.getInstance();
    }

    public void open(Player player) {
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("  ══════════════════════════════════════════════", NamedTextColor.DARK_GRAY));
        player.sendMessage(Component.text("  OmniBridge", NamedTextColor.GOLD, TextDecoration.BOLD)
                .append(Component.text(" — " + msg().msg("menu.title"), NamedTextColor.WHITE)));
        player.sendMessage(Component.text("  " + msg().msg("menu.click-hint"), NamedTextColor.DARK_GRAY));
        player.sendMessage(Component.text("  ══════════════════════════════════════════════", NamedTextColor.DARK_GRAY));
        player.sendMessage(Component.empty());

        player.sendMessage(Component.text("  " + msg().msg("menu.presets-title"), NamedTextColor.GOLD, TextDecoration.BOLD));
        for (var entry : PluginPresets.getAll().entrySet()) {
            PluginPresets.Preset preset = entry.getValue();
            Component line = Component.text("    " + entry.getKey(), NamedTextColor.AQUA, TextDecoration.BOLD)
                    .append(Component.text(" — " + preset.displayName(), NamedTextColor.WHITE))
                    .clickEvent(ClickEvent.runCommand("/ob preset " + entry.getKey()))
                    .hoverEvent(HoverEvent.showText(
                            Component.text(preset.description(), NamedTextColor.GRAY)
                                    .append(Component.newline())
                                    .append(Component.text(msg().msg("menu.click-to-apply"), NamedTextColor.GREEN))));
            player.sendMessage(line);
        }
        player.sendMessage(Component.empty());

        renderGroup(player, msg().msg("menu.group.viaversion"), List.of(
                "viaversion", "viabackwards", "viarewind",
                "viarewind-legacysupport", "viaprilfools", "viabungee"));

        renderGroup(player, msg().msg("menu.group.geysermc"), List.of(
                "geyser", "floodgate", "hurricane", "geyserconnect",
                "thirdpartycosmetics", "thunderbeta", "rainbow"));

        renderGroup(player, msg().msg("menu.group.integration"), List.of("authme", "tab"));

        renderGroup(player, msg().msg("menu.group.community"), List.of("protocolib", "tuffxplus"));

        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("  ─────────────────────────────────────────────", NamedTextColor.DARK_GRAY));

        int enabled = countEnabled();
        Component summary = Component.text("  " + msg().msg("menu.enabled-count", enabled), NamedTextColor.GRAY);
        if (enabled > 0) {
            summary = summary.append(Component.text(" — ", NamedTextColor.GRAY))
                    .append(Component.text("/ob install", NamedTextColor.AQUA, TextDecoration.BOLD)
                            .hoverEvent(HoverEvent.showText(Component.text(msg().msg("menu.click-to-install"), NamedTextColor.GREEN)))
                            .clickEvent(ClickEvent.runCommand("/ob install")))
                    .append(Component.text(msg().msg("menu.to-download"), NamedTextColor.GRAY));
        }
        player.sendMessage(summary);
        player.sendMessage(Component.empty());
    }

    private void renderGroup(Player player, String groupName, List<String> plugins) {
        player.sendMessage(Component.text("  " + groupName, NamedTextColor.AQUA, TextDecoration.BOLD));

        FileConfiguration config = plugin.getConfig();
        for (String key : plugins) {
            if (!Repository.isKnown(key)) continue;

            boolean enabled = config.getBoolean("managed-plugins." + key, false);
            String desc = PluginDescriptions.get(key);
            List<String> deps = Repository.getDependencies(key);

            Component checkbox = enabled
                    ? Component.text("[✓]", NamedTextColor.GREEN, TextDecoration.BOLD)
                    : Component.text("[ ]", NamedTextColor.DARK_GRAY);
            Component name = Component.text(" " + padRight(Repository.getDisplayName(key), 20), NamedTextColor.WHITE);
            Component description = Component.text(desc, NamedTextColor.GRAY);

            Component line = checkbox.append(name).append(description);
            line = line.clickEvent(ClickEvent.runCommand("/ob toggle " + key));
            line = line.hoverEvent(HoverEvent.showText(
                    Component.text(enabled ? msg().msg("menu.click-to-disable") : msg().msg("menu.click-to-enable"), NamedTextColor.YELLOW)));

            player.sendMessage(line);

            for (String dep : deps) {
                boolean depEnabled = config.getBoolean("managed-plugins." + dep, false);
                Component depLine = Component.text("      └ ", NamedTextColor.DARK_GRAY)
                        .append(Component.text(Repository.getDisplayName(dep), NamedTextColor.DARK_GRAY))
                        .append(Component.text(" (", NamedTextColor.DARK_GRAY));
                if (depEnabled) {
                    depLine = depLine.append(Component.text(msg().msg("menu.dep-auto"), NamedTextColor.DARK_GREEN));
                } else {
                    depLine = depLine.append(Component.text(msg().msg("menu.dep-required"), NamedTextColor.DARK_RED));
                }
                depLine = depLine.append(Component.text(")", NamedTextColor.DARK_GRAY));
                player.sendMessage(depLine);
            }
        }
        player.sendMessage(Component.empty());
    }

    public void togglePlugin(Player player, String pluginKey) {
        if (!Repository.isKnown(pluginKey)) {
            player.sendMessage(Component.text(msg().msg("menu.unknown-plugin", pluginKey), NamedTextColor.RED));
            return;
        }

        FileConfiguration config = plugin.getConfig();
        String path = "managed-plugins." + pluginKey;
        boolean current = config.getBoolean(path, false);

        boolean wasEmpty = countEnabled() == 0;
        config.set(path, !current);
        plugin.saveConfig();

        if (wasEmpty && !current) {
            plugin.stopReminder();
        }

        String msgKey = current ? "menu.plugin-disabled" : "menu.plugin-enabled";
        player.sendMessage(Component.text("  " + msg().msg(msgKey, Repository.getDisplayName(pluginKey)), NamedTextColor.GREEN));

        open(player);
    }

    private int countEnabled() {
        var section = plugin.getConfig().getConfigurationSection("managed-plugins");
        if (section == null) return 0;
        int count = 0;
        for (String key : section.getKeys(false)) {
            if (section.getBoolean(key)) count++;
        }
        return count;
    }

    private String padRight(String s, int n) {
        if (s.length() >= n) return s;
        return s + " ".repeat(n - s.length());
    }
}
