package io.omnibrige.core;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PluginPresetsTest {

    @Test
    void allPresetsHaveValidData() {
        for (var entry : PluginPresets.getAll().entrySet()) {
            String key = entry.getKey();
            PluginPresets.Preset preset = entry.getValue();
            assertNotNull(preset.displayName(), "displayName null for " + key);
            assertNotNull(preset.description(), "description null for " + key);
            assertFalse(preset.plugins().isEmpty(), "empty plugins for " + key);
        }
    }

    @Test
    void isKnownForExistingPresets() {
        assertTrue(PluginPresets.isKnown("bedrock"));
        assertTrue(PluginPresets.isKnown("full-version"));
        assertTrue(PluginPresets.isKnown("essentials"));
        assertTrue(PluginPresets.isKnown("max-compat"));
        assertTrue(PluginPresets.isKnown("server-essentials"));
        assertTrue(PluginPresets.isKnown("Bedrock")); // case insensitive
    }

    @Test
    void isKnownForUnknownPresets() {
        assertFalse(PluginPresets.isKnown("nonexistent"));
        assertFalse(PluginPresets.isKnown(""));
    }

    @Test
    void bedrockPresetContainsGeyserAndFloodgate() {
        List<String> plugins = PluginPresets.getPluginKeys("bedrock");
        assertTrue(plugins.contains("geyser"));
        assertTrue(plugins.contains("floodgate"));
        assertTrue(plugins.contains("hurricane"));
    }

    @Test
    void fullVersionPresetContainsViaPlugins() {
        List<String> plugins = PluginPresets.getPluginKeys("full-version");
        assertTrue(plugins.contains("viaversion"));
        assertTrue(plugins.contains("viabackwards"));
        assertTrue(plugins.contains("viarewind"));
        assertTrue(plugins.contains("viarewind-legacysupport"));
    }

    @Test
    void essentialsPresetContainsIntegrationPlugins() {
        List<String> plugins = PluginPresets.getPluginKeys("essentials");
        assertTrue(plugins.contains("authme"));
        assertTrue(plugins.contains("tab"));
        assertTrue(plugins.contains("protocolib"));
    }

    @Test
    void maxCompatContainsAllMajorPlugins() {
        List<String> plugins = PluginPresets.getPluginKeys("max-compat");
        assertTrue(plugins.size() >= 8, "max-compat should have at least 8 plugins");
        assertTrue(plugins.contains("viaversion"));
        assertTrue(plugins.contains("geyser"));
        assertTrue(plugins.contains("authme"));
    }

    @Test
    void getPluginKeysReturnsEmptyForUnknown() {
        assertTrue(PluginPresets.getPluginKeys("nonexistent").isEmpty());
    }

    @Test
    void getReturnsNullForUnknown() {
        assertNull(PluginPresets.get("nonexistent"));
    }
}
