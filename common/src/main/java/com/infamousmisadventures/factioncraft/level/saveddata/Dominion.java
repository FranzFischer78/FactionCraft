package com.infamousmisadventures.factioncraft.level.saveddata;

import com.infamousmisadventures.factioncraft.FCConstants;
import com.infamousmisadventures.factioncraft.config.FactionCraftConfig;
import com.infamousmisadventures.factioncraft.dominion.AreaDominion;
import com.infamousmisadventures.factioncraft.dominion.AreaPos;
import com.infamousmisadventures.factioncraft.faction.Faction;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.infamousmisadventures.factioncraft.FCConstants.MOD_ID;

public class Dominion extends SavedData {


    private Map<AreaPos, AreaDominion> areaDominions = new HashMap<>();


    public static Dominion getOrCreate(ServerLevel level)
    {
        return level.getDataStorage().computeIfAbsent(Dominion::loadStatic, Dominion::create, MOD_ID + "-dominion");
    }

    public static Dominion loadStatic(CompoundTag tag)
    {
        Dominion dominion = create();
        dominion.load(tag);
        return dominion;
    }

    public static Dominion create()
    {
        return new Dominion();
    }

    public Map<AreaPos, AreaDominion> getAreaDominions() {
        return areaDominions;
    }


    @Override
    public CompoundTag save(CompoundTag compoundTag) {
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

    public void load(CompoundTag tag) {
        ListTag list = tag.getList("AreaDominions", 10);
        this.setAreaDominions(list.stream().map(inbt -> {
            CompoundTag areaDominionTag = (CompoundTag) inbt;
            DataResult<AreaPos> dataresult = AreaPos.AREAPOS_CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, areaDominionTag.get("AreaPos")));
            AreaPos areaPos = new AreaPos(0, 0);
            if(dataresult.resultOrPartial(FCConstants.LOGGER::error).isPresent()){
                areaPos = dataresult.resultOrPartial(FCConstants.LOGGER::error).get();
            }
            AreaDominion areaDominion = new AreaDominion(areaPos);
            areaDominion.deserializeNBT(areaDominionTag.getCompound("AreaDominion"));
            return new HashMap.SimpleEntry<>(areaPos, areaDominion);
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    public Dominion setAreaDominions(Map<AreaPos, AreaDominion> areaDominions) {

        this.areaDominions = new HashMap<>(areaDominions);
        return this;
    }

    public void initAreaDominion(ServerLevel level, AreaPos areaPos) {
        if(!FactionCraftConfig.ENABLE_DOMINION.get()) return;
        if (!areaDominions.containsKey(areaPos)) {
            areaDominions.put(areaPos, new AreaDominion(level, areaPos));
        }
    }

    public void adjust(ServerLevel level, AreaPos areaPos, Faction faction, int adjustment) {
        if(!FactionCraftConfig.ENABLE_DOMINION.get()) return;
        if (!areaDominions.containsKey(areaPos)) {
            initAreaDominion(level, areaPos);
        }
        areaDominions.get(areaPos).adjust(level, areaPos, faction, adjustment);
    }

    public List<AreaDominion> getNeighbourDominions(ServerLevel level, AreaPos areaPos) {
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

    public AreaDominion getAreaDominion(ServerLevel level, AreaPos areaPos) {
        if (!areaDominions.containsKey(areaPos)) {
            initAreaDominion(level, areaPos);
        }
        return areaDominions.get(areaPos);
    }

    public void tick() {
        this.setDirty();
    }
}
