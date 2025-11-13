package com.enjine.enderpearlbackport;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.*;

public class EnderpearlChunkManager {

    private static final Map<ResourceKey<Level>, Map<ChunkPos, Integer>> CHUNK_LOAD_COUNTS = new HashMap<>();
    private static final Map<UUID, TrackedChunk> PEARL_CHUNKS = new HashMap<>();

    private record TrackedChunk(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
    }


    public static void updatePearlChunk(MinecraftServer server, ServerLevel level, ThrownEnderpearl pearl) {
        UUID id = pearl.getUUID();
        int chunkX = pearl.blockPosition().getX() >> 4;
        int chunkZ = pearl.blockPosition().getZ() >> 4;

        ResourceKey<Level> currentDim = level.dimension();
        TrackedChunk previous = PEARL_CHUNKS.get(id);

        if (previous != null
                && previous.dimension.equals(currentDim)
                && previous.chunkX == chunkX
                && previous.chunkZ == chunkZ) {
            return;
        }

        if (previous != null) {
            ServerLevel oldLevel = server.getLevel(previous.dimension);
            if (oldLevel != null) {
                decrementChunkLoad(oldLevel, previous.chunkX, previous.chunkZ);
            }
        }

        incrementChunkLoad(level, chunkX, chunkZ);
        PEARL_CHUNKS.put(id, new TrackedChunk(currentDim, chunkX, chunkZ));
    }


    public static void cleanupOrphanPearls(MinecraftServer server, Set<UUID> alivePearls) {
        Iterator<Map.Entry<UUID, TrackedChunk>> it = PEARL_CHUNKS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, TrackedChunk> entry = it.next();
            UUID pearlId = entry.getKey();

            if (!alivePearls.contains(pearlId)) {
                TrackedChunk tracked = entry.getValue();
                ServerLevel level = server.getLevel(tracked.dimension);
                if (level != null) {
                    decrementChunkLoad(level, tracked.chunkX, tracked.chunkZ);
                }
                it.remove();
            }
        }
    }

    private static void incrementChunkLoad(ServerLevel level, int chunkX, int chunkZ) {
        ResourceKey<Level> dim = level.dimension();
        Map<ChunkPos, Integer> dimMap = CHUNK_LOAD_COUNTS.computeIfAbsent(dim, d -> new HashMap<>());
        ChunkPos pos = new ChunkPos(chunkX, chunkZ);
        int count = dimMap.getOrDefault(pos, 0);

        if (count == 0) {
            level.setChunkForced(chunkX, chunkZ, true);
        }

        dimMap.put(pos, count + 1);
    }

    private static void decrementChunkLoad(ServerLevel level, int chunkX, int chunkZ) {
        ResourceKey<Level> dim = level.dimension();
        Map<ChunkPos, Integer> dimMap = CHUNK_LOAD_COUNTS.get(dim);
        if (dimMap == null) return;

        ChunkPos pos = new ChunkPos(chunkX, chunkZ);
        Integer count = dimMap.get(pos);
        if (count == null) return;

        if (count <= 1) {
            dimMap.remove(pos);
            level.setChunkForced(chunkX, chunkZ, false);
            System.out.println("[EnderPearlBackport] Chunk unforced by last pearl: " + pos);
        } else {
            dimMap.put(pos, count - 1);
        }
    }


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
        System.out.println("Chunk unloaded (portal): " + pos);
    }
}
