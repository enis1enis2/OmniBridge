/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * OmniBridge - Universal cross-version and cross-platform connectivity
 * Copyright (c) 2026 OmniBridge Contributors
 */

package io.omnibrige;

import io.omnibrige.api.GeyserIntegration;
import io.omnibrige.api.OmniBridgeAPI;
import io.omnibrige.api.ViaVersionIntegration;
import io.omnibrige.commands.OmniBridgeCommand;
import io.omnibrige.core.ConfigManager;
import io.omnibrige.core.MessageManager;
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

/**
 * Main plugin entry point for Paper/Spigot servers.
 * Handles initialization, lifecycle management, and the ops reminder system.
 */
public final class OmniBridge extends JavaPlugin {

    private static OmniBridge instance;
    private Platform platform;
    private PluginManager pluginManager;
    private ConfigManager configManager;
    private ViaVersionIntegration viaVersionIntegration;
    private GeyserIntegration geyserIntegration;
    private BukkitTask reminderTask;

    /**
     * Called when the plugin is enabled. Initializes all managers, integrations, and scheduled tasks.
     */
    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        String locale = getConfig().getString("locale", "en_US");
        MessageManager.init(getLogger(), locale);

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

    /**
     * Called when the plugin is disabled. Stops reminders and shuts down the API.
     */
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

    /** Cancels the ops reminder task if it is running. */
    public void stopReminder() {
        if (reminderTask != null && !reminderTask.isCancelled()) {
            reminderTask.cancel();
            reminderTask = null;
        }
    }

    private void sendReminder(Player player) {
        MessageManager msg = MessageManager.getInstance();
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("  OmniBridge", NamedTextColor.GOLD, TextDecoration.BOLD)
                .append(Component.text(" — ", NamedTextColor.DARK_GRAY))
                .append(Component.text(msg.msg("reminder.title"), NamedTextColor.YELLOW)));
        player.sendMessage(Component.empty());

        Component setupButton = Component.text("  " + msg.msg("reminder.button"), NamedTextColor.GREEN, TextDecoration.BOLD)
                .hoverEvent(HoverEvent.showText(Component.text(msg.msg("reminder.button-hover"), NamedTextColor.GREEN)))
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

    /**
     * Returns the singleton plugin instance.
     *
     * @return the OmniBridge plugin instance, or null if disabled
     */
    public static OmniBridge getInstance() {
        return instance;
    }

    /**
     * Returns the detected server platform.
     *
     * @return the platform enum value
     */
    public Platform getPlatform() {
        return platform;
    }

    /**
     * Returns the plugin manager instance.
     *
     * @return the PluginManager
     */
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    /**
     * Returns the configuration manager instance.
     *
     * @return the ConfigManager
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Returns the ViaVersion integration instance.
     *
     * @return the ViaVersionIntegration
     */
    public ViaVersionIntegration getViaVersionIntegration() {
        return viaVersionIntegration;
    }

    /**
     * Returns the Geyser integration instance.
     *
     * @return the GeyserIntegration
     */
    public GeyserIntegration getGeyserIntegration() {
        return geyserIntegration;
    }
}
