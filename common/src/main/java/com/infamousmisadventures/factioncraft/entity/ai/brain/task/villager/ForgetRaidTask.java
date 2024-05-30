package com.infamousmisadventures.factioncraft.entity.ai.brain.task.villager;

import com.google.common.collect.ImmutableMap;
import com.infamousmisadventures.factioncraft.level.saveddata.RaidManager;
import com.infamousmisadventures.factioncraft.raid.Raid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.schedule.Activity;

public class ForgetRaidTask extends Behavior<LivingEntity> {
   public ForgetRaidTask() {
      super(ImmutableMap.of());
   }

   protected boolean checkExtraStartConditions(ServerLevel pLevel, LivingEntity pOwner) {
      return pLevel.random.nextInt(20) == 0;
   }

   protected void start(ServerLevel pLevel, LivingEntity pEntity, long pGameTime) {
      Brain<?> brain = pEntity.getBrain();
      RaidManager raidManagerCapability = RaidManager.getOrCreate(pLevel);
      Raid raid = raidManagerCapability.getRaidAt(pEntity.blockPosition());
      if (raid == null || raid.isStopped() || raid.isLoss()) {
         brain.setDefaultActivity(Activity.IDLE);
         brain.updateActivityFromSchedule(pLevel.getDayTime(), pLevel.getGameTime());
      }

   }
}