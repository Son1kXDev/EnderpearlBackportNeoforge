package com.enjine.enderpearlbackport.common.data;

import com.enjine.enderpearlbackport.common.api.Platform;

import java.util.List;
import java.util.UUID;

public final class EnderpearlData {
    private EnderpearlData() {
    }

    public static void savePearls(UUID playerId, List<EnderpearlRecord> pearls) {
        Platform.adapter().savePearls(playerId, pearls);
    }

    public static List<EnderpearlRecord> popPearls(UUID playerId) {
        return Platform.adapter().popPearls(playerId);
    }
}


