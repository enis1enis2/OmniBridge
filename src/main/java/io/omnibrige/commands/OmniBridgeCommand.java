package io.omnibrige.commands;

import io.omnibrige.OmniBridge;
import io.omnibrige.core.PluginManager;
import io.omnibrige.core.PluginManager.PluginStatus;
import io.omnibrige.download.Repository;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class OmniBridgeCommand implements CommandExecutor, TabCompleter {

    private final OmniBridge plugin;

    public OmniBridgeCommand(OmniBridge plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("omnibrige.admin")) {
            sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "install" -> handleInstall(sender);
            case "update" -> handleUpdate(sender);
            case "status" -> handleStatus(sender);
            case "reload" -> handleReload(sender);
            case "versions" -> handleVersions(sender);
            case "remove" -> handleRemove(sender, args);
            case "help" -> sendHelp(sender);
            default -> sendHelp(sender);
        }
        return true;
    }

    private void handleInstall(CommandSender sender) {
        sender.sendMessage(Component.text("Installing missing plugins...", NamedTextColor.YELLOW));
        CompletableFuture.runAsync(() -> {
            plugin.getPluginManager().installAll();
            sender.sendMessage(Component.text("Installation complete! Restart server to load new plugins.", NamedTextColor.GREEN));
        });
    }

    private void handleUpdate(CommandSender sender) {
        sender.sendMessage(Component.text("Checking for updates...", NamedTextColor.YELLOW));
        CompletableFuture.runAsync(() -> {
            plugin.getPluginManager().updateAll();
            sender.sendMessage(Component.text("Update check complete!", NamedTextColor.GREEN));
        });
    }

    private void handleStatus(CommandSender sender) {
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("  OmniBridge Status", NamedTextColor.GOLD, TextDecoration.BOLD));
        sender.sendMessage(Component.text("  Platform: " + plugin.getPlatform().name(), NamedTextColor.GRAY));
        sender.sendMessage(Component.empty());

        Map<String, PluginStatus> status = plugin.getPluginManager().getStatus();

        sender.sendMessage(Component.text("  Plugin                      Version          Status", NamedTextColor.AQUA));
        sender.sendMessage(Component.text("  " + "─".repeat(55), NamedTextColor.DARK_GRAY));

        for (var entry : status.entrySet()) {
            PluginStatus s = entry.getValue();
            Component name = Component.text("  " + String.format("%-28s", s.name()), NamedTextColor.WHITE);
            Component version = Component.text(String.format("%-17s", s.version()), NamedTextColor.GRAY);
            Component statusComp;
            if (s.installed() && s.enabled()) {
                statusComp = Component.text("ENABLED", NamedTextColor.GREEN);
            } else if (s.installed()) {
                statusComp = Component.text("INSTALLED", NamedTextColor.YELLOW);
            } else {
                statusComp = Component.text("MISSING", NamedTextColor.RED);
            }
            sender.sendMessage(name.append(version).append(statusComp));
        }
        sender.sendMessage(Component.empty());
    }

    private void handleReload(CommandSender sender) {
        plugin.getPluginManager().reloadAll();
        plugin.reloadConfig();
        sender.sendMessage(Component.text("All configs reloaded!", NamedTextColor.GREEN));
    }

    private void handleVersions(CommandSender sender) {
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("  Connected Player Versions", NamedTextColor.GOLD, TextDecoration.BOLD));
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
                    line = line.append(Component.text(" [Bedrock]", NamedTextColor.AQUA));
                }
                sender.sendMessage(line);
            }
        } else {
            for (var player : org.bukkit.Bukkit.getOnlinePlayers()) {
                Component line = Component.text("  " + player.getName(), NamedTextColor.WHITE);
                if (geyser.isBedrockPlayer(player.getUniqueId())) {
                    line = line.append(Component.text(" [Bedrock]", NamedTextColor.AQUA));
                }
                sender.sendMessage(line);
            }
        }
        sender.sendMessage(Component.empty());
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /omnibrige remove <plugin>", NamedTextColor.RED));
            return;
        }
        String pluginName = args[1];
        if (!Repository.isKnown(pluginName)) {
            sender.sendMessage(Component.text("Unknown plugin: " + pluginName, NamedTextColor.RED));
            return;
        }
        boolean removed = plugin.getPluginManager().removePlugin(pluginName);
        if (removed) {
            sender.sendMessage(Component.text("Removed " + Repository.getDisplayName(pluginName), NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("Plugin not found or already removed.", NamedTextColor.YELLOW));
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("  OmniBridge Commands", NamedTextColor.GOLD, TextDecoration.BOLD));
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("  /omnibrige install", NamedTextColor.AQUA)
                .append(Component.text("  - Install missing plugins", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /omnibrige update", NamedTextColor.AQUA)
                .append(Component.text("   - Update all plugins", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /omnibrige status", NamedTextColor.AQUA)
                .append(Component.text("   - Show plugin status", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /omnibrige versions", NamedTextColor.AQUA)
                .append(Component.text(" - Show player versions", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /omnibrige reload", NamedTextColor.AQUA)
                .append(Component.text("   - Reload all configs", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /omnibrige remove", NamedTextColor.AQUA)
                .append(Component.text("  - Remove a managed plugin", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /omnibrige help", NamedTextColor.AQUA)
                .append(Component.text("    - Show this help", NamedTextColor.GRAY)));
        sender.sendMessage(Component.empty());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("omnibrige.admin")) return List.of();

        if (args.length == 1) {
            return List.of("install", "update", "status", "reload", "versions", "remove", "help").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            return Repository.getAllPlugins().keySet().stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
