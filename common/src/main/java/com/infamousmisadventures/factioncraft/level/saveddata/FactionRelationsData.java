package com.infamousmisadventures.factioncraft.level.saveddata;

import com.infamousmisadventures.factioncraft.FCConstants;
import com.infamousmisadventures.factioncraft.registry.FCFactions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.infamousmisadventures.factioncraft.faction.Faction;
import com.infamousmisadventures.factioncraft.faction.relations.FactionRelation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import com.infamousmisadventures.factioncraft.util.INBTSerializable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.infamousmisadventures.factioncraft.FCConstants.MOD_ID;

public class FactionRelationsData extends SavedData {

    public static final Codec<FactionRelationsData> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    FactionData.CODEC.listOf().fieldOf("factions").forGetter(data -> new ArrayList<>(data.getCurrentFactionData().values()))
            ).apply(builder, FactionRelationsData::new));

    public static FactionRelationsData getOrCreate(ServerLevel level)
    {
        return level.getServer().overworld().getDataStorage().computeIfAbsent((tag) -> load(tag, level), () -> create(level), MOD_ID + "-raid_manager");
    }

    public static FactionRelationsData load(CompoundTag tag, ServerLevel level)
    {
        FactionRelationsData factionRelationsData = create(level);
        factionRelationsData.load(tag);
        return factionRelationsData;
    }

    public static FactionRelationsData create(ServerLevel level)
    {
        return new FactionRelationsData();
    }

    private Map<ResourceLocation, FactionData> factions = new HashMap<>();

    public FactionRelationsData() {
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        CODEC.encodeStart(NbtOps.INSTANCE, this).resultOrPartial(FCConstants.LOGGER::error).ifPresent((p_216906_) -> {
            compoundTag.put("FactionDataList", p_216906_);
        });
        return compoundTag;
    }

    public void load(CompoundTag pCompound) {
        if (pCompound.contains("FactionDataList", 10)) {
            DataResult<FactionRelationsData> dataresult = FactionRelationsData.CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, pCompound.get("FactionDataList")));
            dataresult.resultOrPartial(FCConstants.LOGGER::error).ifPresent(factionRelationsData -> this.setFactions(factionRelationsData.getFactionData()));
        }
    }

    public FactionRelationsData(List<FactionData> factionDataList) {
        factionDataList.forEach(factionData -> this.factions.put(factionData.faction(), factionData));
    }

    public Map<ResourceLocation, FactionData> getFactionData() {
        return factions;
    }

    public Map<ResourceLocation, FactionData> getCurrentFactionData(){
        return FCFactions.FACTION_DATA.getData().values().stream()
                .collect(Collectors.toMap(Faction::getName, Faction::toFactionData));
    }

    public FactionRelationsData setFactions(Map<ResourceLocation, FactionData> factions) {
        this.factions = factions;
        return this;
    }

    public Map<ResourceLocation, FactionRelation> getOriginalRelations(Faction faction) {
        FactionData factionData = this.factions.get(faction.getName());
        if (factionData != null) {
            return factionData.factionRelations().getRelations(false);
        }
        return new HashMap<>();
    }
}
