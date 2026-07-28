/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * OmniBridge - Universal cross-version and cross-platform connectivity
 * Copyright (c) 2026 OmniBridge Contributors
 */

package io.omnibrige.api;

import io.omnibrige.download.Repository;

import java.util.List;

/**
 * Represents the current state of a managed plugin known to OmniBridge.
 *
 * @param name the internal plugin key
 * @param displayName the human-readable plugin name
 * @param type the plugin category
 * @param installed whether the plugin JAR is present on the server
 * @param enabled whether the plugin is currently loaded and enabled
 * @param version the installed version string, or "N/A" if not installed
 * @param dependencies list of plugin keys this plugin depends on
 */
public record ManagedPlugin(
        String name,
        String displayName,
        Repository.PluginType type,
        boolean installed,
        boolean enabled,
        String version,
        List<String> dependencies
) {}
