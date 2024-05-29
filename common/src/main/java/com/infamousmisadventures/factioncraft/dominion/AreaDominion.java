package com.infamousmisadventures.factioncraft.dominion;

import com.infamousmisadventures.factioncraft.config.FactionCraftConfig;
import com.infamousmisadventures.factioncraft.faction.Faction;
import com.infamousmisadventures.factioncraft.level.saveddata.Dominion;
import com.infamousmisadventures.factioncraft.registry.FCFactions;
import com.infamousmisadventures.factioncraft.util.INBTSerializable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.infamousmisadventures.factioncraft.faction.Faction.GAIA;
import static com.infamousmisadventures.factioncraft.faction.Faction.VILLAGE_NAME;

public class AreaDominion implements INBTSerializable<CompoundTag> {

    public static final Codec<AreaDominion> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT).fieldOf("faction_dominions").forGetter(AreaDominion::getFactionDominions),
                    Codec.LONG.fieldOf("last_calibrate_tick").forGetter(AreaDominion::getLastCalibrateTick)
            ).apply(builder, AreaDominion::new));

    private final Map<ResourceLocation, Integer> factionDominions;
    private long lastCalibrateTick = 0;

    public AreaDominion(AreaPos areaPos) {
        Map<ResourceLocation, Integer> factionDominions = new HashMap<>();
        RandomSource randomSource = RandomSource.create();
        factionDominions.put(GAIA.getName(), 100);
        this.factionDominions = factionDominions ;
    }
    public AreaDominion(Level level, AreaPos areaPos) {
        Map<ResourceLocation, Integer> factionDominions = new HashMap<>();
        RandomSource randomSource = RandomSource.create();
        if(randomSource.nextFloat() <= 0.05F){
            Faction randomFaction = FCFactions.getRandomFaction(level, randomSource, faction1 -> faction1.getRaidConfig().isEnabled());
            factionDominions.put(GAIA.getName(), 1);
            factionDominions.put(randomFaction.getName(), 100);
        }else {
            factionDominions.put(GAIA.getName(), 100);
        }
        this.factionDominions = factionDominions ;
    }

    public AreaDominion(Map<ResourceLocation, Integer> factionDominions, long lastCalibrateTick) {
        this.factionDominions = factionDominions;
        this.lastCalibrateTick = lastCalibrateTick;
    }

    public Map<ResourceLocation, Integer> getFactionDominions() {
        return factionDominions;
    }

    public long getLastCalibrateTick() {
        return lastCalibrateTick;
    }

    public void adjust(ServerLevel level, AreaPos areaPos, Faction faction, int adjustment) {
        factionDominions.merge(faction.getName(), adjustment, Integer::sum);
        update(level, areaPos);
    }

    private void update(ServerLevel level, AreaPos areaPos) {
        if(level.getGameTime() - lastCalibrateTick > 24000) {
            lastCalibrateTick = level.getGameTime();
            propagate(level, areaPos);
            normalize(level);
        }
    }

    private void propagate(ServerLevel level, AreaPos areaPos) {
        Map<ResourceLocation, Integer> factionsWithDominion = factionDominions.entrySet().stream().filter(entry -> entry.getValue() > FactionCraftConfig.PROPAGATE_FACTION_DOMINION_TRESHOLD.get()).filter(entry -> !entry.equals(GAIA.getName())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        for(Map.Entry<ResourceLocation, Integer> entry : factionsWithDominion.entrySet()) {
            for(AreaDominion neighbourDominion : Dominion.getOrCreate(level).getNeighbourDominions(level, areaPos)) {
                if(neighbourDominion.getFactionDominion(entry.getKey()) < FactionCraftConfig.PROPAGATE_FACTION_DOMINION_TRESHOLD.get()) {
                    neighbourDominion.adjust(level, areaPos, FCFactions.getFaction(entry.getKey()), 1);
                }
            }
        }
    }

    public int getFactionDominion(Faction faction) {
        return getFactionDominion(faction.getName());
    }

    public int getFactionDominion(ResourceLocation key) {
        return factionDominions.getOrDefault(key, 0);
    }

    private void normalize(Level level) {
        int totalDominion = factionDominions.values().stream().mapToInt(Integer::intValue).sum();
        if(factionDominions.containsKey(VILLAGE_NAME)){
            // remove village dominion from gaia dominion
            factionDominions.compute(GAIA.getName(), (k, v) -> {
                if(v == null) return 0;
                return v - factionDominions.get(VILLAGE_NAME);
            });
        }
        if(totalDominion > 100) {
            int totalDominionDifference = totalDominion - 100;
            for(Map.Entry<ResourceLocation, Integer> entry : factionDominions.entrySet()) {
                int newDominion = entry.getValue() - (int) Math.ceil((double) entry.getValue() / totalDominion * totalDominionDifference);
                factionDominions.put(entry.getKey(), newDominion);
            }
        }else{
            factionDominions.put(GAIA.getName(), factionDominions.get(GAIA.getName()) + 100 - totalDominion);
        }
        if(factionDominions.containsKey(VILLAGE_NAME)){
            // remove village dominion from gaia dominion
            factionDominions.compute(GAIA.getName(), (k, v) -> {
                if(v == null) return 0;
                return v + factionDominions.get(VILLAGE_NAME);
            });
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        factionDominions.forEach((key, value) -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("faction", key.toString());
            entry.putInt("dominion", value);
            list.add(entry);
        });
        tag.put("faction_dominions", list);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        ListTag list = tag.getList("faction_dominions", 10);
        factionDominions.clear();
        list.forEach(inbt -> {
            CompoundTag entry = (CompoundTag) inbt;
            factionDominions.put(new ResourceLocation(entry.getString("faction")), entry.getInt("dominion"));
        });
    }
}
