package io.omnibrige.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class MessageManagerTest {

    private static final Logger LOGGER = Logger.getLogger("test");

    @BeforeEach
    void setUp() {
        MessageManager.init(LOGGER, "en_US");
    }

    @Test
    void loadsEnglishByDefault() {
        assertEquals("en_US", MessageManager.getInstance().getLocale());
    }

    @Test
    void msgReturnsTranslatedString() {
        String result = MessageManager.getInstance().msg("no-permission");
        assertNotNull(result);
        assertFalse(result.isBlank());
        assertFalse(result.contains("no-permission"), "Should return translated text, not key");
    }

    @Test
    void msgReturnsKeyForUnknown() {
        String result = MessageManager.getInstance().msg("totally.unknown.key");
        assertEquals("totally.unknown.key", result);
    }

    @Test
    void msgFormatsArgs() {
        String result = MessageManager.getInstance().msg("command.preset.activated", "TestPreset");
        assertNotNull(result);
        assertTrue(result.contains("TestPreset"), "Should contain formatted arg");
    }

    @Test
    void reloadSwitchesLocale() {
        MessageManager.getInstance().reload("tr_TR");
        assertEquals("tr_TR", MessageManager.getInstance().getLocale());
        String turkish = MessageManager.getInstance().msg("no-permission");
        assertNotEquals("No permission.", turkish, "Turkish locale should differ from English");
        // Restore
        MessageManager.getInstance().reload("en_US");
    }

    @Test
    void reloadFallsBackForMissingLocale() {
        MessageManager.getInstance().reload("xx_YY");
        String result = MessageManager.getInstance().msg("no-permission");
        assertNotNull(result);
        assertFalse(result.isBlank());
        MessageManager.getInstance().reload("en_US");
    }

    @Test
    void msgHandlesMissingFormatArgs() {
        assertDoesNotThrow(() -> {
            String result = MessageManager.getInstance().msg("command.preset.activated");
            assertNotNull(result);
        });
    }
}
