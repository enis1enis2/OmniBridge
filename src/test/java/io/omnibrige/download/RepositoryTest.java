package io.omnibrige.download;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryTest {

    @Test
    void allPluginsHaveValidInfo() {
        for (var entry : Repository.getAllPlugins().entrySet()) {
            String key = entry.getKey();
            Repository.PluginInfo info = entry.getValue();
            assertNotNull(info.displayName(), "displayName null for " + key);
            assertNotNull(info.type(), "type null for " + key);
            assertNotNull(info.url(), "url null for " + key);
            assertNotNull(info.bukkitName(), "bukkitName null for " + key);
            assertNotNull(info.jarName(), "jarName null for " + key);
            assertTrue(info.url().startsWith("http"), "url doesn't start with http for " + key);
            assertTrue(info.jarName().endsWith(".jar"), "jarName doesn't end with .jar for " + key);
        }
    }

    @Test
    void isKnownForExistingPlugins() {
        assertTrue(Repository.isKnown("viaversion"));
        assertTrue(Repository.isKnown("geyser"));
        assertTrue(Repository.isKnown("floodgate"));
        assertTrue(Repository.isKnown("authme"));
        assertTrue(Repository.isKnown("tuffxplus"));
        assertTrue(Repository.isKnown("luckperms"));
        assertTrue(Repository.isKnown("essentialsx"));
        assertTrue(Repository.isKnown("placeholderapi"));
        assertTrue(Repository.isKnown("worldguard"));
        assertTrue(Repository.isKnown("coreprotect"));
        assertTrue(Repository.isKnown("spark"));
        assertTrue(Repository.isKnown("discordsrv"));
        assertTrue(Repository.isKnown("chunky"));
        assertTrue(Repository.isKnown("bluemap"));
        assertTrue(Repository.isKnown("griefprevention"));
        assertTrue(Repository.isKnown("VIaversion")); // case insensitive
    }

    @Test
    void isKnownForUnknownPlugins() {
        assertFalse(Repository.isKnown("nonexistent"));
        assertFalse(Repository.isKnown(""));
    }

    @Test
    void getDisplayNameFallsBack() {
        assertEquals("nonexistent", Repository.getDisplayName("nonexistent"));
    }

    @Test
    void getUrlReturnsNullForUnknown() {
        assertNull(Repository.getUrl("nonexistent"));
    }

    @Test
    void getTypeReturnsNullForUnknown() {
        assertNull(Repository.getType("nonexistent"));
    }

    @Test
    void dependenciesAreCorrect() {
        assertEquals(List.of("floodgate"), Repository.getDependencies("geyser"));
        assertEquals(List.of("floodgate"), Repository.getDependencies("authme"));
        assertEquals(List.of("viaversion", "viabackwards"), Repository.getDependencies("tuffxplus"));
        assertEquals(List.of("placeholderapi"), Repository.getDependencies("discordsrv"));
        assertTrue(Repository.getDependencies("viaversion").isEmpty());
        assertTrue(Repository.getDependencies("nonexistent").isEmpty());
    }

    @Test
    void bukkitNamesAreCorrect() {
        assertEquals("ViaVersion", Repository.getBukkitName("viaversion"));
        assertEquals("Geyser-Spigot", Repository.getBukkitName("geyser"));
        assertEquals("floodgate", Repository.getBukkitName("floodgate"));
        assertEquals("TuffXPlus", Repository.getBukkitName("tuffxplus"));
        assertEquals("AuthMe", Repository.getBukkitName("authme"));
        assertEquals("TAB", Repository.getBukkitName("tab"));
        assertEquals("ProtocolLib", Repository.getBukkitName("protocolib"));
    }

    @Test
    void jarNamesAreCorrect() {
        assertEquals("Geyser-Spigot.jar", Repository.getJarName("geyser"));
        assertEquals("floodgate-spigot.jar", Repository.getJarName("floodgate"));
        assertEquals("ViaRewind-Legacy-Support.jar", Repository.getJarName("viarewind-legacysupport"));
        assertEquals("ViaBungee.jar", Repository.getJarName("viabungee"));
        assertEquals("Thunder.jar", Repository.getJarName("thunderbeta"));
        assertEquals("TuffXPlus.jar", Repository.getJarName("tuffxplus"));
        assertEquals("ViaVersion.jar", Repository.getJarName("viaversion"));
    }

    @Test
    void allPluginsHaveDescriptions() {
        for (String key : Repository.getAllPlugins().keySet()) {
            String bukkitName = Repository.getBukkitName(key);
            assertNotNull(bukkitName, "bukkitName null for " + key);
            assertFalse(bukkitName.isBlank(), "bukkitName blank for " + key);
        }
    }

    @Test
    void pluginTypesAreCorrect() {
        assertEquals(Repository.PluginType.VIAMCRAFT, Repository.getType("viaversion"));
        assertEquals(Repository.PluginType.VIAMCRAFT, Repository.getType("viabackwards"));
        assertEquals(Repository.PluginType.GEYSERMC, Repository.getType("geyser"));
        assertEquals(Repository.PluginType.GEYSERMC, Repository.getType("floodgate"));
        assertEquals(Repository.PluginType.INTEGRATION, Repository.getType("authme"));
        assertEquals(Repository.PluginType.INTEGRATION, Repository.getType("tab"));
        assertEquals(Repository.PluginType.INTEGRATION, Repository.getType("luckperms"));
        assertEquals(Repository.PluginType.INTEGRATION, Repository.getType("essentialsx"));
        assertEquals(Repository.PluginType.INTEGRATION, Repository.getType("placeholderapi"));
        assertEquals(Repository.PluginType.INTEGRATION, Repository.getType("worldguard"));
        assertEquals(Repository.PluginType.INTEGRATION, Repository.getType("coreprotect"));
        assertEquals(Repository.PluginType.COMMUNITY, Repository.getType("protocolib"));
        assertEquals(Repository.PluginType.COMMUNITY, Repository.getType("tuffxplus"));
        assertEquals(Repository.PluginType.COMMUNITY, Repository.getType("spark"));
        assertEquals(Repository.PluginType.COMMUNITY, Repository.getType("chunky"));
        assertEquals(Repository.PluginType.COMMUNITY, Repository.getType("bluemap"));
        assertEquals(Repository.PluginType.COMMUNITY, Repository.getType("griefprevention"));
        assertEquals(Repository.PluginType.INTEGRATION, Repository.getType("discordsrv"));
    }
}
