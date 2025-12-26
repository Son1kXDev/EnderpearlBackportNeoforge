package com.enjine.enderpearlbackport.platform.neoforge.bridge;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class ChunkController implements VersionedChunkController {

    private final MinecraftServer server;

    public ChunkController(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void forceLoad(String dim, int chunkX, int chunkZ) {
        ServerLevel level = level(dim);
        if (level != null) level.setChunkForced(chunkX, chunkZ, true);
    }

    @Override
    public void release(String dim, int chunkX, int chunkZ) {
        ServerLevel level = level(dim);
        if (level != null) level.setChunkForced(chunkX, chunkZ, false);
    }

    private ServerLevel level(String dim) {
        ResourceLocation id = ResourceLocation.tryParse(dim);
        if (id == null) return null;
        ResourceKey<Level> key = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, id);
        return server.getLevel(key);
    }
}
