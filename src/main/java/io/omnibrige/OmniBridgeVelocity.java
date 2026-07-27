package io.omnibrige;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import io.omnibrige.commands.OmniBridgeCommandVelocity;
import io.omnibrige.core.PlatformDetector;
import io.omnibrige.core.ConfigManager;
import io.omnibrige.core.PluginManager;

import java.nio.file.Path;
import java.util.logging.Logger;

@Plugin(
        id = "omnibrige",
        name = "OmniBridge",
        version = "1.0.0",
        description = "Universal cross-version and cross-platform connectivity solution",
        authors = {"OmniBridge"}
)
public class OmniBridgeVelocity {

    private final ProxyServer proxyServer;
    private final Logger logger;
    private final Path dataDirectory;

    public OmniBridgeVelocity(ProxyServer proxyServer, Logger logger) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.dataDirectory = Path.of("plugins/OmniBridge");
    }

    @Subscribe
    public void onProxyInit(ProxyInitializeEvent event) {
        logger.info("OmniBridge v1.0.0 initializing on Velocity...");

        proxyServer.getCommandManager().register(
                proxyServer.getCommandManager().metaBuilder("omnibrige").build(),
                new OmniBridgeCommandVelocity(proxyServer)
        );

        logger.info("OmniBridge enabled on Velocity.");
    }
}
