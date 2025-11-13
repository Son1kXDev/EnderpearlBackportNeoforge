package com.enjine.enderpearlbackport;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

import java.util.*;

@Mod(EnderpearlBackport.MODID)
public class EnderpearlBackport {
    public static final String MODID = "enderpearlbackport";
    private static final Logger LOGGER = LogUtils.getLogger();

    public EnderpearlBackport(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("[EnderPearlBackport] Common setup complete");
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server == null) return;

        Set<UUID> alivePearls = new HashSet<>();

        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getEntities().getAll()) {
                if (!(entity instanceof ThrownEnderpearl pearl)) continue;

                alivePearls.add(pearl.getUUID());
                EnderpearlChunkManager.updatePearlChunk(server, level, pearl);
                BlockPos pos = pearl.blockPosition();

                if (level.getBlockState(pos).is(Blocks.NETHER_PORTAL) || level.getBlockState(pos).is(Blocks.END_PORTAL)) {
                    if (pearl.getOwner() instanceof ServerPlayer player) {
                        ServerLevel target = getTargetDimension(level, pos, server);
                        if (target != null && target != player.serverLevel()) {
                            EnderpearlChunkManager.loadChunk(target, pos.getX() >> 4, pos.getZ() >> 4);
                            Vec3 exitPos = getPortalExitPosition(level, target, pearl);
                            Vec3 safePos = findSafeTeleportPosition(target, exitPos);

                            player.teleportTo(target, safePos.x, safePos.y, safePos.z, player.getYRot(), player.getXRot());
                            pearl.discard();

                            EnderpearlChunkManager.unloadChunk(target, pos.getX() >> 4, pos.getZ() >> 4);
                        }
                    }
                }
            }
        }

        EnderpearlChunkManager.cleanupOrphanPearls(server, alivePearls);
    }


    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        MinecraftServer server = player.getServer();
        if (server == null) return;

        List<EnderpearlData> pearls = new ArrayList<>();

        for (ServerLevel level : server.getAllLevels()) {
            for (Entity e : level.getEntities().getAll()) {
                if (e instanceof ThrownEnderpearl pearl && pearl.getOwner() == player) {

                    UUID pearlId = pearl.getUUID();
                    Vec3 pos = pearl.position();
                    Vec3 vel = pearl.getDeltaMovement();

                    pearls.add(new EnderpearlData(
                            pearlId,
                            player.getUUID(),
                            level.dimension(),
                            pos,
                            vel
                    ));

                    pearl.discard();
                }
            }
        }

        EnderpearlSaveManager.saveAll(player.getUUID(), pearls);
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        MinecraftServer server = player.getServer();
        if (server == null) return;

        List<EnderpearlData> pearls = EnderpearlSaveManager.loadAll(player.getUUID());
        if (pearls.isEmpty()) return;

        for (EnderpearlData data : pearls) {
            ServerLevel level = server.getLevel(data.dimension);
            if (level == null) continue;

            ThrownEnderpearl pearl = new ThrownEnderpearl(level, player);
            pearl.moveTo(data.position);
            pearl.setDeltaMovement(data.velocity);
            pearl.setUUID(data.pearlId);

            level.addFreshEntity(pearl);
        }
    }


    private ServerLevel getTargetDimension(ServerLevel from, BlockPos pos, MinecraftServer server) {
        if (from.getBlockState(pos).is(Blocks.NETHER_PORTAL)) {
            return switch (from.dimension().location().toString()) {
                case "minecraft:overworld" -> server.getLevel(Level.NETHER);
                case "minecraft:the_nether" -> server.getLevel(Level.OVERWORLD);
                default -> null;
            };
        }
        return null;
    }


    private Vec3 getPortalExitPosition(ServerLevel from, ServerLevel to, ThrownEnderpearl pearl) {
        Vec3 raw = new Vec3(pearl.getX(), pearl.getY(), pearl.getZ());
        raw = adjustCoordinatesForDimension(from, to, raw);

        if (to.dimension() == Level.END) {
            ServerPlayer owner = (ServerPlayer) pearl.getOwner();
            if (owner != null) {
                return owner.getRespawnPosition() != null
                        ? Vec3.atCenterOf(owner.getRespawnPosition())
                        : Vec3.atCenterOf(to.getSharedSpawnPos());
            }
        }

        int range = (to.dimension() == Level.NETHER) ? 16 : 128;
        BlockPos center = new BlockPos((int) raw.x, (int) raw.y, (int) raw.z);

        for (int dx = -range; dx <= range; dx++) {
            for (int dy = -50; dy <= 50; dy++) {
                for (int dz = -range; dz <= range; dz++) {
                    BlockPos check = center.offset(dx, dy, dz);
                    if (to.getBlockState(check).is(Blocks.NETHER_PORTAL) || to.getBlockState(check).is(Blocks.END_PORTAL)) {
                        Vec3 basePos = Vec3.atCenterOf(check).add(1.5, 0, 1.5);
                        return findSafeTeleportPosition(to, basePos);
                    }
                }
            }
        }

        return findSafeTeleportPosition(to, raw);
    }

    private Vec3 findSafeTeleportPosition(ServerLevel level, Vec3 start) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos((int) start.x, (int) start.y, (int) start.z);

        for (int i = 0; i < 10; i++) {
            if (isSafeBlock(level, pos)) return Vec3.atCenterOf(pos);
            pos.move(0, 1, 0);
        }

        pos.set((int) start.x, (int) start.y, (int) start.z);
        for (int i = 0; i < 20; i++) {
            if (isSafeBlock(level, pos)) return Vec3.atCenterOf(pos.above());
            pos.move(0, -1, 0);
        }

        LOGGER.warn("[EnderPearlBackport] Could not find safe spot near {}, returning raw", start);
        return start;
    }

    private boolean isSafeBlock(ServerLevel level, BlockPos pos) {
        boolean headAir = level.isEmptyBlock(pos.above());
        boolean aboveHeadAir = level.isEmptyBlock(pos.above(2));
        boolean hasGround = !level.isEmptyBlock(pos);
        boolean notLiquid = level.getBlockState(pos).getFluidState().isEmpty();
        return headAir && aboveHeadAir && hasGround && notLiquid;
    }

    private Vec3 adjustCoordinatesForDimension(ServerLevel from, ServerLevel to, Vec3 pos) {
        if (from.dimension() == Level.OVERWORLD && to.dimension() == Level.NETHER) {
            return new Vec3(pos.x / 8.0, pos.y, pos.z / 8.0);
        } else if (from.dimension() == Level.NETHER && to.dimension() == Level.OVERWORLD) {
            return new Vec3(pos.x * 8.0, pos.y, pos.z * 8.0);
        }
        return pos;
    }
}
