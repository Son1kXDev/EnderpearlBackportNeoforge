package com.enjine.enderpearlbackport;

import com.enjine.enderpearlbackport.common.api.Platform;
import com.enjine.enderpearlbackport.platform.neoforge.NeoForgePearlMechanics;
import com.enjine.enderpearlbackport.platform.neoforge.NeoForgeSaveAdapter;
import com.enjine.enderpearlbackport.platform.neoforge.bridge.NeoForgeVersionBridge;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod(EnderpearlBackport.MODID)
public final class EnderpearlBackport {
    public static final String MODID = "enderpearlbackport";

    private final NeoForgePearlMechanics mechanics = new NeoForgePearlMechanics();

    public EnderpearlBackport(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(mechanics);
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();

        NeoForgeVersionBridge.init(server);
        Platform.init(new NeoForgeSaveAdapter(server));
    }
}
