package com.infamousmisadventures.factioncraft.faction;

import com.infamousmisadventures.factioncraft.config.FactionCraftConfig;
import com.infamousmisadventures.factioncraft.dominion.AreaDominion;
import com.infamousmisadventures.factioncraft.dominion.AreaPos;
import com.infamousmisadventures.factioncraft.entity.data.FactionEntityData;
import com.infamousmisadventures.factioncraft.entity.data.holder.IFactionEntityDataHolder;
import com.infamousmisadventures.factioncraft.level.saveddata.Dominion;
import com.infamousmisadventures.factioncraft.mixins.EntityAccessor;
import com.infamousmisadventures.factioncraft.registry.FCFactions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.MobSpawnSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.infamousmisadventures.factioncraft.config.FactionCraftConfig.DOMINION_FACTION_SPAWN_TRESHOLD;
import static com.infamousmisadventures.factioncraft.config.FactionCraftConfig.DOMINION_SUPPRESS_GAIA_SPAWN_TRESHOLD;
import static com.infamousmisadventures.factioncraft.faction.Faction.GAIA;

public class FactionSpawnHandler {

    public static WeightedRandomList<MobSpawnSettings.SpawnerData> addDominionSpawns(ServerLevel serverLevel, WeightedRandomList<MobSpawnSettings.SpawnerData> spawnerDataList, MobCategory mobCategory, BlockPos pos) {
        if(mobCategory != MobCategory.MONSTER) return spawnerDataList;
        List<MobSpawnSettings.SpawnerData> newSpawnerDataList = new ArrayList<>();
        if (FactionCraftConfig.ENABLE_DOMINION.get()) {
            Dominion dominion = Dominion.getOrCreate(serverLevel);
            AreaDominion chunkDominion = dominion.getAreaDominion(serverLevel, new AreaPos(pos));
            if(!shouldSuppressGaia(chunkDominion)){
                spawnerDataList.unwrap().forEach(spawnerData -> {
                    newSpawnerDataList.add(spawnerData);
                });
            }
            getSpawningFactionsStream(chunkDominion).forEach(faction -> {
                int dominionAmount = chunkDominion.getFactionDominion(faction);
                faction.getDominionSpawners(serverLevel, pos, dominionAmount).forEach(spawnerData -> {
                    newSpawnerDataList.add(spawnerData);
                });
            });
        }
        return WeightedRandomList.create(newSpawnerDataList);
    }

    private static boolean shouldSuppressGaia(AreaDominion chunkDominion) {
        return chunkDominion.getFactionDominions().entrySet().stream().filter(entry -> !entry.getKey().equals(GAIA.getName())).filter(entry -> entry.getValue() > DOMINION_SUPPRESS_GAIA_SPAWN_TRESHOLD.get()).findFirst().isPresent();
    }

    private static Stream<Faction> getSpawningFactionsStream(AreaDominion areaDominion) {
        return areaDominion.getFactionDominions().entrySet().stream()
                .filter(entry -> entry.getValue() > DOMINION_FACTION_SPAWN_TRESHOLD.get())
                .map(entry -> FCFactions.getFaction(entry.getKey()));
    }

    public static void onEntityJoin(ServerLevel level, Entity entity) {
        if (level.isClientSide() || !((EntityAccessor) entity).isFirstTick() || !FactionCraftConfig.ENABLE_DOMINION.get()) return;
        if (entity instanceof net.minecraft.world.entity.Mob mob) {
            FactionEntityData factionEntity = ((IFactionEntityDataHolder) mob).getOrCreateFactionEntityData();
            if (factionEntity.getFaction() == null || factionEntity.getFaction() == GAIA) {
                Dominion dominion = Dominion.getOrCreate(level);
                AreaDominion areaDominion = dominion.getAreaDominion(level, new AreaPos(entity.blockPosition()));
                List<WeightedEntry.Wrapper<Consumer<Entity>>> factionEntityConverters = getSpawningFactionsStream(areaDominion)
                        .flatMap(faction -> getWeightedEntries(faction, level, entity.blockPosition(), areaDominion.getFactionDominion(faction)).stream())
                        .toList();
                WeightedRandom.getRandomItem(level.getRandom(), factionEntityConverters).ifPresent(wrapper -> {
                    wrapper.getData().accept(entity);
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
