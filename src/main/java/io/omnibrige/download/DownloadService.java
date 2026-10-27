/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * OmniBridge - Universal cross-version and cross-platform connectivity
 * Copyright (c) 2026 OmniBridge Contributors
 */

package io.omnibrige.download;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * Handles downloading plugin JARs from remote repositories with retry logic and validation.
 * Provides both synchronous and asynchronous download methods.
 *
 * <p>Downloads are written to a temporary {@code .part} file next to the destination,
 * validated, and only then moved into place. A failed or truncated download therefore
 * never corrupts an already installed JAR.</p>
 */
public final class DownloadService {

    /** Maximum accepted artifact size. Anything larger is rejected to bound disk usage. */
    private static final long MAX_DOWNLOAD_BYTES = 128L * 1024 * 1024;

    private static final int MAX_RETRIES = 3;
    private static final int TIMEOUT_SECONDS = 60;
    private static final String USER_AGENT = "OmniBridge/1.0.0";
    private static final byte[] ZIP_MAGIC = {0x50, 0x4B, 0x03, 0x04};

    private final HttpClient httpClient;
    private final Logger logger;

    /**
     * Constructs the download service.
     *
     * @param logger the plugin logger for status messages
     */
    public DownloadService(Logger logger) {
        this.logger = logger;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }

    /**
     * Asynchronously downloads a file from the given URL.
     *
     * @param url the download URL
     * @param destination the target file to write
     * @return a future that completes with true if the download succeeded
     */
    public CompletableFuture<Boolean> downloadAsync(String url, File destination) {
        return CompletableFuture.supplyAsync(() -> downloadSync(url, destination));
    }

    /**
     * Fetches the {@code Last-Modified} timestamp of a remote artifact.
     *
     * <p>This is used to detect updates for artifacts whose download URL points at a
     * rolling "latest" endpoint and therefore carries no version number.</p>
     *
     * @param url the artifact URL (must be HTTPS)
     * @return the remote modification time in epoch milliseconds, or empty when the
     *         server does not expose one (or the request failed)
     */
    public OptionalLong fetchLastModified(String url) {
        if (!isHttps(url)) return OptionalLong.empty();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", USER_AGENT)
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return OptionalLong.empty();
            }
            String header = response.headers().firstValue("Last-Modified").orElse(null);
            if (header == null) return OptionalLong.empty();
            ZonedDateTime timestamp = ZonedDateTime.parse(header, DateTimeFormatter.RFC_1123_DATE_TIME);
            return OptionalLong.of(timestamp.toInstant().toEpochMilli());
        } catch (DateTimeParseException e) {
            logger.fine("Unparseable Last-Modified for " + url + ": " + e.getMessage());
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            logger.fine("Could not fetch Last-Modified for " + url + ": " + e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.fine("Invalid URL for Last-Modified check: " + url);
        }
        return OptionalLong.empty();
    }

    /**
     * Synchronously downloads a file with automatic retries.
     *
     * <p>The artifact is streamed into a temporary file, size-capped, validated as a
     * ZIP/JAR, and atomically moved over the destination only after all checks passed.</p>
     *
     * @param url the download URL (must use HTTPS)
     * @param destination the target file to write
     * @return true if the download and validation succeeded
     */
    public boolean downloadSync(String url, File destination) {
        if (destination == null) {
            logger.warning("Download destination is null.");
            return false;
        }
        if (!isHttps(url)) {
            logger.warning("Refusing to download from a non-HTTPS URL: " + url);
            return false;
        }

        Path target = destination.toPath().toAbsolutePath();
        Path directory = target.getParent();
        if (directory == null) directory = Path.of(".");

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            Path temp = null;
            try {
                Files.createDirectories(directory);
                temp = Files.createTempFile(directory, destination.getName(), ".part");
                logger.info("Downloading " + destination.getName()
                        + " (attempt " + attempt + "/" + MAX_RETRIES + ")...");

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                        .header("User-Agent", USER_AGENT)
                        .GET()
                        .build();

                HttpResponse<InputStream> response =
                        httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

                try (InputStream body = response.body()) {
                    if (response.statusCode() < 200 || response.statusCode() >= 300) {
                        logger.warning("HTTP " + response.statusCode()
                                + " downloading " + destination.getName());
                        continue;
                    }

                    long declared = response.headers().firstValueAsLong("Content-Length").orElse(-1L);
                    if (declared > MAX_DOWNLOAD_BYTES) {
                        logger.warning("Refusing to download " + destination.getName()
                                + ": " + declared + " bytes exceeds the "
                                + MAX_DOWNLOAD_BYTES + " byte limit");
                        continue;
                    }

                    if (copyCapped(body, temp, MAX_DOWNLOAD_BYTES) < 0) {
                        logger.warning("Refusing to download " + destination.getName()
                                + ": response exceeds the " + MAX_DOWNLOAD_BYTES + " byte limit");
                        continue;
                    }
                }

                if (!validateJar(temp.toFile())) {
                    logger.warning("Downloaded file is not a valid JAR: " + destination.getName());
                    continue;
                }

                moveIntoPlace(temp, target);
                temp = null; // moved successfully, nothing left to delete
                logger.info("Successfully downloaded " + destination.getName());
                return true;
            } catch (IOException | InterruptedException e) {
                logger.warning("Download failed for " + destination.getName() + ": " + e.getMessage());
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            } catch (IllegalArgumentException e) {
                // Thrown by URI.create() for malformed URLs - retrying will not help.
                logger.warning("Invalid download URL for " + destination.getName() + ": " + url);
                return false;
            } finally {
                if (temp != null) {
                    try {
                        Files.deleteIfExists(temp);
                    } catch (IOException ignored) {
                        // Best effort cleanup.
                    }
                }
            }
        }
        return false;
    }

    /**
     * Copies at most {@code maxBytes} from the input stream into the target file.
     *
     * @return the number of bytes copied, or {@code -1} when the limit was exceeded
     */
    private static long copyCapped(InputStream in, Path target, long maxBytes) throws IOException {
        long total = 0;
        byte[] buffer = new byte[8192];
        try (OutputStream out = Files.newOutputStream(target,
                StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            int read;
            while ((read = in.read(buffer)) != -1) {
                total += read;
                if (total > maxBytes) return -1;
                out.write(buffer, 0, read);
            }
        }
        return total;
    }

    /**
     * Moves a validated temporary file over its final destination.
     */
    private static void moveIntoPlace(Path source, Path target) throws IOException {
        try {
            Files.move(source, target,
                    StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Validates that a file is a valid JAR by checking for the ZIP magic bytes.
     *
     * @param file the file to validate
     * @return true if the file exists and has a valid ZIP/JAR header
     */
    public boolean validateJar(File file) {
        if (file == null || !file.exists() || file.length() < ZIP_MAGIC.length) {
            return false;
        }
        try {
            byte[] header = new byte[4];
            try (var is = Files.newInputStream(file.toPath())) {
                int read = is.read(header);
                if (read < ZIP_MAGIC.length) return false;
            }
            for (int i = 0; i < ZIP_MAGIC.length; i++) {
                if (header[i] != ZIP_MAGIC[i]) {
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Checks whether a URL is a well-formed {@code https} URL.
     *
     * @param url the URL to check
     * @return true when the URL is non-null and uses the HTTPS scheme
     */
    public static boolean isHttps(String url) {
        if (url == null || url.isBlank()) return false;
        try {
            URI uri = URI.create(url.trim());
            return "https".equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
