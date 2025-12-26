package com.enjine.enderpearlbackport.mixin;

import com.enjine.enderpearlbackport.platform.neoforge.NeoForgePearlMechanics;
import com.enjine.enderpearlbackport.platform.neoforge.bridge.NeoForgeVersionBridge;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownEnderpearl.class)
public class EnderPearlEntityMixin {

    @Inject(method = "onHit", at = @At("HEAD"))
    private void epb$onHit(HitResult hitResult, CallbackInfo ci) {
        ThrownEnderpearl pearl = (ThrownEnderpearl) (Object) this;
        if (!(pearl.getOwner() instanceof ServerPlayer player)) return;

        NeoForgePearlMechanics.ensureCrossDimensionTeleport(player, pearl);
        NeoForgeVersionBridge.hooks.onPearlCollision(player, pearl);
    }
}
