package com.patrigan.faction_craft.faction.spawning;

import com.patrigan.faction_craft.faction.Faction;
import com.patrigan.faction_craft.faction.entity.FactionEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DominionSpawner {

    private final Map<SpawnerKey, List<FactionEntityType>> spawnerKeys = new HashMap<>();
    private Faction faction;

    public DominionSpawner(Faction faction) {
        this.faction = faction;
    }

    public List<MobSpawnSettings.SpawnerData>  GetSpawnerData(LevelAccessor level, BlockPos spawnBlockPos, int dominionAmount){
        Holder<Biome> biome = level.getBiome(spawnBlockPos);
        SpawnerKey spawnerKey = new SpawnerKey(level, spawnBlockPos, biome.get(), dominionAmount);
        return getFactionEntityTypes(spawnerKey).stream().map(FactionEntityType::toSpawnerData).toList();
    }

    public List<FactionEntityType> GetSpawnableFactionEntityTypes(LevelAccessor level, BlockPos spawnBlockPos, int dominionAmount){
        Holder<Biome> biome = level.getBiome(spawnBlockPos);
        SpawnerKey spawnerKey = new SpawnerKey(level, spawnBlockPos, biome.get(), dominionAmount);
        return getFactionEntityTypes(spawnerKey);
    }

    private List<FactionEntityType> getFactionEntityTypes(SpawnerKey spawnerKey) {
        if(spawnerKeys.containsKey(spawnerKey)){
            return spawnerKeys.get(spawnerKey);
        }
        if(spawnerKeys.size() > 20){
            spawnerKeys.clear();
        }
        return spawnerKeys.computeIfAbsent(spawnerKey, key -> faction.getEntityTypes().stream().filter(
                        factionEntityType -> factionEntityType.canSpawnForBiome(key.biome)
                                && factionEntityType.canSpawnForYPos(key.spawnBlockPos)
                                && factionEntityType.canSpawnForDominion(key.dominionAmount))
                .toList());
    }

    private record SpawnerKey(LevelAccessor level, BlockPos spawnBlockPos, Biome biome, int dominionAmount) {
    }
}
