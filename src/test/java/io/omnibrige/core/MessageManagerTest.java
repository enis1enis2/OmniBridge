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

    @Test
    void allLocalesLoadSuccessfully() {
        String[] locales = {
            "en_US", "tr_TR", "de_DE", "fr_FR", "es_ES", "pt_BR",
            "ru_RU", "zh_CN", "ja_JP", "ko_KR", "ar_SA",
            "hi_IN", "bn_BD", "it_IT", "vi_VN", "pl_PL", "nl_NL",
            "th_TH", "id_ID", "tl_PH", "cs_CZ", "el_GR", "sv_SE",
            "hu_HU", "ro_RO", "uk_UA", "he_IL", "ms_MY", "fi_FI",
            "da_DK", "no_NO",
            "ta_IN", "te_IN", "ml_IN", "pa_IN", "sw_KE", "am_ET",
            "ha_NG", "yo_NG", "ur_PK", "si_LK", "my_MM", "km_KH",
            "lo_LA", "mn_MN", "ka_GE", "hy_AM", "uz_UZ", "kk_KZ",
            "az_AZ", "eu_ES", "ca_ES", "gl_ES", "af_ZA", "et_EE",
            "lv_LV", "lt_LT", "sk_SK", "bg_BG", "hr_HR", "sr_RS",
            "sq_AL", "mk_MK", "fo_FO", "is_IS", "mt_MT", "cy_GB",
            "ga_IE", "zh_TW", "fa_IR", "mr_IN", "gu_IN"
        };

        String requiredKey = "no-permission";
        for (String locale : locales) {
            MessageManager.getInstance().reload(locale);
            String result = MessageManager.getInstance().msg(requiredKey);
            assertNotNull(result, "msg() returned null for locale " + locale);
            assertNotEquals(requiredKey, result,
                    "Locale " + locale + " returned key instead of translation for '" + requiredKey + "'");
        }
        MessageManager.getInstance().reload("en_US");
    }
}
