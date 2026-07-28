/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * OmniBridge - Universal cross-version and cross-platform connectivity
 * Copyright (c) 2026 OmniBridge Contributors
 */

package io.omnibrige.api;

import java.util.UUID;

public record PlayerPlatformInfo(
        UUID uuid,
        String name,
        String protocolVersion,
        boolean bedrockPlayer,
        String bedrockPlatform
) {}
