package dev.younes.aimassist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AimAssistConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static AimAssistConfig instance = new AimAssistConfig();

    public boolean enabled = true;
    /** true = only aim while the attack button is held; false = always aim. */
    public boolean onlyWhileAttacking = true;
    public boolean targetMobs = true;
    public boolean targetPlayers = false;
    public boolean targetAnimals = false;
    /** Max distance to the target's hitbox, in blocks. */
    public int range = 6;
    /** Field of view (degrees) in which targets are picked. */
    public int fov = 120;
    /** Percent of the remaining angle turned per tick (100 = instant snap). */
    public int speed = 35;
    public boolean requireLineOfSight = true;

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("aimassist.json");
    }

    public static AimAssistConfig get() {
        return instance;
    }

    public static void load() {
        Path p = path();
        if (Files.exists(p)) {
            try {
                AimAssistConfig loaded = GSON.fromJson(Files.readString(p), AimAssistConfig.class);
                if (loaded != null) instance = loaded;
            } catch (Exception e) {
                System.err.println("[AimAssist] Could not read config, using defaults: " + e);
            }
        }
        instance.range = Math.max(1, Math.min(12, instance.range));
        instance.fov = Math.max(10, Math.min(360, instance.fov));
        instance.speed = Math.max(1, Math.min(100, instance.speed));
        save();
    }

    public static void save() {
        try {
            Files.writeString(path(), GSON.toJson(instance));
        } catch (IOException e) {
            System.err.println("[AimAssist] Could not save config: " + e);
        }
    }
}
