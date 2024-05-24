package com.infamousmisadventures.factioncraft.capabilities.raider;

import net.minecraft.world.entity.Mob;
import net.minecraftforge.common.util.LazyOptional;

import static com.infamousmisadventures.factioncraft.capabilities.ModCapabilities.RAIDER_CAPABILITY;


public class RaiderHelper {

    public static Raider getRaiderCapability(Mob mobEntity)
    {
        return mobEntity.getCapability(RAIDER_CAPABILITY).orElse(new Raider(mobEntity));
    }
}
