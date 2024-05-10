package com.patrigan.faction_craft.capabilities.dominion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.patrigan.faction_craft.FactionCraft;
import com.patrigan.faction_craft.capabilities.playerfactions.PlayerFactions;
import com.patrigan.faction_craft.config.FactionCraftConfig;
import com.patrigan.faction_craft.data.CodecHelper;
import com.patrigan.faction_craft.faction.Faction;
import com.patrigan.faction_craft.faction.relations.FactionRelation;
import com.patrigan.faction_craft.registry.Factions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Dominion implements INBTSerializable<CompoundTag> {

    public static final Codec<Dominion> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Codec.unboundedMap(CodecHelper.CHUNKPOS_CODEC, ChunkDominion.CODEC).fieldOf("factions").forGetter(Dominion::getChunkDominions)
            ).apply(builder, Dominion::new));

    private Map<ChunkPos, ChunkDominion> chunkDominions = new HashMap<>();

    public Dominion() {
    }

    public Dominion(Map<ChunkPos, ChunkDominion> chunkDominions) {
        this.chunkDominions = chunkDominions;
    }

    public Map<ChunkPos, ChunkDominion> getChunkDominions() {
        return chunkDominions;
    }


    public Dominion setChunkDominions(Map<ChunkPos, ChunkDominion> chunkDominions) {

        this.chunkDominions = new HashMap<>(chunkDominions);
        return this;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag compoundTag = new CompoundTag();
        ListTag list = new ListTag();
        list.addAll(this.getChunkDominions().entrySet().stream().map(entry -> {
            CompoundTag chunkDominionTag = new CompoundTag();
            chunkDominionTag.put("ChunkPos", CodecHelper.CHUNKPOS_CODEC.encodeStart(NbtOps.INSTANCE, entry.getKey()).result().orElseThrow(RuntimeException::new));
            chunkDominionTag.put("ChunkDominion", entry.getValue().serializeNBT());
            return chunkDominionTag;
        }).toList());
        compoundTag.put("ChunkDominions", list);
        return compoundTag;
    }

    @Override
    public void deserializeNBT(CompoundTag pCompound) {
        ListTag list = pCompound.getList("ChunkDominions", 10);
        this.setChunkDominions(list.stream().map(inbt -> {
            CompoundTag chunkDominionTag = (CompoundTag) inbt;
            DataResult<ChunkPos> dataresult = CodecHelper.CHUNKPOS_CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, chunkDominionTag.get("ChunkPos")));
            ChunkPos chunkPos = new ChunkPos(0, 0);
            if(dataresult.resultOrPartial(FactionCraft.LOGGER::error).isPresent()){
                chunkPos = dataresult.resultOrPartial(FactionCraft.LOGGER::error).get();
            }
            ChunkDominion chunkDominion = new ChunkDominion(chunkPos);
            chunkDominion.deserializeNBT(chunkDominionTag.getCompound("ChunkDominion"));
            return new HashMap.SimpleEntry<>(chunkPos, chunkDominion);
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    public void initChunkDominion(ChunkAccess chunk) {
        if(!FactionCraftConfig.ENABLE_DOMINION.get()) return;
        initChunkDominion(chunk.getPos());
    }

    public void initChunkDominion(ChunkPos chunkPos) {

        if(!FactionCraftConfig.ENABLE_DOMINION.get()) return;
        if (!chunkDominions.containsKey(chunkPos)) {
            chunkDominions.put(chunkPos, new ChunkDominion(chunkPos));
        }
    }

    public void adjust(Level level, ChunkPos chunkPos, Faction faction, int adjustment) {
        if(!FactionCraftConfig.ENABLE_DOMINION.get()) return;
        if (!chunkDominions.containsKey(chunkPos)) {
            initChunkDominion(chunkPos);
        }
        chunkDominions.get(chunkPos).adjust(level, chunkPos, faction, adjustment);
    }

    public List<ChunkDominion> getNeighbourDominions(ChunkPos chunkPos) {
        List<ChunkDominion> neighbourDominions = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if ((x == 0 && z != 0) || (x != 0 && z == 0)) {
                    ChunkPos neighbourPos = new ChunkPos(chunkPos.x + x, chunkPos.z + z);
                    if (!chunkDominions.containsKey(neighbourPos)) {
                        initChunkDominion(neighbourPos);
                    }
                    neighbourDominions.add(chunkDominions.get(neighbourPos));
                }
            }
        }
        return neighbourDominions;
    }

    public ChunkDominion getChunkDominion(ChunkPos chunkPos) {
        if (!chunkDominions.containsKey(chunkPos)) {
            initChunkDominion(chunkPos);
        }
        return chunkDominions.get(chunkPos);
    }
}
