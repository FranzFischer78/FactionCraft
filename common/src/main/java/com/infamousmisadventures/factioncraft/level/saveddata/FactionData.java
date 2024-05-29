package com.infamousmisadventures.factioncraft.level.saveddata;

import com.infamousmisadventures.factioncraft.faction.relations.FactionRelations;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record FactionData(ResourceLocation faction, FactionRelations factionRelations) {

    public static final Codec<FactionData> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    ResourceLocation.CODEC.fieldOf("faction").forGetter(FactionData::faction),
                    FactionRelations.CODEC.fieldOf("relations").forGetter(FactionData::factionRelations)
            ).apply(builder, FactionData::new));

}
