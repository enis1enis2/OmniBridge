package io.omnibrige.download;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class DownloadServiceTest {

    private final DownloadService service = new DownloadService(Logger.getLogger("test"));

    @TempDir
    Path tempDir;

    @Test
    void validateJarReturnsFalseForNonExistent() {
        assertFalse(service.validateJar(new File("nonexistent.jar")));
    }

    @Test
    void validateJarReturnsFalseForNull() {
        assertFalse(service.validateJar(null));
    }

    @Test
    void validateJarReturnsFalseForEmptyFile() throws IOException {
        File empty = tempDir.resolve("empty.jar").toFile();
        empty.createNewFile();
        assertFalse(service.validateJar(empty));
    }

    @Test
    void validateJarReturnsFalseForTooSmallFile() throws IOException {
        File small = tempDir.resolve("small.jar").toFile();
        try (FileOutputStream fos = new FileOutputStream(small)) {
            fos.write(new byte[]{0x50, 0x4B});
        }
        assertFalse(service.validateJar(small));
    }

    @Test
    void validateJarReturnsFalseForInvalidContent() throws IOException {
        File invalid = tempDir.resolve("invalid.jar").toFile();
        try (FileOutputStream fos = new FileOutputStream(invalid)) {
            fos.write(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00});
        }
        assertFalse(service.validateJar(invalid));
    }

    @Test
    void validateJarReturnsTrueForValidZipHeader() throws IOException {
        File valid = tempDir.resolve("valid.jar").toFile();
        try (FileOutputStream fos = new FileOutputStream(valid)) {
            fos.write(new byte[]{0x50, 0x4B, 0x03, 0x04, 0x00, 0x00});
        }
        assertTrue(service.validateJar(valid));
    }

    @Test
    void validateJarHandlesPartialZipMagic() throws IOException {
        File partial = tempDir.resolve("partial.jar").toFile();
        try (FileOutputStream fos = new FileOutputStream(partial)) {
            fos.write(new byte[]{0x50, 0x4B, 0x03, (byte) 0xFF, 0x00});
        }
        assertFalse(service.validateJar(partial));
    }

    @Test
    void isHttpsAcceptsOnlyHttpsUrls() {
        assertTrue(DownloadService.isHttps("https://example.com/plugin.jar"));
        assertTrue(DownloadService.isHttps("  https://example.com/plugin.jar  "));
        assertFalse(DownloadService.isHttps("http://example.com/plugin.jar"));
        assertFalse(DownloadService.isHttps("ftp://example.com/plugin.jar"));
        assertFalse(DownloadService.isHttps("file:///tmp/plugin.jar"));
        assertFalse(DownloadService.isHttps("https://"));
        assertFalse(DownloadService.isHttps(null));
        assertFalse(DownloadService.isHttps(""));
    }

    @Test
    void downloadSyncRejectsNonHttpsUrlWithoutNetwork() {
        File dest = tempDir.resolve("insecure.jar").toFile();
        assertFalse(service.downloadSync("http://example.com/plugin.jar", dest));
        assertFalse(dest.exists());
    }

    @Test
    void downloadSyncRejectsNullUrl() {
        File dest = tempDir.resolve("null-url.jar").toFile();
        assertFalse(service.downloadSync(null, dest));
        assertFalse(dest.exists());
    }

    @Test
    void downloadSyncRejectsNullDestination() {
        assertFalse(service.downloadSync("https://example.com/plugin.jar", null));
    }

    @Test
    void downloadSyncLeavesExistingFileIntactOnFailure() throws IOException {
        File dest = tempDir.resolve("existing.jar").toFile();
        try (FileOutputStream fos = new FileOutputStream(dest)) {
            fos.write(new byte[]{0x50, 0x4B, 0x03, 0x04, 0x01, 0x02});
        }
        long before = dest.length();

        boolean result = service.downloadSync("https://invalid.example.com/nonexistent", dest);

        assertFalse(result);
        assertTrue(dest.exists(), "existing JAR must not be deleted on a failed download");
        assertEquals(before, dest.length(), "existing JAR must not be truncated on a failed download");
    }

    @Test
    void downloadSyncReturnsFalseForInvalidUrl() {
        File dest = tempDir.resolve("fail.jar").toFile();
        boolean result = service.downloadSync("https://invalid.example.com/nonexistent", dest);
        assertFalse(result);
        assertFalse(dest.exists());
    }

    @Test
    void downloadAsyncReturnsFalseForInvalidUrl() {
        File dest = tempDir.resolve("async-fail.jar").toFile();
        boolean result = service.downloadAsync("https://invalid.example.com/nonexistent", dest).join();
        assertFalse(result);
    }

    @Test
    void fetchLastModifiedReturnsEmptyForNonHttpsUrl() {
        assertTrue(service.fetchLastModified("http://example.com/test.jar").isEmpty());
    }

    @Test
    void fetchLastModifiedReturnsEmptyForNullUrl() {
        assertTrue(service.fetchLastModified(null).isEmpty());
    }

    @Test
    void fetchLastModifiedReturnsEmptyForInvalidUrl() {
        assertTrue(service.fetchLastModified("https://invalid.example.com/nonexistent").isEmpty());
    }
}
