package io.omnibrige.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.List;

public class OmniBridgeCommandVelocity implements SimpleCommand {

    private final ProxyServer proxyServer;

    public OmniBridgeCommandVelocity(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 0) {
            sendHelp(source);
            return;
        }

        switch (args[0].toLowerCase()) {
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
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }

    private void handleStatus(CommandSource source) {
        source.sendMessage(Component.empty());
        source.sendMessage(Component.text("  OmniBridge Status (Velocity Proxy)", NamedTextColor.GOLD, TextDecoration.BOLD));
        source.sendMessage(Component.text("  Platform: VELOCITY", NamedTextColor.GRAY));
        source.sendMessage(Component.empty());

        int onlineCount = proxyServer.getPlayerCount();
        source.sendMessage(Component.text("  Online Players: " + onlineCount, NamedTextColor.WHITE));
        source.sendMessage(Component.text("  Registered Servers: " + proxyServer.getAllServers().size(), NamedTextColor.WHITE));
        source.sendMessage(Component.empty());
    }

    private void sendHelp(CommandSource source) {
        source.sendMessage(Component.empty());
        source.sendMessage(Component.text("  OmniBridge Commands (Velocity)", NamedTextColor.GOLD, TextDecoration.BOLD));
        source.sendMessage(Component.empty());
        source.sendMessage(Component.text("  /omnibrige status", NamedTextColor.AQUA)
                .append(Component.text("   - Show proxy status", NamedTextColor.GRAY)));
        source.sendMessage(Component.text("  /omnibrige help", NamedTextColor.AQUA)
                .append(Component.text("    - Show this help", NamedTextColor.GRAY)));
        source.sendMessage(Component.empty());
    }
}
