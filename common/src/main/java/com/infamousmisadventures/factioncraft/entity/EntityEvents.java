package com.infamousmisadventures.factioncraft.entity;

import com.infamousmisadventures.factioncraft.capabilities.factionentity.FactionEntity;
import com.infamousmisadventures.factioncraft.capabilities.factionentity.FactionEntityHelper;
import com.infamousmisadventures.factioncraft.capabilities.patroller.Patroller;
import com.infamousmisadventures.factioncraft.capabilities.patroller.PatrollerHelper;
import com.infamousmisadventures.factioncraft.capabilities.raider.Raider;
import com.infamousmisadventures.factioncraft.capabilities.raider.RaiderHelper;
import com.infamousmisadventures.factioncraft.config.FactionCraftConfig;
import com.infamousmisadventures.factioncraft.dominion.AreaDominion;
import com.infamousmisadventures.factioncraft.dominion.AreaPos;
import com.infamousmisadventures.factioncraft.entity.data.FactionEntityData;
import com.infamousmisadventures.factioncraft.entity.data.MobPatrollerData;
import com.infamousmisadventures.factioncraft.entity.data.holder.IFactionEntityDataHolder;
import com.infamousmisadventures.factioncraft.entity.data.holder.IMobPatrollerDataHolder;
import com.infamousmisadventures.factioncraft.entity.data.holder.IMobRaiderDataHolder;
import com.infamousmisadventures.factioncraft.entity.data.MobRaiderData;
import com.infamousmisadventures.factioncraft.faction.Faction;
import com.infamousmisadventures.factioncraft.level.saveddata.Dominion;
import com.infamousmisadventures.factioncraft.mixin.LivingEntityAccessor;
import com.infamousmisadventures.factioncraft.registry.FCFactions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingConversionEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

import static com.infamousmisadventures.factioncraft.FactionCraft.MODID;
import static net.minecraft.sounds.SoundEvents.SHIELD_BLOCK;

@Mod.EventBusSubscriber(modid = MODID)
public class EntityEvents {

    @SubscribeEvent
    public static void onLivingConversionEvent(LivingConversionEvent.Pre event) {
        if (event.getEntity() instanceof Mob mob) {
            MobRaiderData raiderCap = ((IMobRaiderDataHolder) mob).getOrCreateMobRaiderData();
            if (raiderCap.hasActiveRaid() || patrollerCap.isPatrolling()) {
                event.setCanceled(true);
                event.setConversionTimer(0);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel serverLevel && event.getEntity() instanceof Mob mob) {
            FactionEntityData factionEntity = ((IFactionEntityDataHolder) mob).getOrCreateFactionEntityData();
            if (FactionCraftConfig.ENABLE_DEFAULT_FACTION.get()) {
                if (factionEntity.getFaction() == null || factionEntity.getFaction() == Faction.GAIA) {
                    AreaDominion areaDominion = Dominion.getOrCreate(serverLevel).getAreaDominion(serverLevel, new AreaPos(mob.blockPosition()));
                    List<Faction> factions = FCFactions.getFactionData().values().stream()
                            .filter(faction -> faction.getDefaultEntities().contains(mob.getType()))
                            .filter(faction -> !FactionCraftConfig.ENABLE_DOMINION.get() || areaDominion.getFactionDominion(faction) > FactionCraftConfig.DOMINION_DEFAULT_ENTITY_TRESHOLD.get())
                            .toList();
                    if (!factions.isEmpty()) {
                        RandomSource randomSource = RandomSource.create(new ChunkPos(mob.blockPosition()).toLong());
                        factionEntity.setFaction(factions.get(randomSource.nextInt(factions.size())));
                    }
                }
            }
            MobRaiderData raiderCap = ((IMobRaiderDataHolder) mob).getOrCreateMobRaiderData();
            Patroller patrollerCap = ((IMobPatrollerDataHolder) mob).getOrCreateMobPatrollerData();
            if (raiderCap.hasActiveRaid() || patrollerCap.isPatrolling()) {
                if (mob instanceof AbstractPiglin piglin) {
                    piglin.setImmuneToZombification(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayLevelSoundEvent(PlayLevelSoundEvent.AtPosition event) {
        LivingEntity nearestEntity = event.getLevel().getNearestEntity(LivingEntity.class, TargetingConditions.forNonCombat(), null, event.getPosition().x, event.getPosition().y, event.getPosition().z, AABB.ofSize(event.getPosition(), 1, 1, 1));
        if (nearestEntity instanceof Mob mob && mob.getUseItem().canPerformAction(net.minecraftforge.common.ToolActions.SHIELD_BLOCK) && mob.getLastDamageSource() != null) {
            SoundEvent soundEvent = ((LivingEntityAccessor) mob).invokeGetHurtSound(mob.getLastDamageSource());
            if (event.getSound() == soundEvent) {
                event.setSound(SHIELD_BLOCK);
            }
        }
    }

}