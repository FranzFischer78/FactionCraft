package com.infamousmisadventures.factioncraft.raid.config;

import com.infamousmisadventures.factioncraft.registry.FCRegistries;
import com.mojang.serialization.Codec;

public abstract class RaidConfigType {
    public static Codec<RaidConfigType> CODEC = FCRegistries.RAID_CONFIG_BASE_TYPE.byNameCodec().dispatch(RaidConfigType::baseType, RaidConfigBaseType::codec);
    public static float DEFAULT_MOBS_FRACTION = 0.7F;

    public abstract RaidConfig create();
    public abstract RaidConfigBaseType<? extends RaidConfigType> baseType();
}
