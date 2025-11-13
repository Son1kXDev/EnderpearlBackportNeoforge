package com.enjine.enderpearlbackport;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EnderpearlSaveManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path SAVE_PATH = Path.of("config/enderpearl_backport.json");


    public static List<EnderpearlData> loadAll(UUID playerId) {
        if (!Files.exists(SAVE_PATH)) return List.of();

        try (Reader reader = Files.newBufferedReader(SAVE_PATH)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (!root.has(playerId.toString())) return List.of();

            JsonObject pearlsObj = root.getAsJsonObject(playerId.toString());
            List<EnderpearlData> result = new ArrayList<>();

            for (Map.Entry<String, JsonElement> entry : pearlsObj.entrySet()) {
                JsonObject obj = entry.getValue().getAsJsonObject();

                UUID pearlUUID = UUID.fromString(entry.getKey());

                ResourceLocation id = ResourceLocation.tryParse(obj.get("dimension").getAsString());
                ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, id);

                Vec3 pos = new Vec3(obj.get("x").getAsDouble(), obj.get("y").getAsDouble(), obj.get("z").getAsDouble());
                Vec3 vel = new Vec3(obj.get("vx").getAsDouble(), obj.get("vy").getAsDouble(), obj.get("vz").getAsDouble());

                result.add(new EnderpearlData(pearlUUID, playerId, dimension, pos, vel));
            }

            return result;
        } catch (IOException e) {
            e.fillInStackTrace();
        }

        return List.of();
    }


    public static void saveAll(UUID playerId, List<EnderpearlData> pearls) {
        JsonObject root = new JsonObject();

        if (Files.exists(SAVE_PATH)) {
            try (Reader reader = Files.newBufferedReader(SAVE_PATH)) {
                root = GSON.fromJson(reader, JsonObject.class);
            } catch (Exception ignored) {
            }
        }

        JsonObject pearlsObj = new JsonObject();
        for (EnderpearlData data : pearls) {
            JsonObject obj = new JsonObject();
            obj.addProperty("dimension", data.dimension.location().toString());
            obj.addProperty("x", data.position.x);
            obj.addProperty("y", data.position.y);
            obj.addProperty("z", data.position.z);
            obj.addProperty("vx", data.velocity.x);
            obj.addProperty("vy", data.velocity.y);
            obj.addProperty("vz", data.velocity.z);

            pearlsObj.add(data.pearlId.toString(), obj);
        }

        root.add(playerId.toString(), pearlsObj);

        try (Writer writer = Files.newBufferedWriter(SAVE_PATH)) {
            GSON.toJson(root, writer);
        } catch (IOException e) {
            e.fillInStackTrace();
        }
    }
}
