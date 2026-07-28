/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * OmniBridge - Universal cross-version and cross-platform connectivity
 * Copyright (c) 2026 OmniBridge Contributors
 */

package io.omnibrige.core;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public final class MessageManager {

    private static MessageManager instance;

    private final Logger logger;
    private Map<String, String> messages = new HashMap<>();
    private String locale;

    public MessageManager(Logger logger, String locale) {
        this.logger = logger;
        this.locale = locale;
        loadLocale(locale);
    }

    public static void init(Logger logger, String locale) {
        instance = new MessageManager(logger, locale);
    }

    public static MessageManager getInstance() {
        return instance;
    }

    public String msg(String key) {
        return messages.getOrDefault(key, key);
    }

    public String msg(String key, Object... args) {
        String template = messages.getOrDefault(key, key);
        try {
            return String.format(template, args);
        } catch (Exception e) {
            return template;
        }
    }

    public String getLocale() {
        return locale;
    }

    public void reload(String locale) {
        this.locale = locale;
        loadLocale(locale);
    }

    @SuppressWarnings("unchecked")
    private void loadLocale(String locale) {
        messages.clear();

        Map<String, String> fallback = loadFromJar("locale/en_US.yml");

        if ("en_US".equals(locale)) {
            messages.putAll(fallback);
            logger.info("Loaded locale: en_US (" + messages.size() + " messages)");
            return;
        }

        messages.putAll(fallback);

        Map<String, String> loaded = loadFromJar("locale/" + locale + ".yml");
        if (loaded.isEmpty()) {
            logger.warning("Locale '" + locale + "' not found, falling back to en_US");
        } else {
            messages.putAll(loaded);
            logger.info("Loaded locale: " + locale + " (" + messages.size() + " messages)");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> loadFromJar(String resourcePath) {
        Map<String, String> result = new HashMap<>();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) return result;
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(new InputStreamReader(is, StandardCharsets.UTF_8));
            if (data != null) {
                flattenMap("", data, result);
            }
        } catch (Exception e) {
            logger.warning("Failed to load locale resource: " + resourcePath + " - " + e.getMessage());
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private void flattenMap(String prefix, Map<String, Object> map, Map<String, String> result) {
        for (var entry : map.entrySet()) {
            String fullKey = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof String s) {
                result.put(fullKey, s);
            } else if (value instanceof Map<?, ?> nested) {
                flattenMap(fullKey, (Map<String, Object>) nested, result);
            }
        }
    }
}
