package io.omnibrige.api.events;

import io.omnibrige.download.Repository;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class PluginInstalledEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final String pluginName;
    private final String displayName;
    private final Repository.PluginType type;

    public PluginInstalledEvent(String pluginName, String displayName, Repository.PluginType type) {
        this.pluginName = pluginName;
        this.displayName = displayName;
        this.type = type;
    }

    public String getPluginName() { return pluginName; }
    public String getDisplayName() { return displayName; }
    public Repository.PluginType getType() { return type; }

    @Override
    public HandlerList getHandlers() { return HANDLER_LIST; }
    public static HandlerList getHandlerList() { return HANDLER_LIST; }
}
