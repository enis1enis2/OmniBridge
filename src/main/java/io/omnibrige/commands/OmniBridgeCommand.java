package io.omnibrige.commands;

import io.omnibrige.OmniBridge;
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

public class OmniBridgeCommand implements CommandExecutor, TabCompleter {

    private final OmniBridge plugin;
    private final ChatConfigMenu chatMenu;

    public OmniBridgeCommand(OmniBridge plugin) {
        this.plugin = plugin;
        this.chatMenu = new ChatConfigMenu(plugin);
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
            case "help" -> sendHelp(sender);
            default -> sendHelp(sender);
        }
        return true;
    }

    private void handleInstall(CommandSender sender) {
        sender.sendMessage(Component.text("Installing missing plugins...", NamedTextColor.YELLOW));
        plugin.getPluginManager().installAllAsync(() ->
                sender.sendMessage(Component.text("Installation complete! Restart server to load new plugins.", NamedTextColor.GREEN))
        );
    }

    private void handleUpdate(CommandSender sender) {
        sender.sendMessage(Component.text("Checking for updates...", NamedTextColor.YELLOW));
        plugin.getPluginManager().updateAllAsync(() ->
                sender.sendMessage(Component.text("Update check complete!", NamedTextColor.GREEN))
        );
    }

    private void handleCheck(CommandSender sender) {
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("  Checking for updates...", NamedTextColor.YELLOW));
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
                        .append(Component.text("UPDATE AVAILABLE", NamedTextColor.YELLOW)));
            } else {
                sender.sendMessage(name.append(version)
                        .append(Component.text("up to date", NamedTextColor.DARK_GREEN)));
            }
        }
        sender.sendMessage(Component.empty());
    }

    private void handleSetup(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Setup menu is only available in-game.", NamedTextColor.RED));
            return;
        }
        chatMenu.open(player);
    }

    private void handleToggle(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Toggle is only available in-game.", NamedTextColor.RED));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /ob toggle <plugin>", NamedTextColor.RED));
            return;
        }
        chatMenu.togglePlugin(player, args[1]);
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

    private void handlePreset(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.empty());
            sender.sendMessage(Component.text("  Available Presets", NamedTextColor.GOLD, TextDecoration.BOLD));
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
            sender.sendMessage(Component.text("Unknown preset: " + presetKey, NamedTextColor.RED));
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

        sender.sendMessage(Component.text("  Activated preset: ", NamedTextColor.GREEN)
                .append(Component.text(preset.displayName(), NamedTextColor.WHITE, TextDecoration.BOLD)));
        sender.sendMessage(Component.text("  Enabled " + enabled + " plugin(s)", NamedTextColor.GREEN));
        sender.sendMessage(Component.text("  Run ", NamedTextColor.GRAY)
                .append(Component.text("/ob install", NamedTextColor.AQUA))
                .append(Component.text(" to download them", NamedTextColor.GRAY)));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("  OmniBridge Commands", NamedTextColor.GOLD, TextDecoration.BOLD));
        sender.sendMessage(Component.empty());

        sender.sendMessage(Component.text("  /ob setup", NamedTextColor.AQUA)
                .append(Component.text("    - Open interactive plugin setup", NamedTextColor.GRAY))
                .hoverEvent(HoverEvent.showText(Component.text("Click to open setup menu", NamedTextColor.GREEN)))
                .clickEvent(ClickEvent.runCommand("/ob setup")));
        sender.sendMessage(Component.text("  /ob preset", NamedTextColor.AQUA)
                .append(Component.text("   - Apply a plugin preset group", NamedTextColor.GRAY))
                .hoverEvent(HoverEvent.showText(Component.text("bedrock, full-version, essentials, max-compat", NamedTextColor.YELLOW)))
                .clickEvent(ClickEvent.runCommand("/ob preset")));
        sender.sendMessage(Component.text("  /ob install", NamedTextColor.AQUA)
                .append(Component.text("   - Download & install enabled plugins", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /ob update", NamedTextColor.AQUA)
                .append(Component.text("    - Check for & apply updates", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /ob check", NamedTextColor.AQUA)
                .append(Component.text("     - Show which plugins need updates", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /ob status", NamedTextColor.AQUA)
                .append(Component.text("    - Show plugin status", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /ob versions", NamedTextColor.AQUA)
                .append(Component.text("  - Show player versions", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /ob reload", NamedTextColor.AQUA)
                .append(Component.text("    - Reload all configs", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /ob remove", NamedTextColor.AQUA)
                .append(Component.text("   - Remove a managed plugin", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  /ob help", NamedTextColor.AQUA)
                .append(Component.text("     - Show this help", NamedTextColor.GRAY)));
        sender.sendMessage(Component.empty());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("omnibrige.admin")) return List.of();

        if (args.length == 1) {
            return List.of("setup", "preset", "install", "update", "check", "status",
                            "reload", "versions", "remove", "toggle", "help").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase(Locale.ROOT)))
                    .toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("preset")) {
            return PluginPresets.getAll().keySet().stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase(Locale.ROOT)))
                    .toList();
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("toggle"))) {
            return Repository.getAllPlugins().keySet().stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase(Locale.ROOT)))
                    .toList();
        }
        return List.of();
    }
}
