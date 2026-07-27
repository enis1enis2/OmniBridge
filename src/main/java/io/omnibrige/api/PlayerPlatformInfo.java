package io.omnibrige.api;

import java.util.UUID;

public record PlayerPlatformInfo(
        UUID uuid,
        String name,
        String protocolVersion,
        boolean bedrockPlayer,
        String bedrockPlatform
) {}
