package com.infamousmisadventures.factioncraft.entity.ai.brain.task.villager;

import com.infamousmisadventures.factioncraft.capabilities.raidmanager.RaidManager;
import com.infamousmisadventures.factioncraft.capabilities.raidmanager.RaidManagerHelper;
import com.infamousmisadventures.factioncraft.raid.Raid;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.VillageBoundRandomStroll;
import net.minecraft.server.level.ServerLevel;

public class FindWalkTargetAfterRaidVictoryTask extends VillageBoundRandomStroll {
   public FindWalkTargetAfterRaidVictoryTask(float p_i50337_1_) {
      super(p_i50337_1_);
   }

   protected boolean checkExtraStartConditions(ServerLevel pLevel, PathfinderMob pOwner) {
      RaidManager raidManagerCapability = RaidManagerHelper.getRaidManagerCapability(pLevel);
      Raid raid = raidManagerCapability.getRaidAt(pOwner.blockPosition());
      return raid != null && raid.isVictory() && super.checkExtraStartConditions(pLevel, pOwner);
   }
}