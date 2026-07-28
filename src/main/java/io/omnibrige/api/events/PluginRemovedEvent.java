/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * OmniBridge - Universal cross-version and cross-platform connectivity
 * Copyright (c) 2026 OmniBridge Contributors
 */

package io.omnibrige.api.events;

import io.omnibrige.download.Repository;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when OmniBridge removes a managed plugin from the server.
 */
public final class PluginRemovedEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final String pluginName;
    private final String displayName;
    private final Repository.PluginType type;

    /**
     * Constructs a new PluginRemovedEvent.
     *
     * @param pluginName the internal plugin key
     * @param displayName the human-readable plugin name
     * @param type the plugin category
     */
    public PluginRemovedEvent(String pluginName, String displayName, Repository.PluginType type) {
        this.pluginName = pluginName;
        this.displayName = displayName;
        this.type = type;
    }

    /** Returns the internal plugin key. */
    public String getPluginName() { return pluginName; }
    /** Returns the human-readable display name. */
    public String getDisplayName() { return displayName; }
    /** Returns the plugin category type. */
    public Repository.PluginType getType() { return type; }

    @Override
    public HandlerList getHandlers() { return HANDLER_LIST; }
    public static HandlerList getHandlerList() { return HANDLER_LIST; }
}
