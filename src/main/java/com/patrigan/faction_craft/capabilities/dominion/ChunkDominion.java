package com.patrigan.faction_craft.capabilities.dominion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

import static com.patrigan.faction_craft.faction.Faction.GAIA;

public class ChunkDominion {

    public static final Codec<ChunkDominion> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT).fieldOf("faction_dominions").forGetter(ChunkDominion::getFactionDominions)
            ).apply(builder, ChunkDominion::new));

    private final Map<ResourceLocation, Integer> factionDominions;

    public ChunkDominion() {
        Map<ResourceLocation, Integer> factionDominions = new HashMap<>();
        factionDominions.put(GAIA.getName(), 100);
        this.factionDominions = factionDominions ;
    }

    public ChunkDominion(Map<ResourceLocation, Integer> factionDominions) {
        this.factionDominions = factionDominions;
    }

    public Map<ResourceLocation, Integer> getFactionDominions() {
        return factionDominions;
    }
}
