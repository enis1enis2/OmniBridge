package io.omnibrige.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlatformDetectorTest {

    @Test
    void isBukkitBasedReturnsTrueForPaper() {
        assertTrue(PlatformDetector.isBukkitBased(PlatformDetector.Platform.PAPER));
    }

    @Test
    void isBukkitBasedReturnsTrueForSpigot() {
        assertTrue(PlatformDetector.isBukkitBased(PlatformDetector.Platform.SPIGOT));
    }

    @Test
    void isBukkitBasedReturnsFalseForVelocity() {
        assertFalse(PlatformDetector.isBukkitBased(PlatformDetector.Platform.VELOCITY));
    }

    @Test
    void isBukkitBasedReturnsFalseForFabric() {
        assertFalse(PlatformDetector.isBukkitBased(PlatformDetector.Platform.FABRIC));
    }

    @Test
    void isBukkitBasedReturnsFalseForUnknown() {
        assertFalse(PlatformDetector.isBukkitBased(PlatformDetector.Platform.UNKNOWN));
    }

    @Test
    void isVelocityReturnsTrueForVelocity() {
        assertTrue(PlatformDetector.isVelocity(PlatformDetector.Platform.VELOCITY));
    }

    @Test
    void isVelocityReturnsFalseForPaper() {
        assertFalse(PlatformDetector.isVelocity(PlatformDetector.Platform.PAPER));
    }

    @Test
    void platformEnumHasAllValues() {
        assertEquals(5, PlatformDetector.Platform.values().length);
        assertNotNull(PlatformDetector.Platform.PAPER);
        assertNotNull(PlatformDetector.Platform.SPIGOT);
        assertNotNull(PlatformDetector.Platform.VELOCITY);
        assertNotNull(PlatformDetector.Platform.FABRIC);
        assertNotNull(PlatformDetector.Platform.UNKNOWN);
    }

    @Test
    void detectReturnsUnknownWhenNoMarkerIsPresent() {
        assertEquals(PlatformDetector.Platform.UNKNOWN, PlatformDetector.detect(name -> false));
    }

    @Test
    void detectPrefersVelocityOverBukkitMarkers() {
        PlatformDetector.Platform result = PlatformDetector.detect(name ->
                name.equals("com.velocitypowered.api.proxy.ProxyServer")
                        || name.equals("org.bukkit.Bukkit"));
        assertEquals(PlatformDetector.Platform.VELOCITY, result);
    }

    @Test
    void detectRecognizesModernPaperMarker() {
        PlatformDetector.Platform result = PlatformDetector.detect(name ->
                name.equals("io.papermc.paper.plugin.lifecycle.LifecycleEventManager")
                        || name.equals("org.bukkit.Bukkit"));
        assertEquals(PlatformDetector.Platform.PAPER, result);
    }

    @Test
    void detectRecognizesLegacyPaperMarker() {
        PlatformDetector.Platform result = PlatformDetector.detect(name ->
                name.equals("com.destroystokyo.paper.PaperConfig")
                        || name.equals("org.bukkit.Bukkit"));
        assertEquals(PlatformDetector.Platform.PAPER, result);
    }

    @Test
    void detectRecognizesFabricBeforeBukkitFallback() {
        PlatformDetector.Platform result = PlatformDetector.detect(name ->
                name.equals("net.fabricmc.loader.api.FabricLoader")
                        || name.equals("org.bukkit.Bukkit"));
        assertEquals(PlatformDetector.Platform.FABRIC, result);
    }

    @Test
    void detectFallsBackToSpigotForBukkitOnlyServers() {
        PlatformDetector.Platform result = PlatformDetector.detect(name ->
                name.equals("org.bukkit.craftbukkit.CraftServer"));
        assertEquals(PlatformDetector.Platform.SPIGOT, result);
    }

    @Test
    void detectDoesNotThrowOnTheCurrentClassPath() {
        assertNotNull(PlatformDetector.detect());
    }
}
