package io.omnibrige.api.events;

import io.omnibrige.download.Repository;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class PluginUpdatedEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final String pluginName;
    private final String displayName;
    private final Repository.PluginType type;
    private final String oldVersion;
    private final String newVersion;

    public PluginUpdatedEvent(String pluginName, String displayName, Repository.PluginType type,
                              String oldVersion, String newVersion) {
        this.pluginName = pluginName;
        this.displayName = displayName;
        this.type = type;
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
    }

    public String getPluginName() { return pluginName; }
    public String getDisplayName() { return displayName; }
    public Repository.PluginType getType() { return type; }
    public String getOldVersion() { return oldVersion; }
    public String getNewVersion() { return newVersion; }

    @Override
    public HandlerList getHandlers() { return HANDLER_LIST; }
    public static HandlerList getHandlerList() { return HANDLER_LIST; }
}
