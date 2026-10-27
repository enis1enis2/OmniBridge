/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * OmniBridge - Universal cross-version and cross-platform connectivity
 * Copyright (c) 2026 OmniBridge Contributors
 */

package io.omnibrige;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import io.omnibrige.commands.OmniBridgeCommandVelocity;
import io.omnibrige.core.MessageManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Main plugin entry point for Velocity proxy servers.
 * Registers the command and initializes the message manager on proxy initialization.
 */
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

    /**
     * Constructs the Velocity plugin entry point.
     *
     * @param proxyServer the Velocity proxy server instance
     * @param logger the plugin logger
     * @param dataDirectory the plugin data directory injected by Velocity
     */
    @Inject
    public OmniBridgeVelocity(ProxyServer proxyServer, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    /**
     * Initializes the plugin when the proxy starts.
     *
     * @param event the proxy initialization event
     */
    @Subscribe
    public void onProxyInit(ProxyInitializeEvent event) {
        logger.info("OmniBridge v1.0.0 initializing on Velocity...");

        try {
            Files.createDirectories(dataDirectory);
        } catch (IOException e) {
            logger.warning("Could not create data directory " + dataDirectory + ": " + e.getMessage());
        }

        MessageManager.init(logger, "en_US");

        proxyServer.getCommandManager().register(
                proxyServer.getCommandManager().metaBuilder("omnibrige")
                        .aliases("ob", "omnib")
                        .build(),
                new OmniBridgeCommandVelocity(proxyServer)
        );

        logger.info("OmniBridge enabled on Velocity.");
    }
}
