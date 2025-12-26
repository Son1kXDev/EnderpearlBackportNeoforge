package com.enjine.enderpearlbackport.platform.neoforge.bridge;

public interface VersionedChunkController {
    void forceLoad(String dim, int chunkX, int chunkZ);

    void release(String dim, int chunkX, int chunkZ);
}
