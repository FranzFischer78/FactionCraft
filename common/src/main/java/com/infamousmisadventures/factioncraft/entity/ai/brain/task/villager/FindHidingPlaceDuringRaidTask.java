package com.infamousmisadventures.factioncraft.entity.ai.brain.task.villager;

import com.infamousmisadventures.factioncraft.capabilities.raidmanager.RaidManager;
import com.infamousmisadventures.factioncraft.capabilities.raidmanager.RaidManagerHelper;
import com.infamousmisadventures.factioncraft.raid.Raid;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.LocateHidingPlace;
import net.minecraft.server.level.ServerLevel;

public class FindHidingPlaceDuringRaidTask extends LocateHidingPlace {
   public FindHidingPlaceDuringRaidTask(int p_i50360_1_, float p_i50360_2_) {
      super(p_i50360_1_, p_i50360_2_, 1);
   }

   protected boolean checkExtraStartConditions(ServerLevel pLevel, LivingEntity pOwner) {
      RaidManager raidManagerCapability = RaidManagerHelper.getRaidManagerCapability(pLevel);
      Raid raid = raidManagerCapability.getRaidAt(pOwner.blockPosition());
      return super.checkExtraStartConditions(pLevel, pOwner) && raid != null && raid.isActive() && !raid.isVictory() && !raid.isLoss();
   }
}