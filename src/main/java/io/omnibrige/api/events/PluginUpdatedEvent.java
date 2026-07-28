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
 * Fired when OmniBridge updates a managed plugin to a new version.
 */
public final class PluginUpdatedEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final String pluginName;
    private final String displayName;
    private final Repository.PluginType type;
    private final String oldVersion;
    private final String newVersion;

    /**
     * Constructs a new PluginUpdatedEvent.
     *
     * @param pluginName the internal plugin key
     * @param displayName the human-readable plugin name
     * @param type the plugin category
     * @param oldVersion the version before the update
     * @param newVersion the version after the update
     */
    public PluginUpdatedEvent(String pluginName, String displayName, Repository.PluginType type,
                              String oldVersion, String newVersion) {
        this.pluginName = pluginName;
        this.displayName = displayName;
        this.type = type;
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
    }

    /** Returns the internal plugin key. */
    public String getPluginName() { return pluginName; }
    /** Returns the human-readable display name. */
    public String getDisplayName() { return displayName; }
    /** Returns the plugin category type. */
    public Repository.PluginType getType() { return type; }
    /** Returns the version before the update. */
    public String getOldVersion() { return oldVersion; }
    /** Returns the version after the update. */
    public String getNewVersion() { return newVersion; }

    @Override
    public HandlerList getHandlers() { return HANDLER_LIST; }
    public static HandlerList getHandlerList() { return HANDLER_LIST; }
}
