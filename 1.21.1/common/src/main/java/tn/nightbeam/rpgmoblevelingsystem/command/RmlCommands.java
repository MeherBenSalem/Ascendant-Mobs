package tn.nightbeam.rpgmoblevelingsystem.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import tn.nightbeam.rpgmoblevelingsystem.api.RmlApi;
import tn.nightbeam.rpgmoblevelingsystem.config.ModConfig;

public final class RmlCommands {
    private RmlCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("rml")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("inspect")
                        .then(Commands.argument("target", EntityArgument.entity())
                                .executes(ctx -> {
                                    Entity entity = EntityArgument.getEntity(ctx, "target");
                                    ctx.getSource().sendSuccess(() -> Component.literal(RmlApi.getDebugBreakdown(entity).toString()), false);
                                    return 1;
                                })))
                .then(Commands.literal("recalculate")
                        .then(Commands.argument("target", EntityArgument.entity())
                                .executes(ctx -> {
                                    Entity entity = EntityArgument.getEntity(ctx, "target");
                                    RmlApi.recalculate(entity);
                                    ctx.getSource().sendSuccess(() -> Component.literal("Recalculated level to " + RmlApi.getLevel(entity)), true);
                                    return 1;
                                })))
                .then(Commands.literal("config")
                        .executes(ctx -> {
                            ModConfig.ensureDefaults();
                            ctx.getSource().sendSuccess(() -> Component.literal("Config validated. scaleDistance="
                                    + ModConfig.scale().scaleDistance + " rounding=" + ModConfig.scale().roundingMode), false);
                            return 1;
                        }))
                .then(Commands.literal("distance")
                        .executes(ctx -> {
                            var pos = ctx.getSource().getPosition();
                            var spawn = ctx.getSource().getLevel().getSharedSpawnPos();
                            double dx = pos.x - spawn.getX();
                            double dz = pos.z - spawn.getZ();
                            double dist = Math.sqrt(dx * dx + dz * dz);
                            ctx.getSource().sendSuccess(() -> Component.literal("Distance from spawn: " + (int) dist), false);
                            return 1;
                        })));
    }
}
