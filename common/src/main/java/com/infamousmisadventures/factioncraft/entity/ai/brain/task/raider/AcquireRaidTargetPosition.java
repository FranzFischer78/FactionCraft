package com.infamousmisadventures.factioncraft.entity.ai.brain.task.raider;

import com.google.common.collect.ImmutableMap;
import com.infamousmisadventures.factioncraft.entity.data.holder.IMobRaiderDataHolder;
import com.infamousmisadventures.factioncraft.raid.Raid;
import com.infamousmisadventures.factioncraft.registry.FCMemoryModuleTypes;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class AcquireRaidTargetPosition<E extends LivingEntity> extends Behavior<E> {

   public AcquireRaidTargetPosition() {
      super(constructEntryConditionMap(FCMemoryModuleTypes.RAID_WALK_TARGET.get()));

   }

   private static ImmutableMap<MemoryModuleType<?>, MemoryStatus> constructEntryConditionMap(MemoryModuleType<GlobalPos> pMemoryToAcquire) {
      ImmutableMap.Builder<MemoryModuleType<?>, MemoryStatus> builder = ImmutableMap.builder();
      builder.put(pMemoryToAcquire, MemoryStatus.VALUE_ABSENT);

      return builder.build();
   }

   protected boolean checkExtraStartConditions(ServerLevel pLevel, PathfinderMob pEntity) {
      return ((IMobRaiderDataHolder) pEntity).getOrCreateMobRaiderData().getRaid() != null;
   }


   @Override
   protected void start(ServerLevel pLevel, E pEntity, long pGameTime) {
      if (pEntity instanceof Mob mob) {
         Raid raid = ((IMobRaiderDataHolder) mob).getOrCreateMobRaiderData().getRaid();
         if (raid != null) {
            pEntity.getBrain().setMemory(FCMemoryModuleTypes.RAID_WALK_TARGET.get(), GlobalPos.of(pLevel.dimension(), raid.getRaidTarget().getTargetBlockPos()));
         }
      }
      super.start(pLevel, pEntity, pGameTime);
   }
}