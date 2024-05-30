package com.infamousmisadventures.factioncraft.entity;

import com.google.common.collect.ImmutableList;
import com.infamousmisadventures.factioncraft.entity.ai.brain.task.raider.*;
import com.infamousmisadventures.factioncraft.entity.ai.brain.task.villager.*;
import com.infamousmisadventures.factioncraft.entity.ai.target.FactionAllyHurtTargetGoal;
import com.infamousmisadventures.factioncraft.entity.ai.target.NearestFactionEnemyTargetGoal;
import com.infamousmisadventures.factioncraft.entity.data.MobPatrollerData;
import com.infamousmisadventures.factioncraft.entity.data.holder.IMobPatrollerDataHolder;
import com.infamousmisadventures.factioncraft.entity.data.holder.IMobRaiderDataHolder;
import com.infamousmisadventures.factioncraft.mixins.MobAccessor;
import com.infamousmisadventures.factioncraft.registry.FCActivities;
import com.infamousmisadventures.factioncraft.registry.FCMemoryModuleTypes;
import com.infamousmisadventures.factioncraft.registry.FCSensorTypes;
import com.infamousmisadventures.factioncraft.util.BrainHelper;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;

import static com.infamousmisadventures.factioncraft.FactionCraft.MODID;
import static com.infamousmisadventures.factioncraft.registry.FCMemoryModuleTypes.RAID_WALK_TARGET;
import static com.infamousmisadventures.factioncraft.util.BrainHelper.hasBrain;

