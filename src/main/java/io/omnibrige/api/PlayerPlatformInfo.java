/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * OmniBridge - Universal cross-version and cross-platform connectivity
 * Copyright (c) 2026 OmniBridge Contributors
 */

package io.omnibrige.api;

import java.util.UUID;

/**
 * Contains platform-specific information about a connected player.
 *
 * @param uuid the player's unique identifier
 * @param name the player's username
 * @param protocolVersion the ViaVersion protocol version string, or null if unavailable
 * @param bedrockPlayer whether the player is connecting via Bedrock Edition
 * @param bedrockPlatform the Bedrock device platform, or null if not a Bedrock player
 */
public record PlayerPlatformInfo(
        UUID uuid,
        String name,
        String protocolVersion,
        boolean bedrockPlayer,
        String bedrockPlatform
) {}
