package io.omnibrige.api;

import io.omnibrige.download.Repository;

import java.util.List;

public record ManagedPlugin(
        String name,
        String displayName,
        Repository.PluginType type,
        boolean installed,
        boolean enabled,
        String version,
        List<String> dependencies
) {}
