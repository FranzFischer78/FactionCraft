package com.patrigan.faction_craft.capabilities.dominion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.patrigan.faction_craft.faction.Faction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

import static com.patrigan.faction_craft.faction.Faction.GAIA;

public class ChunkDominion {

    public static final Codec<ChunkDominion> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT).fieldOf("faction_dominions").forGetter(ChunkDominion::getFactionDominions),
                    Codec.LONG.fieldOf("last_calibrate_tick").forGetter(ChunkDominion::getLastCalibrateTick)
            ).apply(builder, ChunkDominion::new));

    private final Map<ResourceLocation, Integer> factionDominions;
    private long lastCalibrateTick = 0;

    public ChunkDominion() {
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

    public void adjust(Level level, Faction faction, int adjustment) {
        factionDominions.merge(faction.getName(), adjustment, Integer::sum);
        calibrate(level);
    }

    private void calibrate(Level level) {
        if(level.getGameTime() - lastCalibrateTick > 24000) {
            lastCalibrateTick = level.getGameTime();
            // If total FactionDominion is above 100, reduce it to 100, keeping values proportional and always rounding up the smaller ones.
            int totalDominion = factionDominions.values().stream().mapToInt(Integer::intValue).sum();
            if(totalDominion > 100) {
                int totalDominionDifference = totalDominion - 100;
                for(Map.Entry<ResourceLocation, Integer> entry : factionDominions.entrySet()) {
                    int newDominion = entry.getValue() - (int) Math.ceil((double) entry.getValue() / totalDominion * totalDominionDifference);
                    factionDominions.put(entry.getKey(), newDominion);
                }
            }else{
                factionDominions.put(GAIA.getName(), factionDominions.get(GAIA.getName()) + 100 - totalDominion);
            }
        }
    }
}
