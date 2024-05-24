package com.infamousmisadventures.factioncraft.capabilities.dominion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.infamousmisadventures.factioncraft.FactionCraft;
import com.infamousmisadventures.factioncraft.config.FactionCraftConfig;
import com.infamousmisadventures.factioncraft.faction.Faction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.Level;
import com.infamousmisadventures.factioncraft.util.INBTSerializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Dominion implements INBTSerializable<CompoundTag> {

    public static final Codec<Dominion> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Codec.unboundedMap(AreaPos.AREAPOS_CODEC, AreaDominion.CODEC).fieldOf("factions").forGetter(Dominion::getAreaDominions)
            ).apply(builder, Dominion::new));

    private Map<AreaPos, AreaDominion> areaDominions = new HashMap<>();

    public Dominion() {
    }

    public Dominion(Map<AreaPos, AreaDominion> areaDominions) {
        this.areaDominions = areaDominions;
    }

    public Map<AreaPos, AreaDominion> getAreaDominions() {
        return areaDominions;
    }


    public Dominion setAreaDominions(Map<AreaPos, AreaDominion> areaDominions) {

        this.areaDominions = new HashMap<>(areaDominions);
        return this;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag compoundTag = new CompoundTag();
        ListTag list = new ListTag();
        list.addAll(this.getAreaDominions().entrySet().stream().map(entry -> {
            CompoundTag areaDominionTag = new CompoundTag();
            areaDominionTag.put("AreaPos", AreaPos.AREAPOS_CODEC.encodeStart(NbtOps.INSTANCE, entry.getKey()).result().orElseThrow(RuntimeException::new));
            areaDominionTag.put("AreaDominion", entry.getValue().serializeNBT());
            return areaDominionTag;
        }).toList());
        compoundTag.put("AreaDominions", list);
        return compoundTag;
    }

    @Override
    public void deserializeNBT(CompoundTag pCompound) {
        ListTag list = pCompound.getList("AreaDominions", 10);
        this.setAreaDominions(list.stream().map(inbt -> {
            CompoundTag areaDominionTag = (CompoundTag) inbt;
            DataResult<AreaPos> dataresult = AreaPos.AREAPOS_CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, areaDominionTag.get("AreaPos")));
            AreaPos areaPos = new AreaPos(0, 0);
            if(dataresult.resultOrPartial(FactionCraft.LOGGER::error).isPresent()){
                areaPos = dataresult.resultOrPartial(FactionCraft.LOGGER::error).get();
            }
            AreaDominion areaDominion = new AreaDominion(areaPos);
            areaDominion.deserializeNBT(areaDominionTag.getCompound("AreaDominion"));
            return new HashMap.SimpleEntry<>(areaPos, areaDominion);
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    public void initAreaDominion(Level level, AreaPos areaPos) {
        if(!FactionCraftConfig.ENABLE_DOMINION.get()) return;
        if (!areaDominions.containsKey(areaPos)) {
            areaDominions.put(areaPos, new AreaDominion(level, areaPos));
        }
    }

    public void adjust(Level level, AreaPos areaPos, Faction faction, int adjustment) {
        if(!FactionCraftConfig.ENABLE_DOMINION.get()) return;
        if (!areaDominions.containsKey(areaPos)) {
            initAreaDominion(level, areaPos);
        }
        areaDominions.get(areaPos).adjust(level, areaPos, faction, adjustment);
    }

    public List<AreaDominion> getNeighbourDominions(Level level, AreaPos areaPos) {
        List<AreaDominion> neighbourDominions = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if ((x == 0 && z != 0) || (x != 0 && z == 0)) {
                    AreaPos neighbourPos = new AreaPos(areaPos.x + x, areaPos.z + z);
                    if (!areaDominions.containsKey(neighbourPos)) {
                        initAreaDominion(level, neighbourPos);
                    }
                    neighbourDominions.add(areaDominions.get(neighbourPos));
                }
            }
        }
        return neighbourDominions;
    }

    public AreaDominion getAreaDominion(Level level, AreaPos areaPos) {
        if (!areaDominions.containsKey(areaPos)) {
            initAreaDominion(level, areaPos);
        }
        return areaDominions.get(areaPos);
    }
}
