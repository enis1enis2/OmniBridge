package io.omnibrige.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;

import io.omnibrige.core.MessageManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.List;
import java.util.Locale;

public class OmniBridgeCommandVelocity implements SimpleCommand {

    private final ProxyServer proxyServer;

    public OmniBridgeCommandVelocity(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
    }

    private MessageManager msg() {
        return MessageManager.getInstance();
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!source.hasPermission("omnibrige.admin")) {
            source.sendMessage(Component.text(msg().msg("no-permission"), NamedTextColor.RED));
            return;
        }

        if (args.length == 0) {
            sendHelp(source);
            return;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "status" -> handleStatus(source);
            case "help" -> sendHelp(source);
            default -> sendHelp(source);
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length == 1) {
            return List.of("status", "help").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase(Locale.ROOT)))
                    .toList();
        }
        return List.of();
    }

    private void handleStatus(CommandSource source) {
        source.sendMessage(Component.empty());
        source.sendMessage(Component.text("  " + msg().msg("command.velocity.status-title"), NamedTextColor.GOLD, TextDecoration.BOLD));
        source.sendMessage(Component.text("  " + msg().msg("command.velocity.platform"), NamedTextColor.GRAY));
        source.sendMessage(Component.empty());

        int onlineCount = proxyServer.getPlayerCount();
        source.sendMessage(Component.text("  " + msg().msg("command.velocity.online-players", onlineCount), NamedTextColor.WHITE));
        source.sendMessage(Component.text("  " + msg().msg("command.velocity.servers", proxyServer.getAllServers().size()), NamedTextColor.WHITE));
        source.sendMessage(Component.empty());
    }

    private void sendHelp(CommandSource source) {
        source.sendMessage(Component.empty());
        source.sendMessage(Component.text("  " + msg().msg("command.velocity.help-title"), NamedTextColor.GOLD, TextDecoration.BOLD));
        source.sendMessage(Component.empty());
        source.sendMessage(Component.text("  /omnibrige status", NamedTextColor.AQUA)
                .append(Component.text("   - " + msg().msg("command.velocity.help-status"), NamedTextColor.GRAY)));
        source.sendMessage(Component.text("  /omnibrige help", NamedTextColor.AQUA)
                .append(Component.text("    - " + msg().msg("command.velocity.help-help"), NamedTextColor.GRAY)));
        source.sendMessage(Component.empty());
    }
}
