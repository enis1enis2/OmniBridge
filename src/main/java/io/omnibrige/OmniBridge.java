package io.omnibrige;

import io.omnibrige.api.GeyserIntegration;
import io.omnibrige.api.OmniBridgeAPI;
import io.omnibrige.api.ViaVersionIntegration;
import io.omnibrige.commands.OmniBridgeCommand;
import io.omnibrige.core.ConfigManager;
import io.omnibrige.core.PlatformDetector;
import io.omnibrige.core.PlatformDetector.Platform;
import io.omnibrige.core.PluginManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class OmniBridge extends JavaPlugin {

    private static OmniBridge instance;
    private Platform platform;
    private PluginManager pluginManager;
    private ConfigManager configManager;
    private ViaVersionIntegration viaVersionIntegration;
    private GeyserIntegration geyserIntegration;
    private BukkitTask reminderTask;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        platform = PlatformDetector.detect();
        getLogger().info("Detected platform: " + platform.name());

        viaVersionIntegration = new ViaVersionIntegration(this);
        geyserIntegration = new GeyserIntegration(this);

        configManager = new ConfigManager(this, platform);
        pluginManager = new PluginManager(this, platform, configManager);

        getCommand("omnibrige").setExecutor(new OmniBridgeCommand(this));

        OmniBridgeAPI.init(this);

        boolean autoInstall = getConfig().getBoolean("auto-install", false);
        boolean autoUpdate = getConfig().getBoolean("auto-update", false);

        if (autoInstall) {
            getLogger().info("Auto-installing missing plugins...");
            pluginManager.installAllAsync(null);
        }

        if (autoUpdate) {
            getLogger().info("Checking for plugin updates...");
            pluginManager.updateAllAsync(null);
        }

        startReminderIfNeeded();

        getLogger().info("OmniBridge v" + getDescription().getVersion() + " enabled on " + platform.name());
    }

    @Override
    public void onDisable() {
        stopReminder();
        OmniBridgeAPI.shutdown();
        getLogger().info("OmniBridge disabled.");
        instance = null;
    }

    private void startReminderIfNeeded() {
        if (isConfigured()) return;

        long intervalMinutes = getConfig().getLong("reminder.interval-minutes", 5);
        long intervalTicks = intervalMinutes * 20 * 60;

        reminderTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (isConfigured()) {
                stopReminder();
                return;
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isOp()) {
                    sendReminder(player);
                }
            }
        }, intervalTicks, intervalTicks);

        getLogger().info("Reminder active: will notify ops every " + intervalMinutes + " min until config is changed.");
    }

    public void stopReminder() {
        if (reminderTask != null && !reminderTask.isCancelled()) {
            reminderTask.cancel();
            reminderTask = null;
        }
    }

    private void sendReminder(Player player) {
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("  OmniBridge", NamedTextColor.GOLD, TextDecoration.BOLD)
                .append(Component.text(" — ", NamedTextColor.DARK_GRAY))
                .append(Component.text("Setup required", NamedTextColor.YELLOW)));
        player.sendMessage(Component.empty());

        Component setupButton = Component.text("  [ Click to setup plugins ]", NamedTextColor.GREEN, TextDecoration.BOLD)
                .hoverEvent(HoverEvent.showText(Component.text("Open interactive setup menu", NamedTextColor.GREEN)))
                .clickEvent(ClickEvent.runCommand("/ob setup"));
        player.sendMessage(setupButton);

        player.sendMessage(Component.empty());
    }

    private boolean isConfigured() {
        var section = getConfig().getConfigurationSection("managed-plugins");
        if (section == null) return false;
        for (String key : section.getKeys(false)) {
            if (section.getBoolean(key)) return true;
        }
        return false;
    }

    public static OmniBridge getInstance() {
        return instance;
    }

    public Platform getPlatform() {
        return platform;
    }

    public PluginManager getPluginManager() {
        return pluginManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ViaVersionIntegration getViaVersionIntegration() {
        return viaVersionIntegration;
    }

    public GeyserIntegration getGeyserIntegration() {
        return geyserIntegration;
    }
}
