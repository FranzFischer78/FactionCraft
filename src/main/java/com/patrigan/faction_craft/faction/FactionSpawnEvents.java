package com.patrigan.faction_craft.faction;

import com.patrigan.faction_craft.FactionCraft;
import com.patrigan.faction_craft.capabilities.dominion.ChunkDominion;
import com.patrigan.faction_craft.capabilities.dominion.Dominion;
import com.patrigan.faction_craft.capabilities.dominion.DominionHelper;
import com.patrigan.faction_craft.capabilities.factionentity.FactionEntity;
import com.patrigan.faction_craft.capabilities.factionentity.FactionEntityHelper;
import com.patrigan.faction_craft.config.FactionCraftConfig;
import com.patrigan.faction_craft.registry.Factions;
import net.minecraft.core.BlockPos;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.function.Consumer;

import static com.patrigan.faction_craft.config.FactionCraftConfig.DOMINION_FACTION_SPAWN_TRESHOLD;
import static com.patrigan.faction_craft.config.FactionCraftConfig.DOMINION_SUPPRESS_GAIA_SPAWN_TRESHOLD;

@Mod.EventBusSubscriber(modid = FactionCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FactionSpawnEvents {

    @SubscribeEvent
    public static void addDominionSpawns(LevelEvent.PotentialSpawns event) {
        if (!FactionCraftConfig.ENABLE_DOMINION.get() && event.getLevel() instanceof Level level) {
            Dominion dominion = DominionHelper.getCapability(level);
            ChunkDominion chunkDominion = dominion.getChunkDominion(new ChunkPos(event.getPos()));
            List<Faction> spawningFactions = getSpawningFactions(chunkDominion);
            chunkDominion.getFactionDominions().values().stream().filter(dominionAmount -> dominionAmount > DOMINION_SUPPRESS_GAIA_SPAWN_TRESHOLD.get()).findFirst().ifPresent(dominionAmount -> {
                event.getSpawnerDataList().forEach(event::removeSpawnerData);
            });
            spawningFactions.forEach(faction -> {
                int dominionAmount = chunkDominion.getFactionDominion(faction);
                faction.getDominionSpawners(event.getLevel(), event.getPos(), dominionAmount).forEach(event::addSpawnerData);
            });
        }
    }

    private static List<Faction> getSpawningFactions(ChunkDominion chunkDominion) {
        return chunkDominion.getFactionDominions().entrySet().stream()
                .filter(entry -> entry.getValue() > DOMINION_FACTION_SPAWN_TRESHOLD.get())
                .map(entry -> Factions.getFaction(entry.getKey()))
                .toList();
    }

    @SubscribeEvent
    public static void onEntityJoinLevelEvent(EntityJoinLevelEvent event) {
        if (event.getEntity().level.isClientSide() || event.loadedFromDisk()) return;
        if (event.getEntity() instanceof net.minecraft.world.entity.Mob mob) {
            FactionEntity factionEntity = FactionEntityHelper.getFactionEntityCapability(mob);
            if (factionEntity.getFaction() == null || factionEntity.getFaction() == Faction.GAIA) {
                Dominion dominion = DominionHelper.getCapability(event.getLevel());
                ChunkDominion chunkDominion = dominion.getChunkDominion(new ChunkPos(event.getEntity().blockPosition()));
                List<Faction> spawningFactions = getSpawningFactions(chunkDominion);
                List<WeightedEntry.Wrapper<Consumer<Entity>>> factionEntityConverters = spawningFactions.stream()
                        .flatMap(faction -> getWeightedEntries(faction, event.getLevel(), event.getEntity().blockPosition(), chunkDominion.getFactionDominion(faction)).stream())
                        .toList();
                WeightedRandom.getRandomItem(event.getLevel().getRandom(), factionEntityConverters).ifPresent(wrapper -> {
                    wrapper.getData().accept(event.getEntity());
                });
            }
        }
    }

    private static List<WeightedEntry.Wrapper<Consumer<Entity>>> getWeightedEntries(Faction faction, Level level, BlockPos pos, int dominionAmount) {
        return faction.getSpawnableFactionEntityTypes(level, pos, dominionAmount).stream()
                .map(factionEntityType -> WeightedEntry.wrap(((Consumer<Entity>) (Entity entity) -> factionEntityType.convertEntity(faction, entity)), factionEntityType.getSpawnWeight()))
                .toList();
    }

}
