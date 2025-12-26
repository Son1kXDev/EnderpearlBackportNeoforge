package com.enjine.enderpearlbackport.platform.neoforge.bridge;

import net.minecraft.server.MinecraftServer;

public final class NeoForgeVersionBridge {

    public static VersionedChunkController chunk;
    public static VersionedTeleportController teleport;
    public static VersionedPearlHooks hooks;

    public static void init(MinecraftServer server) {
        chunk = new ChunkController(server);
        teleport = new TeleportController(server);
        hooks = new PearlHooks();
    }

    private NeoForgeVersionBridge() {
    }
}
