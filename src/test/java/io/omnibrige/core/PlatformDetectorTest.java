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
}
