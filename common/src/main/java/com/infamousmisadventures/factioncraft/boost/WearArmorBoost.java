package com.infamousmisadventures.factioncraft.boost;

import com.infamousmisadventures.factioncraft.registry.FCBoostTypes;
import com.infamousmisadventures.factioncraft.util.data.ResourceSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class WearArmorBoost extends Boost {

    public static final Codec<WearArmorBoost> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.listOf().fieldOf("item_stacks").forGetter(WearArmorBoost::getItemStacks),
            Codec.INT.optionalFieldOf("strength_adjustment", 1).forGetter(WearArmorBoost::getStrengthAdjustment),
            BoostGroup.CODEC.optionalFieldOf("boost_type", BoostGroup.ARMOR).forGetter(WearArmorBoost::getBoostGroup),
            Rarity.CODEC.fieldOf("rarity").forGetter(WearArmorBoost::getRarity),
            ResourceSet.getCodec(Registries.ENTITY_TYPE).optionalFieldOf("allowed_entities", ResourceSet.getEmpty(Registries.ENTITY_TYPE)).forGetter(WearArmorBoost::getAllowedEntities)
    ).apply(instance, WearArmorBoost::new));

    private final List<ItemStack> itemStacks;
    private final int strengthAdjustment;
    private final BoostGroup boostGroup;
    private final Rarity rarity;
    private final ResourceSet<EntityType<?>> allowedEntities;

    public WearArmorBoost(List<ItemStack> itemStacks, int strengthAdjustment, BoostGroup boostGroup, Rarity rarity, ResourceSet<EntityType<?>> allowedEntities) {
        super();
        this.itemStacks = itemStacks;
        this.strengthAdjustment = strengthAdjustment;
        this.boostGroup = boostGroup;
        this.rarity = rarity;
        this.allowedEntities = allowedEntities;
    }

    public List<ItemStack> getItemStacks() {
        return itemStacks;
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
        return FCBoostTypes.WEAR_ARMOR.get();
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
        itemStacks.forEach(itemStack -> livingEntity.setItemSlot(LivingEntity.getEquipmentSlotForItem(itemStack), itemStack));
        super.apply(livingEntity);
        return strengthAdjustment;
    }

    @Override
    public boolean canApply(LivingEntity livingEntity) {
        return livingEntity instanceof Mob && allowedEntities.emptyOrContains(livingEntity.getType());
    }
}
