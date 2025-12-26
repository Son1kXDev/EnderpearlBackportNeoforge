package com.enjine.enderpearlbackport.common.api;

import com.enjine.enderpearlbackport.common.data.EnderpearlRecord;

import java.util.UUID;

public interface TeleportAdapter {

    void teleport(UUID playerId, EnderpearlRecord record);

}
