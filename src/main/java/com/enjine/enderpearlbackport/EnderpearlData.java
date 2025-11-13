package com.enjine.enderpearlbackport;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class EnderpearlData {
    public final UUID pearlId;
    public final UUID playerId;
    public final ResourceKey<Level> dimension;
    public final Vec3 position;
    public final Vec3 velocity;

    public EnderpearlData(UUID pearlId, UUID playerId, ResourceKey<Level> dimension, Vec3 position, Vec3 velocity) {
        this.pearlId = pearlId;
        this.playerId = playerId;
        this.dimension = dimension;
        this.position = position;
        this.velocity = velocity;
    }
}

