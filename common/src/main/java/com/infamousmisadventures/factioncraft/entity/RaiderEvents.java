package com.infamousmisadventures.factioncraft.entity;

import com.infamousmisadventures.factioncraft.entity.data.MobRaiderData;
import com.infamousmisadventures.factioncraft.entity.data.holder.IMobRaiderDataHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.infamousmisadventures.factioncraft.FactionCraft.MODID;

@Mod.EventBusSubscriber(modid = MODID)
public class RaiderEvents {

    @SubscribeEvent
    public static void onLivingHurtEvent(LivingHurtEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!livingEntity.level().isClientSide() && livingEntity instanceof Mob mob) {
            MobRaiderData cap = ((IMobRaiderDataHolder) mob).getOrCreateMobRaiderData();
            if (cap.hasActiveRaid()) {
                cap.getRaid().updateBossbar();
            }
        }
    }
}
