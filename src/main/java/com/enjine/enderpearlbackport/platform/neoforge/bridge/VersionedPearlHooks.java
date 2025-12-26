package com.enjine.enderpearlbackport.platform.neoforge.bridge;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;

public interface VersionedPearlHooks {
    void onPearlCollision(ServerPlayer player, ThrownEnderpearl pearl);
}
