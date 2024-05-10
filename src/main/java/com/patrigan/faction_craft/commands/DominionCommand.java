package com.patrigan.faction_craft.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.patrigan.faction_craft.FactionCraft;
import com.patrigan.faction_craft.capabilities.dominion.AreaDominion;
import com.patrigan.faction_craft.capabilities.dominion.AreaPos;
import com.patrigan.faction_craft.capabilities.dominion.Dominion;
import com.patrigan.faction_craft.capabilities.dominion.DominionHelper;
import com.patrigan.faction_craft.commands.arguments.FactionArgument;
import com.patrigan.faction_craft.config.FactionCraftConfig;
import com.patrigan.faction_craft.faction.Faction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class DominionCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> dominionCommand
                = Commands.literal("dominion")
                .requires(commandSource -> commandSource.hasPermission(2))
                .then(Commands.literal("get").executes(sourceCommandContext ->
                        getDominion(sourceCommandContext.getSource())
                ).then(Commands.argument("location", BlockPosArgument.blockPos()).executes(sourceCommandContext ->
                        getDominion(sourceCommandContext.getSource(), BlockPosArgument.getLoadedBlockPos(sourceCommandContext, "location"))
                )))
                .then(Commands.literal("adjust").then(Commands.argument("faction", FactionArgument.factions()).executes(sourceCommandContext ->
                        adjustDominion(sourceCommandContext.getSource(), FactionArgument.getFaction(sourceCommandContext, "faction"), 1, new BlockPos(sourceCommandContext.getSource().getPosition()))
                ).then(Commands.argument("adjustment", IntegerArgumentType.integer()).executes(sourceCommandContext ->
                        adjustDominion(sourceCommandContext.getSource(), FactionArgument.getFaction(sourceCommandContext, "faction"), IntegerArgumentType.getInteger(sourceCommandContext, "adjustment"), new BlockPos(sourceCommandContext.getSource().getPosition()))
                ).then(Commands.argument("location", BlockPosArgument.blockPos()).executes(sourceCommandContext ->
                        adjustDominion(sourceCommandContext.getSource(), FactionArgument.getFaction(sourceCommandContext, "faction"), IntegerArgumentType.getInteger(sourceCommandContext, "adjustment"), BlockPosArgument.getLoadedBlockPos(sourceCommandContext, "location")))))));

        dispatcher.register(dominionCommand);
    }

    private static int getDominion(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        return getDominion(source, player.blockPosition());
    }

    private static int getDominion(CommandSourceStack source, BlockPos blockPos) {
        if(FactionCraftConfig.ENABLE_DOMINION.get() == false) {
            source.sendSuccess(Component.translatable("commands.dominion.disabled"), true);
            return 0;
        }
        ServerLevel level = source.getLevel();
        Dominion dominion = DominionHelper.getCapability(level);
        AreaPos areaPos = new AreaPos(blockPos);
        AreaDominion areaDominion = dominion.getAreaDominion(level, areaPos);
        if (areaDominion != null) {
            source.sendSuccess(Component.translatable("commands.dominion.factions", areaDominion.getFactionDominions()), true);
            return 1;
        } else {
            source.sendSuccess(Component.translatable("commands.dominion.no_faction"), true);
            return 0;
        }
    }

    private static int adjustDominion(CommandSourceStack source, Faction faction, int adjustment, BlockPos blockPos) {
        if(FactionCraftConfig.ENABLE_DOMINION.get() == false) {
            source.sendSuccess(Component.translatable("commands.dominion.disabled"), true);
            return 0;
        }
        ServerLevel level = source.getLevel();
        Dominion dominion = DominionHelper.getCapability(level);
        AreaPos areaPos = new AreaPos(blockPos);
        dominion.adjust(level, areaPos, faction, adjustment);
        source.sendSuccess(Component.translatable("commands.dominion.adjusted", faction.getName(), adjustment), true);
        return 1;
    }
}

