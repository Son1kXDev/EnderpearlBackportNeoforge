package com.enjine.enderpearlbackport.common.api;

import com.enjine.enderpearlbackport.common.data.EnderpearlRecord;

import java.util.List;
import java.util.UUID;

public interface PlatformAdapter {

    void savePearls(UUID playerId, List<EnderpearlRecord> pearls);

    List<EnderpearlRecord> popPearls(UUID playerId);
}

