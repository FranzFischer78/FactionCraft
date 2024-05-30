package com.infamousmisadventures.factioncraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableSet;
import com.infamousmisadventures.factioncraft.entity.data.FactionEntityData;
import com.infamousmisadventures.factioncraft.entity.data.holder.IFactionEntityDataHolder;
import com.infamousmisadventures.factioncraft.faction.Faction;
import com.infamousmisadventures.factioncraft.registry.FCMemoryModuleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;

import java.util.Optional;
import java.util.Set;

public class FactionSpecificSensor extends Sensor<LivingEntity> {
   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.of(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, FCMemoryModuleTypes.NEAREST_VISIBLE_FACTION_ALLY.get(), FCMemoryModuleTypes.NEAREST_VISIBLE_DAMAGED_FACTION_ALLY.get(), FCMemoryModuleTypes.NEAREST_VISIBLE_FACTION_ENEMY.get());
   }

   protected void doTick(ServerLevel pLevel, LivingEntity pEntity) {
      Brain<?> brain = pEntity.getBrain();
      Optional<LivingEntity> optional = Optional.empty();
      Optional<LivingEntity> optional1 = Optional.empty();
      Optional<LivingEntity> optional2 = Optional.empty();
      Optional<Mob> optional3 = Optional.empty();
      NearestVisibleLivingEntities nearestvisiblelivingentities = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());

      for(LivingEntity livingentity : nearestvisiblelivingentities.findAll((p_186157_) -> {
         return p_186157_ instanceof Mob;
      })) {
         if(pEntity instanceof Mob mob) {
            FactionEntityData thisFactionEntityCapability = ((IFactionEntityDataHolder) mob).getOrCreateFactionEntityData();
            Faction thisEntityFaction = thisFactionEntityCapability.getFaction();
            if(thisEntityFaction != null) {
               FactionEntityData factionEntityCapability = ((IFactionEntityDataHolder) livingentity).getOrCreateFactionEntityData();
               Faction entityFaction = factionEntityCapability.getFaction();
               if (thisEntityFaction.isEnemyOf(entityFaction)) {
                  optional2 = Optional.of(livingentity);
                  optional3 = Optional.of((Mob) livingentity);
               } else if (thisEntityFaction.equals(entityFaction)) {
                  optional = Optional.of(livingentity);
                  if (livingentity.getHealth() < livingentity.getMaxHealth()) {
                     optional1 = Optional.of(livingentity);
                  }
               }
            }
         }

      }

      brain.setMemory(FCMemoryModuleTypes.NEAREST_VISIBLE_FACTION_ALLY.get(), optional);
      brain.setMemory(FCMemoryModuleTypes.NEAREST_VISIBLE_DAMAGED_FACTION_ALLY.get(), optional1);
      brain.setMemory(FCMemoryModuleTypes.NEAREST_VISIBLE_FACTION_ENEMY.get(), optional2);
      if (optional3.isPresent()) {
         brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS, optional3);
         brain.setMemory(MemoryModuleType.NEAREST_ATTACKABLE, optional3);
         brain.setMemory(MemoryModuleType.NEAREST_HOSTILE, optional3);
         brain.setMemory(MemoryModuleType.ATTACK_TARGET, optional3);
      }
   }

}