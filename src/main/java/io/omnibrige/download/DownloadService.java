/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * OmniBridge - Universal cross-version and cross-platform connectivity
 * Copyright (c) 2026 OmniBridge Contributors
 */

package io.omnibrige.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * Handles downloading plugin JARs from remote repositories with retry logic and validation.
 * Provides both synchronous and asynchronous download methods.
 */
public final class DownloadService {

    private static final int MAX_RETRIES = 3;
    private static final int TIMEOUT_SECONDS = 60;
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
     * Checks whether an update is available by performing a HEAD request.
     *
     * @param url the download URL
     * @param currentVersion the currently installed version string
     * @return true if the server returned a successful response
     */
    public boolean checkUpdate(String url, String currentVersion) {
        if (url == null || currentVersion == null) return false;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", "OmniBridge/1.0.0")
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            String disposition = response.headers().firstValue("Content-Disposition").orElse("");
            String contentType = response.headers().firstValue("Content-Type").orElse("");
            return response.statusCode() >= 200 && response.statusCode() < 300;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Synchronously downloads a file with automatic retries.
     *
     * @param url the download URL
     * @param destination the target file to write
     * @return true if the download and validation succeeded
     */
    public boolean downloadSync(String url, File destination) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                logger.info("Downloading " + destination.getName() + " (attempt " + attempt + "/" + MAX_RETRIES + ")...");

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                        .header("User-Agent", "OmniBridge/1.0.0")
                        .GET()
                        .build();

                HttpResponse<Path> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofFile(destination.toPath()));

                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    if (validateJar(destination)) {
                        logger.info("Successfully downloaded " + destination.getName());
                        return true;
                    } else {
                        logger.warning("Downloaded file is not a valid JAR: " + destination.getName());
                        destination.delete();
                    }
                } else {
                    logger.warning("HTTP " + response.statusCode() + " downloading " + destination.getName());
                    destination.delete();
                }
            } catch (IOException | InterruptedException e) {
                logger.warning("Download failed for " + destination.getName() + ": " + e.getMessage());
                destination.delete();
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Validates that a file is a valid JAR by checking for the ZIP magic bytes.
     *
     * @param file the file to validate
     * @return true if the file exists and has a valid ZIP/JAR header
     */
    public boolean validateJar(File file) {
        if (!file.exists() || file.length() < 4) {
            return false;
        }
        try {
            byte[] header = new byte[4];
            try (var is = Files.newInputStream(file.toPath())) {
                is.read(header);
            }
            for (int i = 0; i < ZIP_MAGIC.length; i++) {
                if (header[i] != (ZIP_MAGIC[i] & 0xFF)) {
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
