package com.enjine.enderpearlbackport.platform.neoforge.bridge;

import com.enjine.enderpearlbackport.common.data.EnderpearlRecord;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;

public final class PearlHooks implements VersionedPearlHooks {

    @Override
    public void onPearlCollision(ServerPlayer player, ThrownEnderpearl pearl) {
        if (player.level() != pearl.level()) {
            NeoForgeVersionBridge.teleport.teleport(
                    player.getUUID(),
                    new EnderpearlRecord(
                            pearl.getUUID(),
                            pearl.level().dimension().location().toString(),
                            pearl.getX(), pearl.getY(), pearl.getZ(),
                            pearl.getDeltaMovement().x,
                            pearl.getDeltaMovement().y,
                            pearl.getDeltaMovement().z
                    )
            );
        }
    }
}
