package com.enjine.enderpearlbackport.platform.neoforge.bridge;

import com.enjine.enderpearlbackport.common.data.EnderpearlRecord;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.UUID;

public final class TeleportController implements VersionedTeleportController {

    private final MinecraftServer server;

    public TeleportController(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void teleport(UUID playerId, EnderpearlRecord r) {
        ServerPlayer player = server.getPlayerList().getPlayer(playerId);
        if (player == null) return;

        ServerLevel target = level(r.dimensionId());
        if (target == null) return;

        player.teleportTo(target, r.x(), r.y(), r.z(), player.getYRot(), player.getXRot());
        player.setDeltaMovement(r.vx(), r.vy(), r.vz());
        player.hurtMarked = true;
    }

    private ServerLevel level(String dim) {
        ResourceLocation id = ResourceLocation.tryParse(dim);
        if (id == null) return null;
        ResourceKey<Level> key = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, id);
        return server.getLevel(key);
    }
}
