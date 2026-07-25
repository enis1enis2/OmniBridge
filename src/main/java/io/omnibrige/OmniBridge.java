package io.omnibrige;

import io.omnibrige.api.GeyserIntegration;
import io.omnibrige.api.ViaVersionIntegration;
import io.omnibrige.commands.OmniBridgeCommand;
import io.omnibrige.core.ConfigManager;
import io.omnibrige.core.PlatformDetector;
import io.omnibrige.core.PlatformDetector.Platform;
import io.omnibrige.core.PluginManager;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class OmniBridge extends JavaPlugin {

    private static OmniBridge instance;
    private Platform platform;
    private PluginManager pluginManager;
    private ConfigManager configManager;
    private ViaVersionIntegration viaVersionIntegration;
    private GeyserIntegration geyserIntegration;

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

        boolean autoInstall = getConfig().getBoolean("auto-install", true);
        boolean autoUpdate = getConfig().getBoolean("auto-update", false);

        if (autoInstall) {
            getLogger().info("Auto-installing missing plugins...");
            pluginManager.installAll();
        }

        if (autoUpdate) {
            getLogger().info("Checking for plugin updates...");
            pluginManager.updateAll();
        }

        getLogger().info("OmniBridge v" + getDescription().getVersion() + " enabled on " + platform.name());
    }

    @Override
    public void onDisable() {
        getLogger().info("OmniBridge disabled.");
        instance = null;
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
