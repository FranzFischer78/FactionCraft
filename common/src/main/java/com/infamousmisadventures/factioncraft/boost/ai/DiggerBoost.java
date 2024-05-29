package com.infamousmisadventures.factioncraft.boost.ai;

import com.google.common.collect.ImmutableList;
import com.infamousmisadventures.factioncraft.boost.Boost;
import com.infamousmisadventures.factioncraft.boost.BoostType;
import com.infamousmisadventures.factioncraft.config.FactionCraftConfig;
import com.infamousmisadventures.factioncraft.entity.ai.brain.task.raider.DigTask;
import com.infamousmisadventures.factioncraft.entity.ai.goal.FactionDigGoal;
import com.infamousmisadventures.factioncraft.mixins.MobAccessor;
import com.infamousmisadventures.factioncraft.registry.FCActivities;
import com.infamousmisadventures.factioncraft.registry.FCBoostTypes;
import com.infamousmisadventures.factioncraft.util.BrainHelper;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Set;

import static com.infamousmisadventures.factioncraft.boost.Boost.BoostGroup.SPECIAL;
import static com.infamousmisadventures.factioncraft.boost.Boost.Rarity.NONE;
import static com.infamousmisadventures.factioncraft.registry.FCMemoryModuleTypes.IS_STUCK;

public class DiggerBoost extends Boost {

    public static final Codec<DiggerBoost> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("requires_tool", true).forGetter(DiggerBoost::isRequiresTool),
            Codec.BOOL.optionalFieldOf("requires_proper_tool", true).forGetter(DiggerBoost::isRequiresProperTool),
            Codec.BOOL.optionalFieldOf("off_hand_tool", false).forGetter(DiggerBoost::isOffHandTool),
            Codec.INT.optionalFieldOf("strength_adjustment", 1).forGetter(DiggerBoost::getStrengthAdjustment),
            Rarity.CODEC.optionalFieldOf("rarity", NONE).forGetter(DiggerBoost::getRarity)
    ).apply(instance, DiggerBoost::new));

    private final boolean requiresTool;
    private final boolean requiresProperTool;
    private final boolean offHandTool;
    private final int strengthAdjustment;
    private final Rarity rarity;

    public DiggerBoost(boolean requiresTool, boolean requiresProperTool, boolean offHandTool, int strengthAdjustment, Rarity rarity) {
        this.requiresTool = requiresTool;
        this.requiresProperTool = requiresProperTool;
        this.offHandTool = offHandTool;
        this.strengthAdjustment = strengthAdjustment;
        this.rarity = rarity;
    }

    public boolean isRequiresTool() {
        return requiresTool;
    }

    public boolean isRequiresProperTool() {
        return requiresProperTool;
    }

    public boolean isOffHandTool() {
        return offHandTool;
    }

    public EquipmentSlot getHand() {
        return offHandTool ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
    }

    public int getStrengthAdjustment() {
        return strengthAdjustment;
    }

    @Override
    public Codec<? extends Boost> getCodec() {
        return CODEC;
    }

    @Override
    public BoostGroup getBoostGroup() {
        return SPECIAL;
    }

    @Override
    public Rarity getRarity() {
        return rarity;
    }

    @Override
    public BoostType<? extends Boost> type() {
        return FCBoostTypes.DIGGER.get();
    }

    @Override
    public int apply(LivingEntity livingEntity) {
        if (!canApply(livingEntity)) {
            return 0;
        }
        if (livingEntity instanceof Mob mob) {
            applyAIChanges(mob);
        }
        super.apply(livingEntity);
        return strengthAdjustment;
    }

    @Override
    public boolean canApply(LivingEntity livingEntity) {
        return livingEntity instanceof Mob;
    }

    @Override
    public void applyAIChanges(Mob mobEntity) {
        if (mobEntity instanceof PathfinderMob pathfinder && FactionCraftConfig.ENABLE_DIGGER_AI.get()) {
            if(BrainHelper.hasBrain(mobEntity)){
                mobEntity.getBrain().addActivityWithConditions(FCActivities.DIG.get(), getDiggerPackage(1.1F, mobEntity), Set.of(Pair.of(IS_STUCK.get(), MemoryStatus.VALUE_PRESENT)));
            }else {
                FactionDigGoal meleeGoal = new FactionDigGoal(pathfinder, requiresTool, requiresProperTool, getHand());
                ((MobAccessor) mobEntity).getGoalSelector().addGoal(2, meleeGoal);
            }
        }
    }

    private <E extends LivingEntity> ImmutableList<? extends Pair<Integer, ? extends Behavior<? super E>>> getDiggerPackage(float speedModifier, E mobEntity) {
        if (!(mobEntity instanceof Mob)) {
            return ImmutableList.of();
        }
        Brain<E> brain = (Brain<E>)mobEntity.getBrain();
//        Behavior<? super E> attackTask = BrainHelper.getAttackTask(brain);
        return ImmutableList.of(Pair.of(0, new DigTask((Mob) mobEntity, requiresTool, requiresProperTool, getHand())));
    }
}
