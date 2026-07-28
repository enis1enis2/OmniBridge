/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * OmniBridge - Universal cross-version and cross-platform connectivity
 * Copyright (c) 2026 OmniBridge Contributors
 */

package io.omnibrige;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import io.omnibrige.commands.OmniBridgeCommandVelocity;
import io.omnibrige.core.MessageManager;
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
/**
 * Main plugin entry point for Velocity proxy servers.
 * Registers the command and initializes the message manager on proxy initialization.
 */
public class OmniBridgeVelocity {

    private final ProxyServer proxyServer;
    private final Logger logger;
    private final Path dataDirectory;

    /**
     * Constructs the Velocity plugin entry point.
     *
     * @param proxyServer the Velocity proxy server instance
     * @param logger the plugin logger
     */
    public OmniBridgeVelocity(ProxyServer proxyServer, Logger logger) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.dataDirectory = Path.of("plugins/OmniBridge");
    }

    @Subscribe
    public void onProxyInit(ProxyInitializeEvent event) {
        logger.info("OmniBridge v1.0.0 initializing on Velocity...");

        MessageManager.init(logger, "en_US");

        proxyServer.getCommandManager().register(
                proxyServer.getCommandManager().metaBuilder("omnibrige").build(),
                new OmniBridgeCommandVelocity(proxyServer)
        );

        logger.info("OmniBridge enabled on Velocity.");
    }
}
