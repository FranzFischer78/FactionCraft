package com.infamousmisadventures.factioncraft.capabilities.patroller;

import com.infamousmisadventures.factioncraft.capabilities.factionentity.FactionEntity;
import com.infamousmisadventures.factioncraft.capabilities.factionentity.FactionEntityHelper;
import com.infamousmisadventures.factioncraft.capabilities.factioninteraction.FactionInteraction;
import com.infamousmisadventures.factioncraft.capabilities.factioninteraction.FactionInteractionHelper;
import com.infamousmisadventures.factioncraft.config.FactionCraftConfig;
import com.infamousmisadventures.factioncraft.effect.ModMobEffects;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.infamousmisadventures.factioncraft.FactionCraft.MODID;

@Mod.EventBusSubscriber(modid = MODID)
public class PatrollerEvents {

    @SubscribeEvent
    public static void onLivingDeathEvent(LivingDeathEvent event) {
        LivingEntity livingEntity = event.getEntity();
        Entity sourceEntity = event.getSource().getEntity();
        if (!livingEntity.level.isClientSide() && livingEntity instanceof Mob mob && sourceEntity instanceof Player playerEntity) {
            Patroller cap = ((IMobPatrollerDataHolder) mob).getOrCreateMobPatrollerData();
            if (cap.isPatrolLeader()) {
                MobEffectInstance effectinstance1 = playerEntity.getEffect(ModMobEffects.FACTION_BAD_OMEN.get());
                int i = 1;
                if (effectinstance1 != null) {
                    i += effectinstance1.getAmplifier();
                    playerEntity.removeEffectNoUpdate(ModMobEffects.FACTION_BAD_OMEN.get());
                } else {
                    --i;
                }

                i = Mth.clamp(i, 0, FactionCraftConfig.RAID_MAX_FACTIONS.get() - 1);
                MobEffectInstance effectinstance = new MobEffectInstance(ModMobEffects.FACTION_BAD_OMEN.get(), 120000, i, false, false, true);
                if (!FactionCraftConfig.DISABLE_FACTION_RAIDS.get()) {
                    FactionInteraction factionInteractionCapability = FactionInteractionHelper.getFactionInteractionCapability(playerEntity);
                    if (factionInteractionCapability.getBadOmenFactions().size() < FactionCraftConfig.RAID_MAX_FACTIONS.get()) {
                        FactionEntity factionEntityCapability = FactionEntityHelper.getFactionEntityCapability(mob);
                        if (factionEntityCapability.getFaction() != null) {
                            factionInteractionCapability.addBadOmenFaction(factionEntityCapability.getFaction());
                        }
                    }
                    playerEntity.addEffect(effectinstance);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onAllowDespawn(LivingSpawnEvent.AllowDespawn event) {
        LivingEntity livingEntity = event.getEntity();
        if (!livingEntity.level.isClientSide() && livingEntity instanceof Mob mobEntity) {
            Patroller cap = PatrollerHelper.getPatrollerCapability(mobEntity);
            if (cap.isPatrolling()) {
                event.setResult(Event.Result.DENY);
            }
        }
    }

}
