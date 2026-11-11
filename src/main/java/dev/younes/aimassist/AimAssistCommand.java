package dev.younes.aimassist;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public class AimAssistCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> d) {
        d.register(literal("aimassist")
                .executes(ctx -> status(ctx.getSource()))
                .then(literal("status").executes(ctx -> status(ctx.getSource())))
                .then(literal("toggle").executes(ctx -> {
                    AimAssistConfig c = AimAssistConfig.get();
                    c.enabled = !c.enabled;
                    AimAssistConfig.save();
                    say(ctx.getSource(), "AimAssist: " + (c.enabled ? "ON" : "OFF"));
                    return 1;
                }))
                .then(literal("range").then(argument("blocks", IntegerArgumentType.integer(1, 12))
                        .executes(ctx -> {
                            AimAssistConfig c = AimAssistConfig.get();
                            c.range = IntegerArgumentType.getInteger(ctx, "blocks");
                            AimAssistConfig.save();
                            say(ctx.getSource(), "Range: " + c.range + " blocks");
                            return 1;
                        })))
                .then(literal("fov").then(argument("degrees", IntegerArgumentType.integer(10, 360))
                        .executes(ctx -> {
                            AimAssistConfig c = AimAssistConfig.get();
                            c.fov = IntegerArgumentType.getInteger(ctx, "degrees");
                            AimAssistConfig.save();
                            say(ctx.getSource(), "FOV: " + c.fov + " degrees");
                            return 1;
                        })))
                .then(literal("speed").then(argument("percent", IntegerArgumentType.integer(1, 100))
                        .executes(ctx -> {
                            AimAssistConfig c = AimAssistConfig.get();
                            c.speed = IntegerArgumentType.getInteger(ctx, "percent");
                            AimAssistConfig.save();
                            say(ctx.getSource(), "Speed: " + c.speed + "% per tick (100 = instant)");
                            return 1;
                        })))
                .then(literal("hold").then(argument("value", BoolArgumentType.bool())
                        .executes(ctx -> {
                            AimAssistConfig c = AimAssistConfig.get();
                            c.onlyWhileAttacking = BoolArgumentType.getBool(ctx, "value");
                            AimAssistConfig.save();
                            say(ctx.getSource(), c.onlyWhileAttacking
                                    ? "Aims only while holding attack" : "Aims all the time");
                            return 1;
                        })))
                .then(literal("mobs").then(argument("value", BoolArgumentType.bool())
                        .executes(ctx -> {
                            AimAssistConfig c = AimAssistConfig.get();
                            c.targetMobs = BoolArgumentType.getBool(ctx, "value");
                            AimAssistConfig.save();
                            say(ctx.getSource(), "Target hostile mobs: " + c.targetMobs);
                            return 1;
                        })))
                .then(literal("players").then(argument("value", BoolArgumentType.bool())
                        .executes(ctx -> {
                            AimAssistConfig c = AimAssistConfig.get();
                            c.targetPlayers = BoolArgumentType.getBool(ctx, "value");
                            AimAssistConfig.save();
                            say(ctx.getSource(), "Target players: " + c.targetPlayers);
                            return 1;
                        })))
                .then(literal("animals").then(argument("value", BoolArgumentType.bool())
                        .executes(ctx -> {
                            AimAssistConfig c = AimAssistConfig.get();
                            c.targetAnimals = BoolArgumentType.getBool(ctx, "value");
                            AimAssistConfig.save();
                            say(ctx.getSource(), "Target animals: " + c.targetAnimals);
                            return 1;
                        })))
                .then(literal("los").then(argument("value", BoolArgumentType.bool())
                        .executes(ctx -> {
                            AimAssistConfig c = AimAssistConfig.get();
                            c.requireLineOfSight = BoolArgumentType.getBool(ctx, "value");
                            AimAssistConfig.save();
                            say(ctx.getSource(), "Require line of sight: " + c.requireLineOfSight);
                            return 1;
                        }))));
    }

    private static void say(FabricClientCommandSource src, String msg) {
        src.sendFeedback(Component.literal(msg));
    }

    private static int status(FabricClientCommandSource src) {
        AimAssistConfig c = AimAssistConfig.get();
        say(src, "AimAssist " + (c.enabled ? "ON" : "OFF")
                + " | range " + c.range + " | fov " + c.fov + " | speed " + c.speed + "%"
                + " | " + (c.onlyWhileAttacking ? "hold attack" : "always"));
        say(src, "Targets: mobs=" + c.targetMobs + " players=" + c.targetPlayers
                + " animals=" + c.targetAnimals + " | line of sight=" + c.requireLineOfSight);
        return 1;
    }
}
