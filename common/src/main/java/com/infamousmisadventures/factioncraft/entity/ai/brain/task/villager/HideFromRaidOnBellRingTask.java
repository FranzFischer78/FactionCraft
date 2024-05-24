package com.infamousmisadventures.factioncraft.entity.ai.brain.task.villager;

import com.google.common.collect.ImmutableMap;
import com.infamousmisadventures.factioncraft.capabilities.raidmanager.RaidManager;
import com.infamousmisadventures.factioncraft.capabilities.raidmanager.RaidManagerHelper;
import com.infamousmisadventures.factioncraft.raid.Raid;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.server.level.ServerLevel;

public class HideFromRaidOnBellRingTask extends Behavior<LivingEntity> {
   public HideFromRaidOnBellRingTask() {
      super(ImmutableMap.of(MemoryModuleType.HEARD_BELL_TIME, MemoryStatus.VALUE_PRESENT));
   }

   protected void start(ServerLevel pLevel, LivingEntity pEntity, long pGameTime) {
      Brain<?> brain = pEntity.getBrain();
      RaidManager raidManagerCapability = RaidManagerHelper.getRaidManagerCapability(pLevel);
      Raid raid = raidManagerCapability.getRaidAt(pEntity.blockPosition());
      if (raid == null) {
         brain.setActiveActivityIfPossible(Activity.HIDE);
      }

   }
}