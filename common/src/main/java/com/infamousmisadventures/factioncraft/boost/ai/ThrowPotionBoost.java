package com.infamousmisadventures.factioncraft.boost.ai;

import com.infamousmisadventures.factioncraft.boost.Boost;
import com.infamousmisadventures.factioncraft.boost.BoostType;
import com.infamousmisadventures.factioncraft.entity.ai.goal.NearestFactionAllyTargetGoal;
import com.infamousmisadventures.factioncraft.entity.ai.goal.ThrowPotionGoal;
import com.infamousmisadventures.factioncraft.entity.data.FactionEntityData;
import com.infamousmisadventures.factioncraft.entity.data.holder.IFactionEntityDataHolder;
import com.infamousmisadventures.factioncraft.mixins.MobAccessor;
import com.infamousmisadventures.factioncraft.registry.FCBoostTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;

import static com.infamousmisadventures.factioncraft.boost.Boost.BoostGroup.AI;
import static com.infamousmisadventures.factioncraft.boost.Boost.Rarity.NONE;
import static net.minecraft.core.registries.BuiltInRegistries.POTION;

public class ThrowPotionBoost extends Boost {

    public static final Codec<ThrowPotionBoost> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            POTION.byNameCodec().optionalFieldOf("potion", Potions.HARMING).forGetter(ThrowPotionBoost::getPotion),
            Codec.BOOL.optionalFieldOf("beneficial", false).forGetter(ThrowPotionBoost::isBeneficial),
            Codec.BOOL.optionalFieldOf("requires_damaged", false).forGetter(ThrowPotionBoost::isRequiresDamaged),
            Codec.INT.optionalFieldOf("strength_adjustment", 1).forGetter(ThrowPotionBoost::getStrengthAdjustment),
            Rarity.CODEC.optionalFieldOf("rarity", NONE).forGetter(ThrowPotionBoost::getRarity)
    ).apply(instance, ThrowPotionBoost::new));

    private final Potion potion;
    private final boolean beneficial;
    private final boolean requiresDamaged;
    private final int strengthAdjustment;
    private final Rarity rarity;

    public ThrowPotionBoost(Potion potion, boolean beneficial, boolean requiresDamaged, int strengthAdjustment, Rarity rarity) {
        this.potion = potion;
        this.beneficial = beneficial;
        this.requiresDamaged = requiresDamaged;
        this.strengthAdjustment = strengthAdjustment;
        this.rarity = rarity;
    }

    public Potion getPotion() {
        return potion;
    }

    public boolean isBeneficial() {
        return beneficial;
    }

    public boolean isRequiresDamaged() {
        return requiresDamaged;
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
        return FCBoostTypes.THROW_POTION.get();
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
        return livingEntity instanceof Mob;
    }

    @Override
    public void applyAIChanges(Mob mobEntity) {
        FactionEntityData factionEntity = ((IFactionEntityDataHolder) mobEntity).getOrCreateFactionEntityData();
        ThrowPotionGoal throwPotionGoal = new ThrowPotionGoal(mobEntity, 1.0D, 60, 10.0F, potion, this.isBeneficial() ? factionEntity::getNearestDamagedFactionAlly : mobEntity::getTarget);
        ((MobAccessor) mobEntity).getGoalSelector().addGoal(2, throwPotionGoal);
        if(isBeneficial()) {
            NearestFactionAllyTargetGoal<LivingEntity> nearestFactionAllyTargetGoal = new NearestFactionAllyTargetGoal<>(mobEntity, LivingEntity.class, true, isRequiresDamaged() ? ThrowPotionBoost::requiresDamagedSelector : ThrowPotionBoost::anySelector);
            ((MobAccessor) mobEntity).getTargetSelector().addGoal(1, nearestFactionAllyTargetGoal);
        }
    }

    private static boolean requiresDamagedSelector(LivingEntity livingEntity) {
        return livingEntity != null && livingEntity.isAlive() && livingEntity.getHealth() < livingEntity.getMaxHealth();
    }

    private static boolean anySelector(LivingEntity livingEntity) {
        return livingEntity != null && livingEntity.isAlive();
    }
}
