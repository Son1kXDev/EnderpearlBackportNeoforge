package com.enjine.enderpearlbackport.platform.neoforge.bridge;

import com.enjine.enderpearlbackport.common.data.EnderpearlRecord;

import java.util.UUID;

public interface VersionedTeleportController {
    void teleport(UUID playerId, EnderpearlRecord record);
}
