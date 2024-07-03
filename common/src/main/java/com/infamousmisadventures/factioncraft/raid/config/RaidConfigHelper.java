package com.infamousmisadventures.factioncraft.raid.config;

import com.infamousmisadventures.factioncraft.registry.FCRaidConfigTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

public class RaidConfigHelper {

    public static RaidConfig load(ServerLevel level, CompoundTag compoundNBT){
        ResourceLocation type1 = new ResourceLocation(compoundNBT.getString("Type"));
        RaidConfigType raidConfigType = FCRaidConfigTypes.getRaidConfig(type1);
        RaidConfig raidConfig = raidConfigType.create();
        raidConfig.loadAdditionalData(level, compoundNBT);
        return raidConfig;
    }
}
