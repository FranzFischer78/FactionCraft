package com.infamousmisadventures.factioncraft.entity.data.holder;

import com.infamousmisadventures.factioncraft.entity.data.MobPatrollerData;
import com.infamousmisadventures.factioncraft.entity.data.MobRaiderData;

public interface IMobPatrollerDataHolder {
    MobPatrollerData getOrCreateMobPatrollerData();
}
