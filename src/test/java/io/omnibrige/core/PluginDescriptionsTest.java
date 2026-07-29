package io.omnibrige.core;

import io.omnibrige.download.Repository;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PluginDescriptionsTest {

    @Test
    void allPluginsHaveDescriptions() {
        for (String key : Repository.getAllPlugins().keySet()) {
            String desc = PluginDescriptions.get(key);
            assertNotNull(desc, "description null for " + key);
            assertNotEquals("No description available.", desc, "default description for " + key);
        }
    }

    @Test
    void getReturnsDefaultForUnknown() {
        assertEquals("No description available.", PluginDescriptions.get("nonexistent"));
    }

    @Test
    void getAllReturnsNonEmptyMap() {
        Map<String, String> all = PluginDescriptions.getAll();
        assertFalse(all.isEmpty());
        assertTrue(all.containsKey("viaversion"));
        assertTrue(all.containsKey("geyser"));
        assertTrue(all.containsKey("vault"));
        assertTrue(all.containsKey("commandapi"));
    }

    @Test
    void descriptionsAreNonBlank() {
        for (var entry : PluginDescriptions.getAll().entrySet()) {
            assertFalse(entry.getValue().isBlank(), "blank description for " + entry.getKey());
        }
    }
}
