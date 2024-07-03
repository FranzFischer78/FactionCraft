package com.infamousmisadventures.factioncraft.registry;

import com.infamousmisadventures.factioncraft.raid.config.FactionBattleConfigType;
import com.infamousmisadventures.factioncraft.raid.config.PlayerRaidConfigType;
import com.infamousmisadventures.factioncraft.raid.config.RaidConfigType;
import com.infamousmisadventures.factioncraft.raid.config.VillageRaidConfigType;
import com.infamousmisadventures.factioncraft.raid.target.*;
import com.infamousmisadventures.factioncraft.util.GeneralUtils;
import com.infamousmisadventures.factioncraft.util.data.DefaultsCodecJsonDataManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.infamousmisadventures.factioncraft.util.ResourceLocationHelper.modLoc;

public class FCRaidConfigTypes {
    public static final ResourceLocation RESOURCELOCATION = modLoc("raid_config_types");
    public static final DefaultsCodecJsonDataManager<RaidConfigType> RAID_CONFIG_TYPES = new DefaultsCodecJsonDataManager<>(RESOURCELOCATION, "RaidConfigType", RaidConfigType.CODEC);

    public static final ResourceLocation VILLAGE_RAID_CONFIG = modLoc("generic_village");
    public static final ResourceLocation PLAYER_AMBUSH_CONFIG = modLoc("generic_player");
    public static final ResourceLocation FACTION_BATTLE_CONFIG = modLoc("generic_battle");
    static{
        RAID_CONFIG_TYPES.addDefault(VILLAGE_RAID_CONFIG, VillageRaidConfigType.DEFAULT);
        RAID_CONFIG_TYPES.addDefault(PLAYER_AMBUSH_CONFIG, PlayerRaidConfigType.DEFAULT);
        RAID_CONFIG_TYPES.addDefault(FACTION_BATTLE_CONFIG, FactionBattleConfigType.DEFAULT);
    }



    public static ResourceLocation getKey(RaidConfigType raidConfigType){
        return RAID_CONFIG_TYPES.getData().entrySet().stream().filter(entry -> entry.getValue().equals(raidConfigType)).map(Map.Entry::getKey).findFirst().orElse(null);
    }

    public static RaidConfigType getRaidConfig(ResourceLocation factionResourceLocation){
        return RAID_CONFIG_TYPES.getData().getOrDefault(factionResourceLocation, VillageRaidConfigType.DEFAULT);
    }

    public static boolean RaidTargetExists(ResourceLocation RaidTargetResourceLocation){
        return RAID_CONFIG_TYPES.getData().containsKey(RaidTargetResourceLocation);
    }

    public static Collection<ResourceLocation> RaidTargetKeys(){
        return RAID_CONFIG_TYPES.getData().keySet();
    }

    public static RaidConfigType getRandomRaidTarget(RandomSource random) {
        if(RAID_CONFIG_TYPES.getData().isEmpty()){
            return null;
        }
        return GeneralUtils.getRandomItem(new ArrayList<>(RAID_CONFIG_TYPES.getData().values()), random);
    }

    public static RaidConfigType getRandomRaidTarget(RandomSource random, List<RaidConfigType> whitelist, List<RaidConfigType> blacklist) {
        if(RAID_CONFIG_TYPES.getData().isEmpty()){
            return null;
        }
        List<RaidConfigType> filtered = RAID_CONFIG_TYPES.getData().values().stream().filter(RaidTarget -> whitelist.isEmpty() || whitelist.contains(RaidTarget)).filter(RaidTarget -> !blacklist.contains(RaidTarget)).collect(Collectors.toList());
        return GeneralUtils.getRandomItem(filtered, random);
    }
}
