package com.patrigan.faction_craft.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.patrigan.faction_craft.capabilities.ModCapabilities;
import com.patrigan.faction_craft.capabilities.dominion.ChunkDominion;
import com.patrigan.faction_craft.capabilities.dominion.Dominion;
import com.patrigan.faction_craft.capabilities.raidmanager.RaidManager;
import com.patrigan.faction_craft.capabilities.raidmanager.RaidManagerHelper;
import com.patrigan.faction_craft.commands.arguments.FactionArgument;
import com.patrigan.faction_craft.faction.Faction;
import com.patrigan.faction_craft.raid.Raid;
import com.patrigan.faction_craft.raid.target.FactionBattleRaidTarget;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.Arrays;
import java.util.Optional;

public class DominionCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> dominionCommand
                = Commands.literal("dominion")
                .requires(commandSource -> commandSource.hasPermission(2))
                .then(Commands.literal("get").executes(sourceCommandContext ->
                    getDominion(sourceCommandContext.getSource())
                ).then(Commands.argument("location", BlockPosArgument.blockPos()).executes(sourceCommandContext ->
                    getDominion(sourceCommandContext.getSource(), BlockPosArgument.getLoadedBlockPos(sourceCommandContext, "location")))));

        dispatcher.register(dominionCommand);
    }

    private static int getDominion(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        return getDominion(source, player.blockPosition());
    }

    private static int getDominion(CommandSourceStack source, BlockPos blockPos) {
        // Get the ServerLevel
        ServerLevel level = source.getLevel();
        // Get the Dominion Capability of the level
        Optional<Dominion> dominionOptional = level.getCapability(ModCapabilities.DOMINION_CAPABILITY).resolve();
        if (dominionOptional.isPresent()) {
            Dominion dominion = dominionOptional.get();
            // Get the ChunkPos of the block
            ChunkPos chunkPos = new ChunkPos(blockPos);
            // Get the ChunkDominion of the block
            ChunkDominion chunkDominion = dominion.getChunkDominion(chunkPos);
            // If the ChunkDominion is not null
            if (chunkDominion != null) {
                // Send a message to the player with the Factions in the Chunk
                source.sendSuccess(Component.translatable("commands.dominion.factions", chunkDominion.getFactionDominions()), true);
                return 1;
            } else {
                // Send a message to the player that there is no Faction in the Chunk
                source.sendSuccess(Component.translatable("commands.dominion.no_faction"), true);
                return 0;
            }
        }
        return 0;
    }
}