@Mod.EventBusSubscriber(modid = MODID)
public class EntityAIEvents {

//    @SubscribeEvent
//    public static void onLivingEntityUpdate(LivingEvent.LivingUpdateEvent event){
//
//    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityJoinLevel(EntityJoinLevelEvent event){
        if(event.getEntity() instanceof Villager){
            addVillagerTasks((Villager) event.getEntity());
        }else if(event.getEntity() instanceof Mob mob){
            if(!hasBrain(mob)){
                ((MobAccessor) mob).getTargetSelector().addGoal(2, new NearestFactionEnemyTargetGoal(mob, 10, true, false));
                ((MobAccessor) mob).getTargetSelector().addGoal(2, new FactionAllyHurtTargetGoal(mob, 10, true, false));
            }else if(brainValid(mob)){
                // Add Brain faction targets
                BrainHelper.addMemory(mob.getBrain(), FCMemoryModuleTypes.NEAREST_VISIBLE_FACTION_ENEMY.get());
                BrainHelper.addMemory(mob.getBrain(), FCMemoryModuleTypes.NEAREST_VISIBLE_FACTION_ALLY.get());
                BrainHelper.addMemory(mob.getBrain(), FCMemoryModuleTypes.NEAREST_VISIBLE_DAMAGED_FACTION_ALLY.get());
                BrainHelper.addSensor(mob.getBrain(), FCSensorTypes.FACTION_SENSOR.get());
                BrainHelper.addMemory(mob.getBrain(), FCMemoryModuleTypes.RAIDED_VILLAGE_POI.get());
                BrainHelper.addMemory(mob.getBrain(), FCMemoryModuleTypes.RAID.get());
                BrainHelper.addMemory(mob.getBrain(), FCMemoryModuleTypes.PATROLLER.get());
                MobPatrollerData patrollerCapability = ((IMobPatrollerDataHolder) mob).getOrCreateMobPatrollerData();
                if(patrollerCapability.isPatrolling()){
                    mob.getBrain().setMemory(FCMemoryModuleTypes.PATROLLER.get(), true);
                }
                addRaiderTasks(mob);
            }
        }
    }

    private static boolean brainValid(Mob mob) {
        return mob.getBrain().getMemories().containsKey(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
    }

    public static <E extends Mob> void addRaiderTasks(E mob) {
        Brain<E> brain = (Brain<E>)mob.getBrain();
        BrainHelper.addMemory(brain, RAID_WALK_TARGET.get());
        BrainHelper.addMemory(brain, FCMemoryModuleTypes.RAIDED_VILLAGE_POI.get());
        BrainHelper.addMemory(brain, FCMemoryModuleTypes.RAID.get());
        BrainHelper.addMemory(brain, FCMemoryModuleTypes.IS_STUCK.get());
        Behavior<? super E> attackTask = BrainHelper.getAttackTask(brain);
        brain.addActivityWithConditions(FCActivities.FACTION_RAIDER_PREP.get(), getRaiderPackage(1.1F, attackTask), Set.of(Pair.of(FCMemoryModuleTypes.RAID.get(), MemoryStatus.VALUE_PRESENT)));
        brain.addActivityWithConditions(FCActivities.FACTION_RAIDER_VILLAGE.get(), getVillageRaiderPackage(0.8F, attackTask), Set.of(Pair.of(FCMemoryModuleTypes.RAIDED_VILLAGE_POI.get(), MemoryStatus.VALUE_PRESENT)));
        brain.addActivityWithConditions(FCActivities.FACTION_PATROL.get(), getPatrollerPackage(((IMobPatrollerDataHolder) mob).getOrCreateMobPatrollerData().getPatrollerWalkSpeed(mob), attackTask), Set.of(Pair.of(FCMemoryModuleTypes.PATROLLER.get(), MemoryStatus.VALUE_PRESENT)));
        ((IMobRaiderDataHolder) mob).getOrCreateMobRaiderData().updateRaidAI();
    }

    public static void addVillagerTasks(Villager villagerEntity) {
        Brain<Villager> brain = villagerEntity.getBrain();
        ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> prioritizedCoreTasks= ImmutableList.of(Pair.of(0, new BeginVillagerRaidTask()));
        BrainHelper.addPrioritizedBehaviors(Activity.CORE, prioritizedCoreTasks, brain);
        BrainHelper.addPrioritizedBehaviors(FCActivities.PRE_FACTION_RAID.get(), getPreRaidPackage(villagerEntity.getVillagerData().getProfession(), 0.5F), brain);
        BrainHelper.addPrioritizedBehaviors(FCActivities.FACTION_RAID.get(), getRaidPackage(villagerEntity.getVillagerData().getProfession(), 0.5F), brain);
    }

    private static ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> getPreRaidPackage(VillagerProfession pProfession, float p_220642_1_) {
        return ImmutableList.of(Pair.of(0, new RingBell()), Pair.of(0, new RunOne<>(ImmutableList.of(Pair.of(new SetWalkTargetFromBlockMemory(MemoryModuleType.MEETING_POINT, p_220642_1_ * 1.5F, 2, 150, 200), 6), Pair.of(new VillageBoundRandomStroll(p_220642_1_ * 1.5F), 2)))), getMinimalLookBehavior(), Pair.of(99, new ForgetRaidTask()));
    }

    private static ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> getRaidPackage(VillagerProfession pProfession, float p_220640_1_) {
        return ImmutableList.of(Pair.of(0, new RunOne<>(ImmutableList.of(Pair.of(MoveToSkySeeingSpot.create(p_220640_1_), 5), Pair.of(VillageBoundRandomStroll.create(p_220640_1_ * 1.1F, 2, 2), 2)))), Pair.of(0, new CelebrateRaidVictoryTask(600, 600)), Pair.of(2, LocateHidingPlace.create(24, p_220640_1_ * 1.4F, 1)), getMinimalLookBehavior(), Pair.of(99, new ForgetRaidTask()));
    }

    private static  <E extends LivingEntity>  ImmutableList<Pair<Integer, ? extends Behavior<? super E>>> getRaiderPackage(float speedModifier, Behavior<? super E> attackTask) {
        if(attackTask != null) {
            return ImmutableList.of(Pair.of(0, new AcquireRaidTargetPosition<>()), Pair.of(1, new BeginRaiderRaidVillageTask()), Pair.of(1, attackTask), Pair.of(2, new RaiderSetWalkTargetFromBlockMemory<>(speedModifier, 2, 150, 200)));
        }else{
            return ImmutableList.of(Pair.of(0, new AcquireRaidTargetPosition<>()), Pair.of(1, new BeginRaiderRaidVillageTask()), Pair.of(2, new RaiderSetWalkTargetFromBlockMemory<>(speedModifier, 2, 150, 200)));
        }
    }

    private static <E extends LivingEntity>  ImmutableList<Pair<Integer, ? extends Behavior<? super E>>> getVillageRaiderPackage(float speedModifier, Behavior<? super E> attackTask) {
        if(attackTask != null){
            return ImmutableList.of(Pair.of(0, new AcquireVillageRaidTarget<>(2)), Pair.of(1, attackTask), Pair.of(2, new RaiderSetWalkTargetFromBlockMemory<>(speedModifier, 2, 150, 200)));
        }else{
            return ImmutableList.of(Pair.of(0, new AcquireVillageRaidTarget<>(2)), Pair.of(2, new RaiderSetWalkTargetFromBlockMemory<>(speedModifier, 2, 150, 200)));
        }
    }

    private static <E extends LivingEntity> ImmutableList<? extends Pair<Integer, ? extends Behavior<? super E>>> getPatrollerPackage(float speedModifier, Behavior<? super E> attackTask) {
        if(attackTask != null) {
            return ImmutableList.of(Pair.of(0, new AcquirePatrolTarget<>(RAID_WALK_TARGET.get(), 5)), Pair.of(1, attackTask), Pair.of(2, new RaiderSetWalkTargetFromBlockMemory<>(speedModifier, 2, 150, 200)));
        }else{
            return ImmutableList.of(Pair.of(0, new AcquirePatrolTarget<>(RAID_WALK_TARGET.get(), 5)), Pair.of(2, new RaiderSetWalkTargetFromBlockMemory<>(speedModifier, 2, 150, 200)));
        }
    }

    private static Pair<Integer, Behavior<LivingEntity>> getMinimalLookBehavior() {
        return Pair.of(5, new RunOne<>(ImmutableList.of(Pair.of(new SetEntityLookTarget(EntityType.VILLAGER, 8.0F), 2), Pair.of(new SetEntityLookTarget(EntityType.PLAYER, 8.0F), 2), Pair.of(new DoNothing(30, 60), 8))));
    }
}
