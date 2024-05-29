package com.infamousmisadventures.factioncraft.commands;

import com.infamousmisadventures.factioncraft.commands.arguments.FactionArgument;
import com.infamousmisadventures.factioncraft.faction.Faction;
import com.infamousmisadventures.factioncraft.level.saveddata.RaidManager;
import com.infamousmisadventures.factioncraft.raid.Raid;
import com.infamousmisadventures.factioncraft.raid.target.FactionBattleRaidTarget;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Arrays;

public class FactionBattleCommand {
    private static final SimpleCommandExceptionType ERROR_START_FAILED = new SimpleCommandExceptionType(Component.translatable ("commands.battle.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> factionRaidCommand
                = Commands.literal("factionbattle")
                .requires(commandSource -> commandSource.hasPermission(2))
                .then(Commands.argument("faction1", FactionArgument.factions()).then(Commands.argument("faction2", FactionArgument.enemyFactions("faction1")).executes(sourceCommandContext ->
                    spawnBattle(sourceCommandContext.getSource(), FactionArgument.getFaction(sourceCommandContext, "faction1"), FactionArgument.getFaction(sourceCommandContext, "faction2"))
                ).then(Commands.argument("location", BlockPosArgument.blockPos()).executes(sourceCommandContext ->
                    spawnBattle(sourceCommandContext.getSource(), FactionArgument.getFaction(sourceCommandContext, "faction1"), FactionArgument.getFaction(sourceCommandContext, "faction2"), BlockPosArgument.getLoadedBlockPos(sourceCommandContext, "location"))))
                .then(Commands.argument("player", EntityArgument.player()).executes(sourceCommandContext ->
                        spawnBattle(sourceCommandContext.getSource(), FactionArgument.getFaction(sourceCommandContext, "faction1"), FactionArgument.getFaction(sourceCommandContext, "faction2"), EntityArgument.getPlayer(sourceCommandContext, "player"))))));

        dispatcher.register(factionRaidCommand);
    }

    private static int spawnBattle(CommandSourceStack source, Faction faction1, Faction faction2) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        return spawnBattle(source, faction1, faction2, player.blockPosition());
    }

    private static int spawnBattle(CommandSourceStack source, Faction faction1, Faction faction2, ServerPlayer player) throws CommandSyntaxException {
        return spawnBattle(source, faction1, faction2, player.blockPosition());
    }

    private static int spawnBattle(CommandSourceStack source, Faction faction1, Faction faction2, BlockPos blockPos) throws CommandSyntaxException {
        ServerLevel level = source.getLevel();
        FactionBattleRaidTarget raidTarget = new FactionBattleRaidTarget(blockPos, faction1, faction2, level);
        RaidManager raidManagerCapability = RaidManager.getOrCreate(level);
        Raid raid = raidManagerCapability.createRaid(Arrays.asList(faction1, faction2), raidTarget);
        if (raid == null) {
            throw ERROR_START_FAILED.create();
        } else {
            source.sendSuccess(() -> Component.translatable ("commands.battle.success", faction1.getName(), faction2.getName(), blockPos), true);
        }
        return 1;
    }

}

