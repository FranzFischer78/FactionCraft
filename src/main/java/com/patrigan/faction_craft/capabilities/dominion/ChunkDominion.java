package com.patrigan.faction_craft.capabilities.dominion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.patrigan.faction_craft.config.FactionCraftConfig;
import com.patrigan.faction_craft.faction.Faction;
import com.patrigan.faction_craft.registry.Factions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.patrigan.faction_craft.faction.Faction.GAIA;
import static com.patrigan.faction_craft.faction.Faction.VILLAGE_NAME;

public class ChunkDominion {

    public static final Codec<ChunkDominion> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT).fieldOf("faction_dominions").forGetter(ChunkDominion::getFactionDominions),
                    Codec.LONG.fieldOf("last_calibrate_tick").forGetter(ChunkDominion::getLastCalibrateTick)
            ).apply(builder, ChunkDominion::new));

    private final Map<ResourceLocation, Integer> factionDominions;
    private long lastCalibrateTick = 0;

    public ChunkDominion(ChunkPos chunkPos) {
        Map<ResourceLocation, Integer> factionDominions = new HashMap<>();
        factionDominions.put(GAIA.getName(), 100);
        this.factionDominions = factionDominions ;
    }

    public ChunkDominion(Map<ResourceLocation, Integer> factionDominions, long lastCalibrateTick) {
        this.factionDominions = factionDominions;
        this.lastCalibrateTick = lastCalibrateTick;
    }

    public Map<ResourceLocation, Integer> getFactionDominions() {
        return factionDominions;
    }

    public long getLastCalibrateTick() {
        return lastCalibrateTick;
    }

    public void adjust(Level level, ChunkPos chunkPos, Faction faction, int adjustment) {
        factionDominions.merge(faction.getName(), adjustment, Integer::sum);
        update(level, chunkPos);
    }

    private void update(Level level, ChunkPos chunkPos) {
        if(level.getGameTime() - lastCalibrateTick > 24000) {
            lastCalibrateTick = level.getGameTime();
            propagate(level, chunkPos);
            normalize(level);
        }
    }

    private void propagate(Level level, ChunkPos chunkPos) {
        Map<ResourceLocation, Integer> factionsWithDominion = factionDominions.entrySet().stream().filter(entry -> entry.getValue() > FactionCraftConfig.PROPAGATE_FACTION_DOMINION_TRESHOLD.get()).filter(entry -> !entry.equals(GAIA.getName())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        for(Map.Entry<ResourceLocation, Integer> entry : factionsWithDominion.entrySet()) {
            for(ChunkDominion neighbourDominion : DominionHelper.getCapability(level).getNeighbourDominions(chunkPos)) {
                if(neighbourDominion.GetFactionDominion(entry.getKey()) < FactionCraftConfig.PROPAGATE_FACTION_DOMINION_TRESHOLD.get()) {
                    neighbourDominion.adjust(level, chunkPos, Factions.getFaction(entry.getKey()), 1);
                }
            }
        }
    }

    public int getFactionDominion(Faction faction) {
        return factionDominions.get(faction.getName());
    }

    public int GetFactionDominion(ResourceLocation key) {
        return factionDominions.get(key);
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
}
