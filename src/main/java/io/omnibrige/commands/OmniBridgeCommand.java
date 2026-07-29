/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * OmniBridge - Universal cross-version and cross-platform connectivity
 * Copyright (c) 2026 OmniBridge Contributors
 */

package io.omnibrige.commands;

import io.omnibrige.OmniBridge;
import io.omnibrige.core.MessageManager;
import io.omnibrige.core.PluginDescriptions;
import io.omnibrige.core.PluginManager;
import io.omnibrige.core.PluginManager.PluginStatus;
import io.omnibrige.core.PluginPresets;
import io.omnibrige.download.DownloadService;
import io.omnibrige.download.Repository;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.Locale;

/**
 * Handles the /omnibrige (alias /ob) command on Paper/Spigot servers.
 * Provides subcommands for plugin management, status, presets, and configuration.
 */
public class OmniBridgeCommand implements CommandExecutor, TabCompleter {

    private final OmniBridge plugin;
    private final ChatConfigMenu chatMenu;

    /**
     * Constructs the command handler.
     *
     * @param plugin the OmniBridge plugin instance
     */
    public OmniBridgeCommand(OmniBridge plugin) {
        this.plugin = plugin;
        this.chatMenu = new ChatConfigMenu(plugin);
    }

    private MessageManager msg() {
        return MessageManager.getInstance();
    }

