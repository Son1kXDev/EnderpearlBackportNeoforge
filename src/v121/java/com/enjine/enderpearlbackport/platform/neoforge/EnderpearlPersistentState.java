package com.enjine.enderpearlbackport.platform.neoforge;

import com.enjine.enderpearlbackport.common.data.EnderpearlRecord;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class EnderpearlPersistentState extends SavedData {

    private static final String DATA_NAME = "enderpearl_backport_saved_pearls";
    private final Map<UUID, List<EnderpearlRecord>> pearlsByPlayer = new HashMap<>();

    public static EnderpearlPersistentState get(ServerLevel overworld) {
        return overworld.getDataStorage().computeIfAbsent(
                new Factory<>(EnderpearlPersistentState::new, EnderpearlPersistentState::load),
                DATA_NAME
        );
    }

    public void savePearls(UUID playerId, List<EnderpearlRecord> records) {
        if (records == null || records.isEmpty()) pearlsByPlayer.remove(playerId);
        else pearlsByPlayer.put(playerId, new ArrayList<>(records));
        setDirty();
    }

    public List<EnderpearlRecord> popPearls(UUID playerId) {
        List<EnderpearlRecord> out = pearlsByPlayer.remove(playerId);
        if (out == null) return List.of();
        setDirty();
        return out;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        CompoundTag root = new CompoundTag();

        for (var entry : pearlsByPlayer.entrySet()) {
            UUID playerId = entry.getKey();
            List<EnderpearlRecord> list = entry.getValue();

            ListTag arr = new ListTag();
            for (EnderpearlRecord r : list) {
                CompoundTag t = new CompoundTag();
                t.putUUID("pearlId", r.pearlId());
                t.putString("dim", r.dimensionId());
                t.putDouble("x", r.x());
                t.putDouble("y", r.y());
                t.putDouble("z", r.z());
                t.putDouble("vx", r.vx());
                t.putDouble("vy", r.vy());
                t.putDouble("vz", r.vz());
                arr.add(t);
            }
            root.put(playerId.toString(), arr);
        }

        tag.put("players", root);
        return tag;
    }

    private static EnderpearlPersistentState load(CompoundTag tag, HolderLookup.Provider provider) {
        EnderpearlPersistentState state = new EnderpearlPersistentState();

        CompoundTag players = tag.getCompound("players");
        for (String key : players.getAllKeys()) {
            UUID playerId;
            try {
                playerId = UUID.fromString(key);
            } catch (Exception e) {
                continue;
            }

            Tag maybeList = players.get(key);
            if (!(maybeList instanceof ListTag listTag)) continue;

            List<EnderpearlRecord> list = new ArrayList<>();
            for (int i = 0; i < listTag.size(); i++) {
                if (!(listTag.get(i) instanceof CompoundTag t)) continue;

                list.add(new EnderpearlRecord(
                        t.getUUID("pearlId"),
                        t.getString("dim"),
                        t.getDouble("x"), t.getDouble("y"), t.getDouble("z"),
                        t.getDouble("vx"), t.getDouble("vy"), t.getDouble("vz")
                ));
            }

            if (!list.isEmpty()) state.pearlsByPlayer.put(playerId, list);
        }

        return state;
    }
}
