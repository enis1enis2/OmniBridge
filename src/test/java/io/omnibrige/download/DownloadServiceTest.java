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
    void downloadSyncReturnsFalseForInvalidUrl() {
        File dest = tempDir.resolve("fail.jar").toFile();
        boolean result = service.downloadSync("https://invalid.example.com/nonexistent", dest);
        assertFalse(result);
    }

    @Test
    void downloadAsyncReturnsFalseForInvalidUrl() {
        File dest = tempDir.resolve("async-fail.jar").toFile();
        boolean result = service.downloadAsync("https://invalid.example.com/nonexistent", dest).join();
        assertFalse(result);
    }

    @Test
    void checkUpdateReturnsFalseForNullUrl() {
        assertFalse(service.checkUpdate(null, "1.0"));
    }

    @Test
    void checkUpdateReturnsFalseForNullVersion() {
        assertFalse(service.checkUpdate("https://example.com/test.jar", null));
    }

    @Test
    void checkUpdateReturnsFalseForInvalidUrl() {
        assertFalse(service.checkUpdate("https://invalid.example.com/nonexistent", "1.0"));
    }
}
