package com.enjine.enderpearlbackport.common.api;

public interface ChunkAdapter {

    void forceLoad(String dimensionId, int chunkX, int chunkZ);

    void release(String dimensionId, int chunkX, int chunkZ);

}
