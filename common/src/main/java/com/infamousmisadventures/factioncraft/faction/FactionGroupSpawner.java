package com.infamousmisadventures.factioncraft.faction;

import com.infamousmisadventures.factioncraft.entity.data.FactionEntityData;
import com.infamousmisadventures.factioncraft.entity.data.MobRaiderData;
import com.infamousmisadventures.factioncraft.entity.data.holder.IFactionEntityDataHolder;
import com.infamousmisadventures.factioncraft.entity.data.holder.IMobRaiderDataHolder;
import com.infamousmisadventures.factioncraft.faction.entity.FactionEntityRank;
import com.infamousmisadventures.factioncraft.faction.entity.FactionEntityType;
import com.infamousmisadventures.factioncraft.util.GeneralUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.biome.Biome;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.infamousmisadventures.factioncraft.util.GeneralUtils.getRandomEntry;

public class FactionGroupSpawner {

    private final ServerLevel level;
    private final BlockPos spawnBlockPos;
    private final int waveNumber;
    private final int targetStrength;
    private final Faction faction;
    private final float mobsFraction;

    private List<Mob> entities = new ArrayList<>();

    public FactionGroupSpawner(ServerLevel level, BlockPos spawnBlockPos, int waveNumber, int targetStrength, float mobsFraction, Faction faction) {
        this.level = level;
        this.spawnBlockPos = spawnBlockPos;
        this.waveNumber = waveNumber;
        this.targetStrength = targetStrength;
        this.mobsFraction = mobsFraction;
        this.faction = faction;
    }

    public List<Mob> getEntities() {
        return new ArrayList<>(entities);
    }

    public void spawnGroup() {
        int totalMobStrength = (int) Math.floor(targetStrength * mobsFraction);

        SelectedFactionEntityTypes selectedFactionEntityTypes = determineFactionEntityTypes(totalMobStrength, waveNumber, faction, spawnBlockPos);
        int selectedStrength = selectedFactionEntityTypes.selectedStrength;
        List<FactionEntityType> waveFactionEntities = selectedFactionEntityTypes.factionEntityTypeList;

        waveFactionEntities.forEach(factionEntityType -> {
            Entity entity = factionEntityType.createEntity(level, faction, spawnBlockPos, false, FactionEntityRank.SOLDIER, MobSpawnType.PATROL);
            if (entity instanceof Mob mobEntity) {
                //Add to Raid
                addToEntities(faction, mobEntity);
            }
        });
        
        FactionBoostHelper.applyBoosts(targetStrength - selectedStrength, entities, faction, this.level);

        entities.stream().toList().forEach(mobEntity -> {
            addToEntities(faction, mobEntity);
        });

        createCaptain();
    }

    private void addToEntities(Faction faction, Mob baseEntity) {
        baseEntity.getRootVehicle().getSelfAndPassengers().forEach(entity -> {
            if (entity instanceof Mob mob && !entities.contains(mob)) {
                FactionEntityData factionEntityCapability = ((IFactionEntityDataHolder) mob).getOrCreateFactionEntityData();
                if (factionEntityCapability != null && faction.equals(factionEntityCapability.getFaction())) {
                    entities.add(mob);
                }
            }
        });
    }

    private void createCaptain() {
        List<Mob> captainEntities = entities.stream().filter(mob -> ((IFactionEntityDataHolder) mob).getOrCreateFactionEntityData().getFactionEntityType().canBeBannerHolder()).toList();
        Mob randomItem = GeneralUtils.getRandomItem(captainEntities, level.getRandom());
        if (randomItem != null) {
            faction.makeBannerHolder(randomItem);
            MobRaiderData raiderCapability = ((IMobRaiderDataHolder) randomItem).getOrCreateMobRaiderData();
            raiderCapability.setWaveLeader(true);
            ((IFactionEntityDataHolder) randomItem).getOrCreateFactionEntityData().setFactionEntityRank(FactionEntityRank.CAPTAIN);
        }
    }

    private SelectedFactionEntityTypes determineFactionEntityTypes(int targetStrength, int waveNumber, Faction faction, BlockPos spawnBlockPos) {
        Map<FactionEntityType, Integer> weightMap = getWeightMap(waveNumber, faction, spawnBlockPos);
        SelectedFactionEntityTypes selectedMinimalFactionEntityTypes = selectMinimalMobs(targetStrength, weightMap);
        return determineRandomEntries(targetStrength, weightMap, selectedMinimalFactionEntityTypes);
    }

    private Map<FactionEntityType, Integer> getWeightMap(int waveNumber, Faction faction, BlockPos spawnBlockPos) {
        Holder<Biome> biome = this.level.getBiome(spawnBlockPos);
        EntityWeightMapProperties entityWeightMapProperties = new EntityWeightMapProperties().setWave(waveNumber).setBiome(biome.value()).setBlockPos(spawnBlockPos);
        return faction.getWeightMap(entityWeightMapProperties);
    }

    private SelectedFactionEntityTypes selectMinimalMobs(int targetStrength, Map<FactionEntityType, Integer> weightMap) {
        List<FactionEntityType> waveFactionEntities = new ArrayList<>();
        int selectedStrength = 0;
        for (Map.Entry<FactionEntityType, Integer> pair : weightMap.entrySet()) {
            FactionEntityType factionEntityType = pair.getKey();
            if (factionEntityType.getSpawnedRange().min() > 0) {
                int strength = factionEntityType.getStrength();
                int amount = Math.min((int) Math.ceil((targetStrength - selectedStrength) / strength), factionEntityType.getSpawnedRange().min());
                selectedStrength += strength * amount;
                for(int i = 0; i < amount; i++){
                    waveFactionEntities.add(factionEntityType);
                }
            }
            if (selectedStrength >= targetStrength) {
                break;
            }
        }
        return new SelectedFactionEntityTypes(waveFactionEntities, selectedStrength);
    }

    private SelectedFactionEntityTypes determineRandomEntries(int targetStrength, Map<FactionEntityType, Integer> weightMap, SelectedFactionEntityTypes selectedMinimalFactionEntityTypes) {
        List<FactionEntityType> waveFactionEntities = new ArrayList<>(selectedMinimalFactionEntityTypes.factionEntityTypeList);
        int selectedStrength = selectedMinimalFactionEntityTypes.selectedStrength;
        while (selectedStrength < targetStrength && weightMap.size() > 0) {
            FactionEntityType randomEntry = getRandomEntry(weightMap, level.random);
            int entryCount = waveFactionEntities.stream().filter(factionEntityType -> factionEntityType.equals(randomEntry)).toList().size();
            selectedStrength += randomEntry.getStrength();
            if (entryCount >= randomEntry.getMaxSpawnedInGroup(waveFactionEntities.size())) {
                weightMap.remove(randomEntry);
            }else{
                waveFactionEntities.add(randomEntry);
                selectedStrength += randomEntry.getStrength();
            }
        }
        return new SelectedFactionEntityTypes(waveFactionEntities, selectedStrength);
    }

    private record SelectedFactionEntityTypes(List<FactionEntityType> factionEntityTypeList, int selectedStrength){}
}