package dev.younes.aimassist;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class AimAssistClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        AimAssistConfig.load();
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
        ClientCommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess) -> AimAssistCommand.register(dispatcher));
    }

    private void onTick(Minecraft mc) {
        AimAssistConfig cfg = AimAssistConfig.get();
        LocalPlayer player = mc.player;

        if (!cfg.enabled || player == null || mc.level == null
                || !player.isAlive() || player.isSpectator()
                || !mc.mouseHandler.isMouseGrabbed()) {
            return;
        }
        if (cfg.onlyWhileAttacking && !mc.options.keyAttack.isDown()) {
            return;
        }

        Vec3 eye = player.getEyePosition();
        Entity best = null;
        float bestYaw = 0, bestPitch = 0;
        double bestScore = Double.MAX_VALUE;

        AABB search = player.getBoundingBox().inflate(cfg.range + 1.0);
        List<Entity> candidates = mc.level.getEntities(player, search, e -> true);

        for (Entity e : candidates) {
            if (!isValidTarget(e, player, cfg)) continue;

            AABB box = e.getBoundingBox();

            // distance from eyes to the closest point of the hitbox
            double cx = Mth.clamp(eye.x, box.minX, box.maxX);
            double cy = Mth.clamp(eye.y, box.minY, box.maxY);
            double cz = Mth.clamp(eye.z, box.minZ, box.maxZ);
            double dist = eye.distanceTo(new Vec3(cx, cy, cz));
            if (dist > cfg.range) continue;

            if (cfg.requireLineOfSight && !player.hasLineOfSight(e)) continue;

            Vec3 aim = box.getCenter();
            float[] rot = rotationTo(eye, aim);
            float dYaw = Mth.wrapDegrees(rot[0] - player.getYRot());
            float dPitch = rot[1] - player.getXRot();

            double half = cfg.fov / 2.0;
            if (Math.abs(dYaw) > half || Math.abs(dPitch) > half) continue;

            double score = Math.hypot(dYaw, dPitch);
            if (score < bestScore) {
                bestScore = score;
                best = e;
                bestYaw = rot[0];
                bestPitch = rot[1];
            }
        }

        if (best == null) return;

        float dYaw = Mth.wrapDegrees(bestYaw - player.getYRot());
        float dPitch = bestPitch - player.getXRot();
        double f = cfg.speed / 100.0;

        float newYaw = player.getYRot() + (float) (dYaw * f);
        float newPitch = player.getXRot() + (float) (dPitch * f);
        if (Math.abs(dYaw) < 0.05f) newYaw = bestYaw;
        if (Math.abs(dPitch) < 0.05f) newPitch = bestPitch;

        player.setYRot(newYaw);
        player.setXRot(Mth.clamp(newPitch, -90.0f, 90.0f));
    }

    private static boolean isValidTarget(Entity e, LocalPlayer self, AimAssistConfig cfg) {
        if (e == self || !e.isAlive() || e.isSpectator()) return false;
        if (!(e instanceof LivingEntity)) return false;

        if (e instanceof Player) {
            return cfg.targetPlayers;
        }
        MobCategory cat = e.getType().getCategory();
        if (cat == MobCategory.MONSTER) return cfg.targetMobs;
        if (cat == MobCategory.CREATURE) return cfg.targetAnimals;
        return false;
    }

    /** Returns {yaw, pitch} in Minecraft's convention to look from 'from' toward 'to'. */
    private static float[] rotationTo(Vec3 from, Vec3 to) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, horizontal));
        return new float[]{yaw, pitch};
    }
}
