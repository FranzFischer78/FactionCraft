package com.infamousmisadventures.factioncraft.item;

import com.infamousmisadventures.factioncraft.registry.FCMobEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OminousBottleItem extends Item {
    private static final int MAX_USE_TIME = 32;
    public static final int BAD_OMEN_LENGTH = 120000;

    public OminousBottleItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        if (!world.isClientSide()) {
            //world.playSound(null, user.position(), SoundEvents.ITEM_OMINOUS_BOTTLE_DISPOSE, user.getSoundCategory(), 1.0f, 1.0f);
            Integer amplifier = getAmplifier(stack);
            user.removeEffect(FCMobEffects.FACTION_BAD_OMEN.get());
            user.addEffect(new MobEffectInstance(FCMobEffects.FACTION_BAD_OMEN.get(), BAD_OMEN_LENGTH, amplifier, false, false, true));
        }
        if(user instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer) user;
            serverPlayer.awardStat(Stats.ITEM_USED.get(this));
            if(serverPlayer.isCreative()) {
                stack.shrink(1);
            }
        }
        return stack;
    }

    private int getAmplifier(ItemStack stack) {
        int amplifier = 0;
        if(stack.getItem() instanceof OminousBottleItem) {
            CompoundTag tag = stack.getOrCreateTag();
            if(tag.contains("Amplifier")) {
                amplifier = tag.getInt("Amplifier");
            }
        }
        return amplifier;
    }


    @Override
    public int getUseDuration(ItemStack stack) {
        return MAX_USE_TIME;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        return ItemUtils.startUsingInstantly(level, player, usedHand);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> components, TooltipFlag flag) {
        super.appendHoverText(itemStack, level, components, flag);
        Integer amplifier = getAmplifier(itemStack);
        List<MobEffectInstance> list = List.of(new MobEffectInstance(FCMobEffects.FACTION_BAD_OMEN.get(), BAD_OMEN_LENGTH, amplifier, false, false, true));
        PotionUtils.addPotionTooltip(list, components, 1.0f);
    }
}

