package io.omnibrige.config;

import io.omnibrige.core.ConfigManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ConfigManagerTest {

    @TempDir
    Path tempDir;

    @Test
    void writeConfigFileCreatesFile() {
        File target = tempDir.resolve("test.yml").toFile();
        ConfigManager.writeConfigFile(target, "key: value");
        assertTrue(target.exists());
    }

    @Test
    void writeConfigFileDoesNotOverwriteExisting() throws IOException {
        File target = tempDir.resolve("existing.yml").toFile();
        Files.writeString(target.toPath(), "original: content");
        ConfigManager.writeConfigFile(target, "new: content");
        assertEquals("original: content", Files.readString(target.toPath()));
    }

    @Test
    void writeConfigFileCreatesParentDirectories() {
        File target = tempDir.resolve("sub/deep/config.yml").toFile();
        ConfigManager.writeConfigFile(target, "key: value");
        assertTrue(target.exists());
    }

    @Test
    void viaVersionConfigGeneratesFile() {
        ViaVersionConfig generator = new ViaVersionConfig();
        File result = generator.generate(tempDir.toFile());
        assertTrue(result.exists());
        assertTrue(result.getPath().contains("ViaVersion"));
    }

    @Test
    void viaBackwardsConfigGeneratesFile() {
        ViaBackwardsConfig generator = new ViaBackwardsConfig();
        File result = generator.generate(tempDir.toFile());
        assertTrue(result.exists());
        assertTrue(result.getPath().contains("ViaBackwards"));
    }

    @Test
    void viaRewindConfigGeneratesFile() {
        ViaRewindConfig generator = new ViaRewindConfig();
        File result = generator.generate(tempDir.toFile());
        assertTrue(result.exists());
        assertTrue(result.getPath().contains("ViaRewind"));
    }

    @Test
    void viaRewindLegacySupportConfigGeneratesFile() {
        ViaRewindLegacySupportConfig generator = new ViaRewindLegacySupportConfig();
        File result = generator.generate(tempDir.toFile());
        assertTrue(result.exists());
        assertTrue(result.getPath().contains("ViaRewindLegacySupport"));
    }

    @Test
    void floodgateConfigGeneratesFile() {
        FloodgateConfig generator = new FloodgateConfig();
        File result = generator.generate(tempDir.toFile());
        assertTrue(result.exists());
        assertTrue(result.getPath().contains("floodgate"));
    }

    @Test
    void viaVersionConfigContainsExpectedKeys() throws IOException {
        ViaVersionConfig generator = new ViaVersionConfig();
        File result = generator.generate(tempDir.toFile());
        String content = Files.readString(result.toPath());
        assertTrue(content.contains("check-for-updates:"));
        assertTrue(content.contains("simulate-pt:"));
        assertTrue(content.contains("packet-limiter:"));
    }

    @Test
    void configFilesAreNotOverwritten() throws IOException {
        ViaVersionConfig generator = new ViaVersionConfig();
        File first = generator.generate(tempDir.toFile());
        Files.writeString(first.toPath(), "custom: config");
        File second = generator.generate(tempDir.toFile());
        assertEquals("custom: config", Files.readString(second.toPath()));
    }
}
