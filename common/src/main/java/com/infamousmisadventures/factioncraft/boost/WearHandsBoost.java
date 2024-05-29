package com.infamousmisadventures.factioncraft.boost;

import com.infamousmisadventures.factioncraft.mixins.MobAccessor;
import com.infamousmisadventures.factioncraft.registry.FCBoostTypes;
import com.infamousmisadventures.factioncraft.util.data.ResourceSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.stream.Collectors;

import static com.infamousmisadventures.factioncraft.boost.Boost.BoostGroup.MAINHAND;
import static com.infamousmisadventures.factioncraft.boost.Boost.BoostGroup.OFFHAND;
import static net.minecraft.world.InteractionHand.MAIN_HAND;

public class WearHandsBoost extends Boost {

    public static final Codec<WearHandsBoost> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.optionalFieldOf("item", null).forGetter(WearHandsBoost::getItem),
            Codec.INT.optionalFieldOf("strength_adjustment", 1).forGetter(WearHandsBoost::getStrengthAdjustment),
            BoostGroup.CODEC.optionalFieldOf("boost_type", MAINHAND).forGetter(WearHandsBoost::getBoostGroup),
            Rarity.CODEC.fieldOf("rarity").forGetter(WearHandsBoost::getRarity),
            ResourceSet.getCodec(Registries.ENTITY_TYPE).optionalFieldOf("allowed_entities", ResourceSet.getEmpty(Registries.ENTITY_TYPE)).forGetter(WearHandsBoost::getAllowedEntities)
    ).apply(instance, WearHandsBoost::new));

    private final ItemStack item;
    private final int strengthAdjustment;
    private final BoostGroup boostGroup;
    private final Rarity rarity;
    private final ResourceSet<EntityType<?>> allowedEntities;

    public WearHandsBoost(ItemStack item, int strengthAdjustment, BoostGroup boostGroup, Rarity rarity, ResourceSet<EntityType<?>> allowedEntities) {
        super();
        this.item = item;
        this.strengthAdjustment = strengthAdjustment;
        this.boostGroup = boostGroup;
        this.rarity = rarity;
        this.allowedEntities = allowedEntities;
    }

    public ItemStack getItem() {
        return item;
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
        return boostGroup;
    }

    @Override
    public BoostType<? extends Boost> type() {
        return FCBoostTypes.WEAR_HANDS.get();
    }

    @Override
    public Rarity getRarity() {
        return rarity;
    }

    public ResourceSet<EntityType<?>> getAllowedEntities() {
        return allowedEntities;
    }

    @Override
    public int apply(LivingEntity livingEntity) {
        if (!canApply(livingEntity)) {
            return 0;
        }
        if(boostGroup.equals(OFFHAND)){
            livingEntity.setItemSlot(EquipmentSlot.OFFHAND, item);
        }else {
            livingEntity.setItemSlot(EquipmentSlot.MAINHAND, item);
        }
        if(livingEntity instanceof Mob mob) {
            applyAIChanges(mob);
        }
        super.apply(livingEntity);
        return strengthAdjustment;
    }

    @Override
    public boolean canApply(LivingEntity livingEntity) {
        return livingEntity instanceof Mob && allowedEntities.emptyOrContains(livingEntity.getType());
    }

    @Override
    public void applyAIChanges(Mob mobEntity) {
        ItemStack itemstack = mobEntity.getItemInHand(MAIN_HAND);
        if (itemstack.is(Items.BOW) && mobEntity instanceof RangedAttackMob) {
//                RangedBowAttackGoal<RangedAttackMob> bowGoal = new RangedBowAttackGoal<>(mobEntity, 1.0D, 20, 15.0F);
//                int i = 20;
//                if (mobEntity.level.getDifficulty() != Difficulty.HARD) {
//                    i = 40;
//                }
//
//                mobEntity.bowGoal.setMinAttackInterval(i);
//                ((MobAccessor) mobEntity).getGoalSelector().addGoal(4, mobEntity.bowGoal);
        } else {
            if(mobEntity instanceof PathfinderMob pathfinder) {
                List<Goal> meleeGoals = ((MobAccessor) pathfinder).getGoalSelector().getAvailableGoals().stream().map(WrappedGoal::getGoal).filter(goal -> goal instanceof MeleeAttackGoal).collect(Collectors.toList());
                if(meleeGoals.isEmpty()) {
                    MeleeAttackGoal meleeGoal = new MeleeAttackGoal(pathfinder, 1.2D, false);
                    ((MobAccessor) mobEntity).getGoalSelector().addGoal(4, meleeGoal);
                }
            }
        }

    }
}