    /**
     * Executes the /omnibrige command with the given arguments.
     *
     * @param sender the command sender
     * @param command the command definition
     * @param label the alias used to invoke the command
     * @param args the command arguments
     * @return always true to suppress usage message
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("omnibrige.admin")) {
            sender.sendMessage(Component.text(msg().msg("no-permission"), NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "install" -> handleInstall(sender);
            case "update" -> handleUpdate(sender);
            case "check" -> handleCheck(sender);
            case "status" -> handleStatus(sender);
            case "reload" -> handleReload(sender);
            case "versions" -> handleVersions(sender);
            case "remove" -> handleRemove(sender, args);
            case "setup" -> handleSetup(sender);
            case "toggle" -> handleToggle(sender, args);
            case "preset" -> handlePreset(sender, args);
            case "info" -> handleInfo(sender, args);
            case "enable-all" -> handleEnableAll(sender);
            case "disable-all" -> handleDisableAll(sender);
            case "help" -> sendHelp(sender);
            default -> sendHelp(sender);
        }
        return true;
    }

    private void handleInstall(CommandSender sender) {
        sender.sendMessage(Component.text(msg().msg("command.install.start"), NamedTextColor.YELLOW));
        plugin.getPluginManager().installAllAsync(() ->
                sender.sendMessage(Component.text(msg().msg("command.install.complete"), NamedTextColor.GREEN))
        );
    }

    private void handleUpdate(CommandSender sender) {
        sender.sendMessage(Component.text(msg().msg("command.update.start"), NamedTextColor.YELLOW));
        plugin.getPluginManager().updateAllAsync(() ->
                sender.sendMessage(Component.text(msg().msg("command.update.complete"), NamedTextColor.GREEN))
        );
    }

    private void handleCheck(CommandSender sender) {
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("  " + msg().msg("command.check.title"), NamedTextColor.YELLOW));
        sender.sendMessage(Component.empty());

        DownloadService ds = new DownloadService(plugin.getLogger());
        for (var entry : Repository.getAllPlugins().entrySet()) {
            String key = entry.getKey();
            Repository.PluginInfo info = entry.getValue();
            String currentVersion = plugin.getPluginManager().getInstalledVersion(key);
            if (currentVersion == null) continue;

            boolean updateAvailable = ds.checkUpdate(info.url(), currentVersion);
            Component name = Component.text("  " + info.displayName(), NamedTextColor.WHITE);
            Component version = Component.text(" (" + currentVersion + ") ", NamedTextColor.GRAY);

            if (updateAvailable) {
                sender.sendMessage(name.append(version)
                        .append(Component.text(msg().msg("command.check.available"), NamedTextColor.YELLOW)));
            } else {
                sender.sendMessage(name.append(version)
                        .append(Component.text(msg().msg("command.check.uptodate"), NamedTextColor.DARK_GREEN)));
            }
        }
        sender.sendMessage(Component.empty());
    }

    private void handleSetup(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text(msg().msg("command.setup.only-ingame"), NamedTextColor.RED));
            return;
        }
        chatMenu.open(player);
    }

    private void handleToggle(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text(msg().msg("command.toggle.only-ingame"), NamedTextColor.RED));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(Component.text(msg().msg("command.toggle.usage"), NamedTextColor.RED));
            return;
        }
        chatMenu.togglePlugin(player, args[1]);
    }

    private void handleStatus(CommandSender sender) {
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("  " + msg().msg("command.status.title"), NamedTextColor.GOLD, TextDecoration.BOLD));
        sender.sendMessage(Component.text("  " + msg().msg("command.status.platform", plugin.getPlatform().name()), NamedTextColor.GRAY));
        sender.sendMessage(Component.empty());

        Map<String, PluginStatus> status = plugin.getPluginManager().getStatus();

        sender.sendMessage(Component.text(msg().msg("command.status.header"), NamedTextColor.AQUA));
        sender.sendMessage(Component.text("  " + "─".repeat(55), NamedTextColor.DARK_GRAY));

        for (var entry : status.entrySet()) {
            PluginStatus s = entry.getValue();
            Component name = Component.text("  " + String.format("%-28s", s.name()), NamedTextColor.WHITE);
            Component version = Component.text(String.format("%-17s", s.version()), NamedTextColor.GRAY);
            Component statusComp;
            if (s.installed() && s.enabled()) {
                statusComp = Component.text(msg().msg("command.status.enabled"), NamedTextColor.GREEN);
            } else if (s.installed()) {
                statusComp = Component.text(msg().msg("command.status.installed"), NamedTextColor.YELLOW);
            } else {
                statusComp = Component.text(msg().msg("command.status.missing"), NamedTextColor.RED);
            }
            sender.sendMessage(name.append(version).append(statusComp));
        }
        sender.sendMessage(Component.empty());
    }

    private void handleReload(CommandSender sender) {
        plugin.getPluginManager().reloadAll();
        plugin.reloadConfig();
        String locale = plugin.getConfig().getString("locale", "en_US");
        MessageManager.getInstance().reload(locale);
        sender.sendMessage(Component.text(msg().msg("command.reload.done"), NamedTextColor.GREEN));
    }

    private void handleVersions(CommandSender sender) {
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("  " + msg().msg("command.versions.title"), NamedTextColor.GOLD, TextDecoration.BOLD));
        sender.sendMessage(Component.empty());

        var viaVersion = plugin.getViaVersionIntegration();
        var geyser = plugin.getGeyserIntegration();

        if (viaVersion.isAvailable()) {
            Map<UUID, String> versions = viaVersion.getConnectedVersions();
            for (var entry : versions.entrySet()) {
                String name = org.bukkit.Bukkit.getOfflinePlayer(entry.getKey()).getName();
                Component line = Component.text("  " + (name != null ? name : entry.getKey().toString()), NamedTextColor.WHITE)
                        .append(Component.text(" - " + entry.getValue(), NamedTextColor.GRAY));
                if (geyser.isBedrockPlayer(entry.getKey())) {
                    line = line.append(Component.text(msg().msg("command.versions.bedrock-tag"), NamedTextColor.AQUA));
                }
                sender.sendMessage(line);
            }
        } else {
            for (var player : org.bukkit.Bukkit.getOnlinePlayers()) {
                Component line = Component.text("  " + player.getName(), NamedTextColor.WHITE);
                if (geyser.isBedrockPlayer(player.getUniqueId())) {
                    line = line.append(Component.text(msg().msg("command.versions.bedrock-tag"), NamedTextColor.AQUA));
                }
                sender.sendMessage(line);
            }
        }
        sender.sendMessage(Component.empty());
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text(msg().msg("command.remove.usage"), NamedTextColor.RED));
            return;
        }
        String pluginName = args[1];
        if (!Repository.isKnown(pluginName)) {
            sender.sendMessage(Component.text(msg().msg("command.remove.unknown", pluginName), NamedTextColor.RED));
            return;
        }
        boolean removed = plugin.getPluginManager().removePlugin(pluginName);
        if (removed) {
            sender.sendMessage(Component.text(msg().msg("command.remove.done", Repository.getDisplayName(pluginName)), NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text(msg().msg("command.remove.not-found"), NamedTextColor.YELLOW));
        }
    }

    private void handleEnableAll(CommandSender sender) {
        var config = plugin.getConfig();
        int count = 0;
        for (String key : Repository.getAllPlugins().keySet()) {
            if (!config.getBoolean("managed-plugins." + key, false)) {
                config.set("managed-plugins." + key, true);
                count++;
            }
        }
        plugin.saveConfig();
        boolean wasEmpty = countEnabled() == count;
        if (wasEmpty) plugin.stopReminder();
        sender.sendMessage(Component.text("  " + msg().msg("command.enable-all.done", count), NamedTextColor.GREEN));
        if (sender instanceof Player player) handleSetup(player);
    }

    private void handleDisableAll(CommandSender sender) {
        var config = plugin.getConfig();
        int count = 0;
        for (String key : Repository.getAllPlugins().keySet()) {
            if (config.getBoolean("managed-plugins." + key, false)) {
                config.set("managed-plugins." + key, false);
                count++;
            }
        }
        plugin.saveConfig();
        sender.sendMessage(Component.text("  " + msg().msg("command.disable-all.done", count), NamedTextColor.YELLOW));
        if (sender instanceof Player player) handleSetup(player);
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

    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text(msg().msg("command.info.usage"), NamedTextColor.RED));
            return;
        }
        String key = args[1].toLowerCase(Locale.ROOT);
        if (!Repository.isKnown(key)) {
            sender.sendMessage(Component.text(msg().msg("command.info.unknown", key), NamedTextColor.RED));
            return;
        }
        Repository.PluginInfo info = Repository.getAllPlugins().get(key);
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("  " + info.displayName(), NamedTextColor.GOLD, TextDecoration.BOLD));
        sender.sendMessage(Component.text("    " + msg().msg("command.info.type") + ": " + info.type(), NamedTextColor.GRAY));
        sender.sendMessage(Component.text("    " + msg().msg("command.info.description") + ": " + PluginDescriptions.get(key), NamedTextColor.GRAY));
        sender.sendMessage(Component.text("    " + msg().msg("command.info.jar") + ": " + info.jarName(), NamedTextColor.GRAY));
        List<String> deps = Repository.getDependencies(key);
        if (!deps.isEmpty()) {
            sender.sendMessage(Component.text("    " + msg().msg("command.info.dependencies") + ": "
                    + String.join(", ", deps), NamedTextColor.GRAY));
        }
        sender.sendMessage(Component.empty());
    }

    private void handlePreset(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.empty());
            sender.sendMessage(Component.text("  " + msg().msg("command.preset.title"), NamedTextColor.GOLD, TextDecoration.BOLD));
            sender.sendMessage(Component.empty());
            for (var entry : PluginPresets.getAll().entrySet()) {
                PluginPresets.Preset p = entry.getValue();
                sender.sendMessage(Component.text("  /ob preset " + entry.getKey(), NamedTextColor.AQUA)
                        .append(Component.text(" - " + p.displayName(), NamedTextColor.WHITE)));
                sender.sendMessage(Component.text("    " + p.description(), NamedTextColor.GRAY));
                sender.sendMessage(Component.text("    Plugins: " + String.join(", ", p.plugins()), NamedTextColor.DARK_GRAY));
            }
            sender.sendMessage(Component.empty());
            return;
        }

        String presetKey = args[1];
        if (!PluginPresets.isKnown(presetKey)) {
            sender.sendMessage(Component.text(msg().msg("command.preset.unknown", presetKey), NamedTextColor.RED));
            return;
        }

        PluginPresets.Preset preset = PluginPresets.get(presetKey);
        var config = plugin.getConfig();

        int enabled = 0;
        for (String pluginKey : preset.plugins()) {
            if (Repository.isKnown(pluginKey) && !config.getBoolean("managed-plugins." + pluginKey, false)) {
                config.set("managed-plugins." + pluginKey, true);
                enabled++;
            }
        }
        plugin.saveConfig();

        sender.sendMessage(Component.text("  " + msg().msg("command.preset.activated", preset.displayName()), NamedTextColor.GREEN)
                .append(Component.text("", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("  " + msg().msg("command.preset.enabled-count", enabled), NamedTextColor.GREEN));
        sender.sendMessage(Component.text("  " + msg().msg("command.preset.hint"), NamedTextColor.GRAY));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("  " + msg().msg("command.help.title"), NamedTextColor.GOLD, TextDecoration.BOLD));
        sender.sendMessage(Component.empty());

        sender.sendMessage(Component.text("  /ob setup", NamedTextColor.AQUA)
                .append(Component.text("    - " + msg().msg("command.help.setup"), NamedTextColor.GRAY))
                .hoverEvent(HoverEvent.showText(Component.text(msg().msg("command.help.setup"), NamedTextColor.GREEN)))
                .clickEvent(ClickEvent.runCommand("/ob setup")));
        sender.sendMessage(Component.text("  /ob preset", NamedTextColor.AQUA)
                .append(Component.text("   - " + msg().msg("command.help.preset"), NamedTextColor.GRAY))
                .hoverEvent(HoverEvent.showText(Component.text(msg().msg("command.help.preset-hint"), NamedTextColor.YELLOW)))
                .clickEvent(ClickEvent.runCommand("/ob preset")));
        sender.sendMessage(Component.text("  /ob install", NamedTextColor.AQUA)
                .append(Component.text("   - " + msg().msg("command.help.install"), NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /ob update", NamedTextColor.AQUA)
                .append(Component.text("    - " + msg().msg("command.help.update"), NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /ob check", NamedTextColor.AQUA)
                .append(Component.text("     - " + msg().msg("command.help.check"), NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /ob status", NamedTextColor.AQUA)
                .append(Component.text("    - " + msg().msg("command.help.status"), NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /ob versions", NamedTextColor.AQUA)
                .append(Component.text("  - " + msg().msg("command.help.versions"), NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /ob reload", NamedTextColor.AQUA)
                .append(Component.text("    - " + msg().msg("command.help.reload"), NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /ob remove", NamedTextColor.AQUA)
                .append(Component.text("   - " + msg().msg("command.help.remove"), NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /ob info [plugin]", NamedTextColor.AQUA)
                .append(Component.text(" - " + msg().msg("command.help.info"), NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /ob enable-all", NamedTextColor.AQUA)
                .append(Component.text(" - " + msg().msg("command.help.enable-all"), NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /ob disable-all", NamedTextColor.AQUA)
                .append(Component.text("- " + msg().msg("command.help.disable-all"), NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /ob help", NamedTextColor.AQUA)
                .append(Component.text("     - " + msg().msg("command.help.help"), NamedTextColor.GRAY)));
        sender.sendMessage(Component.empty());
    }

    /**
     * Provides tab completion for the /omnibrige command.
     *
     * @param sender the command sender
     * @param command the command definition
     * @param label the alias used to invoke the command
     * @param args the partial command arguments
     * @return a list of matching completions
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("omnibrige.admin")) return List.of();

        if (args.length == 1) {
            return List.of("setup", "preset", "install", "update", "check", "status",
                            "reload", "versions", "remove", "toggle", "info",
                            "enable-all", "disable-all", "help").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase(Locale.ROOT)))
                    .toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("preset")) {
            return PluginPresets.getAll().keySet().stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase(Locale.ROOT)))
                    .toList();
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("toggle") || args[0].equalsIgnoreCase("info"))) {
            return Repository.getAllPlugins().keySet().stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase(Locale.ROOT)))
                    .toList();
        }
        return List.of();
    }
}
