package com.enjine.enderpearlbackport.platform.neoforge;

import com.enjine.enderpearlbackport.common.api.PlatformAdapter;
import com.enjine.enderpearlbackport.common.data.EnderpearlRecord;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;

public final class NeoForgeSaveAdapter implements PlatformAdapter {

    private final MinecraftServer server;

    public NeoForgeSaveAdapter(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void savePearls(UUID playerId, List<EnderpearlRecord> pearls) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) return;
        EnderpearlPersistentState.get(overworld).savePearls(playerId, pearls);
    }

    @Override
    public List<EnderpearlRecord> popPearls(UUID playerId) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) return List.of();
        return EnderpearlPersistentState.get(overworld).popPearls(playerId);
    }
}
