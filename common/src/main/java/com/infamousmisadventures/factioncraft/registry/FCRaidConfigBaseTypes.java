package com.infamousmisadventures.factioncraft.registry;

import com.infamousmisadventures.factioncraft.platform.Services;
import com.infamousmisadventures.factioncraft.raid.target.*;

import java.util.function.Supplier;

import static com.infamousmisadventures.factioncraft.registry.FCRegistries.RAID_CONFIG_BASE_TYPE;
import static com.infamousmisadventures.factioncraft.util.ResourceLocationHelper.modLoc;

public class FCRaidConfigBaseTypes {

    public static final Supplier<RaidConfigBaseType<VillageRaidConfigType>> VILLAGE = registerRaidTargetType("village", () -> new RaidConfigBaseType<>(VillageRaidConfigType.CODEC));
    public static final Supplier<RaidConfigBaseType<PlayerRaidConfigType>> PLAYER = registerRaidTargetType("player", () -> new RaidConfigBaseType<>(PlayerRaidConfigType.CODEC));
    //public static final Supplier<RaidConfigBaseType<FactionBattleRaidTarget>> BATTLE = registerRaidTargetType("raid", () -> new RaidConfigBaseType<>(FactionBattleRaidTarget.CODEC));
    
    public static void register() {
    }

    private static <U extends RaidConfigType> Supplier<RaidConfigBaseType<U>> registerRaidTargetType(String id, Supplier<RaidConfigBaseType<U>> attribSup) {
        return Services.REGISTRAR.registerObject(modLoc(id), attribSup, RAID_CONFIG_BASE_TYPE);
    }
}
