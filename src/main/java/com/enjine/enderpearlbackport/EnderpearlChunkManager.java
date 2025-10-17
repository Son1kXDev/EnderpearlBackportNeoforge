package com.enjine.enderpearlbackport;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

public class EnderpearlChunkManager {

    public static void loadChunk(ServerLevel level, int chunkX, int chunkZ) {
        ChunkPos pos = new ChunkPos(chunkX, chunkZ);

        level.setChunkForced(chunkX, chunkZ, true);
        LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);

        if (chunk == null) {
            System.out.println("Failed to load chunk at " + pos);
        }
    }

    public static void unloadChunk(ServerLevel level, int chunkX, int chunkZ) {
        ChunkPos pos = new ChunkPos(chunkX, chunkZ);
        level.setChunkForced(chunkX, chunkZ, false);
        System.out.println("Chunk unloaded: " + pos);
    }
}
