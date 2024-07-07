package com.infamousmisadventures.factioncraft.raid.config.wave;

import com.infamousmisadventures.factioncraft.raid.Raid;
import com.infamousmisadventures.factioncraft.raid.config.raid.RaidConfig;
import com.infamousmisadventures.factioncraft.raid.config.raid.RaidConfigType;
import com.infamousmisadventures.factioncraft.registry.FCRaidConfigTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

public class WaveConfigHelper {
    private static final ResourceLocation RAID_CONFIG = new ResourceLocation("raid_config");

    public static WaveConfig load(ServerLevel level, Raid raid, CompoundTag compoundNBT){
        ResourceLocation type1 = new ResourceLocation(compoundNBT.getString("Type"));
        if(type1.equals(RAID_CONFIG)){
            return raid.getRaidConfig();
        }
        RaidConfigType raidConfigType = FCRaidConfigTypes.getRaidConfig(type1);
        RaidConfig raidConfig = raidConfigType.create();
        raidConfig.loadAdditionalData(level, compoundNBT);
        return raidConfig;
    }
}
