package com.enjine.enderpearlbackport.platform.neoforge;

import com.enjine.enderpearlbackport.common.data.EnderpearlData;
import com.enjine.enderpearlbackport.common.data.EnderpearlRecord;
import com.enjine.enderpearlbackport.platform.neoforge.bridge.NeoForgeVersionBridge;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;

public final class NeoForgePearlMechanics {

    public NeoForgePearlMechanics() {
    }

    private record PearlKey(String dim, UUID pearlUuid) {
    }

    private static final Map<PearlKey, ChunkPos> FORCED = new HashMap<>();
    private static final Map<String, Map<ChunkPos, Integer>> CHUNK_REFCOUNT = new HashMap<>();
    private static final Set<PearlKey> ALIVE = new HashSet<>();

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server == null) return;

        ALIVE.clear();

        for (ServerLevel world : server.getAllLevels()) {
            String dim = world.dimension().location().toString();

            for (Entity entity : world.getEntities().getAll()) {
                if (!(entity instanceof ThrownEnderpearl pearl)) continue;
                if (!pearl.isAlive()) continue;

                PearlKey key = new PearlKey(dim, pearl.getUUID());
                ChunkPos cp = new ChunkPos(pearl.blockPosition());

                ChunkPos prev = FORCED.get(key);
                if (prev == null || !prev.equals(cp)) {
                    if (prev != null) decrementChunk(dim, prev);
                    incrementChunk(dim, cp);
                    FORCED.put(key, cp);
                }

                ALIVE.add(key);
            }
        }

        Iterator<Map.Entry<PearlKey, ChunkPos>> it = FORCED.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<PearlKey, ChunkPos> e = it.next();
            if (!ALIVE.contains(e.getKey())) {
                decrementChunk(e.getKey().dim, e.getValue());
                it.remove();
            }
        }
    }

    private static void incrementChunk(String dim, ChunkPos pos) {
        Map<ChunkPos, Integer> map = CHUNK_REFCOUNT.computeIfAbsent(dim, k -> new HashMap<>());
        int count = map.getOrDefault(pos, 0);

        if (count == 0) NeoForgeVersionBridge.chunk.forceLoad(dim, pos.x, pos.z);
        map.put(pos, count + 1);
    }

    private static void decrementChunk(String dim, ChunkPos pos) {
        Map<ChunkPos, Integer> map = CHUNK_REFCOUNT.get(dim);
        if (map == null) return;

        int count = map.getOrDefault(pos, 0) - 1;
        if (count <= 0) {
            map.remove(pos);
            NeoForgeVersionBridge.chunk.release(dim, pos.x, pos.z);
        } else {
            map.put(pos, count);
        }
        if (map.isEmpty()) CHUNK_REFCOUNT.remove(dim);
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        MinecraftServer server = player.getServer();
        if (server == null) return;

        UUID playerId = player.getUUID();
        List<EnderpearlRecord> list = new ArrayList<>();

        for (ServerLevel world : server.getAllLevels()) {
            String dim = world.dimension().location().toString();

            for (Entity entity : world.getEntities().getAll()) {
                if (!(entity instanceof ThrownEnderpearl pearl)) continue;
                if (!pearl.isAlive()) continue;
                if (!(pearl.getOwner() instanceof ServerPlayer owner)) continue;
                if (!owner.getUUID().equals(playerId)) continue;

                list.add(new EnderpearlRecord(
                        pearl.getUUID(),
                        dim,
                        pearl.getX(), pearl.getY(), pearl.getZ(),
                        pearl.getDeltaMovement().x,
                        pearl.getDeltaMovement().y,
                        pearl.getDeltaMovement().z
                ));

                pearl.discard();
            }
        }

        EnderpearlData.savePearls(playerId, list);
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        MinecraftServer server = player.getServer();
        if (server == null) return;

        UUID playerId = player.getUUID();
        List<EnderpearlRecord> list = EnderpearlData.popPearls(playerId);
        if (list.isEmpty()) return;

        for (EnderpearlRecord r : list) {
            ServerLevel level = null;
            for (ServerLevel l : server.getAllLevels()) {
                if (l.dimension().location().toString().equals(r.dimensionId())) {
                    level = l;
                    break;
                }
            }
            if (level == null) continue;

            ThrownEnderpearl pearl = new ThrownEnderpearl(level, player);
            pearl.moveTo(r.x(), r.y(), r.z(), player.getYRot(), player.getXRot());
            pearl.setDeltaMovement(r.vx(), r.vy(), r.vz());
            level.addFreshEntity(pearl);
        }
    }

    public static void ensureCrossDimensionTeleport(ServerPlayer player, ThrownEnderpearl pearl) {
        if (!(player.level() instanceof ServerLevel playerWorld)) return;
        if (!(pearl.level() instanceof ServerLevel pearlWorld)) return;

        if (playerWorld != pearlWorld) {
            String dim = pearlWorld.dimension().location().toString();
            NeoForgeVersionBridge.teleport.teleport(
                    player.getUUID(),
                    new EnderpearlRecord(
                            pearl.getUUID(),
                            dim,
                            pearl.getX(), pearl.getY(), pearl.getZ(),
                            pearl.getDeltaMovement().x,
                            pearl.getDeltaMovement().y,
                            pearl.getDeltaMovement().z
                    )
            );
        }
    }
}
