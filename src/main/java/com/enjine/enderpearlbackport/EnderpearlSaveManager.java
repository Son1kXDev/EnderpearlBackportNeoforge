package com.enjine.enderpearlbackport;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EnderpearlSaveManager {
    private static final Path SAVE_PATH = Paths.get("config", "enderpearlbackport_data.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<UUID, EnderpearlData> DATA = new HashMap<>();

    public static void savePearl(EnderpearlData data) {
        DATA.put(data.playerId, data);
        saveToFile();
    }

    public static EnderpearlData getPearl(UUID playerId) {
        return DATA.get(playerId);
    }

    public static void removePearl(UUID playerId) {
        DATA.remove(playerId);
        saveToFile();
    }

    public static void loadFromFile() {
        if (!Files.exists(SAVE_PATH)) return;

        try (Reader reader = Files.newBufferedReader(SAVE_PATH)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            DATA.clear();

            for (String key : root.keySet()) {
                UUID uuid = UUID.fromString(key);
                JsonObject obj = root.getAsJsonObject(key);
                String dim = obj.get("dimension").getAsString();

                Vec3 pos = new Vec3(
                        obj.get("x").getAsDouble(),
                        obj.get("y").getAsDouble(),
                        obj.get("z").getAsDouble()
                );

                Vec3 vel = new Vec3(
                        obj.get("vx").getAsDouble(),
                        obj.get("vy").getAsDouble(),
                        obj.get("vz").getAsDouble()
                );

                ResourceLocation dimLoc = ResourceLocation.parse(dim);
                ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, dimLoc);

                EnderpearlData data = new EnderpearlData(uuid, dimension, pos, vel);
                DATA.put(uuid, data);
            }

        } catch (IOException e) {
            e.fillInStackTrace();
        }
    }

    private static void saveToFile() {
        JsonObject root = new JsonObject();
        for (var entry : DATA.entrySet()) {
            UUID uuid = entry.getKey();
            EnderpearlData data = entry.getValue();

            JsonObject obj = new JsonObject();
            obj.addProperty("dimension", data.dimension.location().toString());
            obj.addProperty("x", data.position.x);
            obj.addProperty("y", data.position.y);
            obj.addProperty("z", data.position.z);
            obj.addProperty("vx", data.velocity.x);
            obj.addProperty("vy", data.velocity.y);
            obj.addProperty("vz", data.velocity.z);
            root.add(uuid.toString(), obj);
        }

        try (Writer writer = Files.newBufferedWriter(SAVE_PATH)) {
            GSON.toJson(root, writer);
        } catch (IOException e) {
            e.fillInStackTrace();
        }
    }
}
