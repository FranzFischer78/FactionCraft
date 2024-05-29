package com.infamousmisadventures.factioncraft.boost.ai;

import com.infamousmisadventures.factioncraft.boost.Boost;
import com.infamousmisadventures.factioncraft.boost.BoostType;
import com.infamousmisadventures.factioncraft.config.FactionCraftConfig;
import com.infamousmisadventures.factioncraft.entity.ai.goal.UseShieldGoal;
import com.infamousmisadventures.factioncraft.mixins.MobAccessor;
import com.infamousmisadventures.factioncraft.registry.FCBoostTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;

import static com.infamousmisadventures.factioncraft.boost.Boost.BoostGroup.AI;
import static com.infamousmisadventures.factioncraft.boost.Boost.Rarity.NONE;
import static com.infamousmisadventures.factioncraft.tags.EntityTags.CAN_USE_SHIELD;

public class ShieldAIBoost extends Boost {

    public static final Codec<ShieldAIBoost> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("strength_adjustment", 1).forGetter(ShieldAIBoost::getStrengthAdjustment),
            Rarity.CODEC.optionalFieldOf("rarity", NONE).forGetter(ShieldAIBoost::getRarity)
    ).apply(instance, ShieldAIBoost::new));

    private final int strengthAdjustment;
    private final Rarity rarity;

    public ShieldAIBoost(int strengthAdjustment, Rarity rarity) {
        this.strengthAdjustment = strengthAdjustment;
        this.rarity = rarity;
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
        return AI;
    }

    @Override
    public BoostType<? extends Boost> type() {
        return FCBoostTypes.SHIELD_AI.get();
    }

    @Override
    public Rarity getRarity() {
        return rarity;
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
        return FactionCraftConfig.ENABLE_EXPERIMENTAL_FEATURES.get() && livingEntity instanceof PathfinderMob && livingEntity.getType().is(CAN_USE_SHIELD);
    }

    @Override
    public void applyAIChanges(Mob mobEntity) {
        UseShieldGoal useShieldGoal = new UseShieldGoal((PathfinderMob) mobEntity, 7.5D, 60, 160, 15, 1, false);
        ((MobAccessor) mobEntity).getGoalSelector().addGoal(0, useShieldGoal);
    }

    private static boolean requiresDamagedSelector(LivingEntity livingEntity) {
        return livingEntity != null && livingEntity.isAlive() && livingEntity.getHealth() < livingEntity.getMaxHealth();
    }

    private static boolean anySelector(LivingEntity livingEntity) {
        return livingEntity != null && livingEntity.isAlive();
    }
}
