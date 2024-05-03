package com.patrigan.faction_craft.faction;

import com.patrigan.faction_craft.FactionCraft;
import com.patrigan.faction_craft.capabilities.dominion.ChunkDominion;
import com.patrigan.faction_craft.capabilities.dominion.Dominion;
import com.patrigan.faction_craft.capabilities.dominion.DominionHelper;
import com.patrigan.faction_craft.config.FactionCraftConfig;
import com.patrigan.faction_craft.registry.Factions;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = FactionCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FactionSpawnEvents {

    @SubscribeEvent
    public static void addDominionSpawns(LevelEvent.PotentialSpawns event) {
        if (!FactionCraftConfig.ENABLE_DOMINION.get() && event.getLevel() instanceof Level level) {
            Dominion dominion = DominionHelper.getCapability(level);
            ChunkDominion chunkDominion = dominion.getChunkDominion(new ChunkPos(event.getPos()));
            List<Faction> spawningFactions = chunkDominion.getFactionDominions().entrySet().stream()
                    .filter(entry -> entry.getValue() > 50)
                    .map(entry -> Factions.getFaction(entry.getKey()))
                    .collect(Collectors.toList());

            spawningFactions.forEach(faction -> {
                int dominionAmount = chunkDominion.getFactionDominion(faction);
                faction.getDominionSpawners(event.getLevel(), event.getPos(), dominionAmount).forEach(event::addSpawnerData);
            });
        }
    }
}
