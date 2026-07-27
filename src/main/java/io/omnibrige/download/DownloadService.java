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

public final class DownloadService {

    private static final int MAX_RETRIES = 3;
    private static final int TIMEOUT_SECONDS = 60;
    private static final byte[] ZIP_MAGIC = {0x50, 0x4B, 0x03, 0x04};

    private final HttpClient httpClient;
    private final Logger logger;

    public DownloadService(Logger logger) {
        this.logger = logger;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }

    public CompletableFuture<Boolean> downloadAsync(String url, File destination) {
        return CompletableFuture.supplyAsync(() -> downloadSync(url, destination));
    }

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
