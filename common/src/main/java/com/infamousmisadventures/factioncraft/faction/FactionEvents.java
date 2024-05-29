package com.infamousmisadventures.factioncraft.faction;

import com.infamousmisadventures.factioncraft.entity.data.FactionEntityData;
import com.infamousmisadventures.factioncraft.entity.data.holder.IFactionEntityDataHolder;
import com.infamousmisadventures.factioncraft.level.saveddata.PlayerFactions;
import com.infamousmisadventures.factioncraft.registry.FCFactions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.infamousmisadventures.factioncraft.FactionCraft.MODID;
import static com.infamousmisadventures.factioncraft.faction.Faction.GAIA;
import static com.infamousmisadventures.factioncraft.registry.FCFactions.reloadPlayerFactions;
import static net.minecraft.world.level.Level.OVERWORLD;

@Mod.EventBusSubscriber(modid = MODID)
public class FactionEvents {

    @SubscribeEvent
    public static void onLivingChangeTargetEvent(LivingChangeTargetEvent event){
        LivingEntity livingEntity = event.getEntity();
        if(!livingEntity.level().isClientSide() && event.getNewTarget() != null) {
            FactionEntityData sourceCap = ((IFactionEntityDataHolder)event.getEntity()).getOrCreateFactionEntityData();
            FactionEntityData targetCap = ((IFactionEntityDataHolder)event.getNewTarget()).getOrCreateFactionEntityData();
            if (sameFaction(targetCap, sourceCap) || sourceCap.getFaction().isAllyOf(targetCap.getFaction())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurtEvent(LivingAttackEvent event){
        LivingEntity livingEntity = event.getEntity();
        if(!livingEntity.level().isClientSide() && event.getEntity() instanceof Mob && event.getSource().getEntity() instanceof Mob) {
            FactionEntityData sourceCap = ((IFactionEntityDataHolder)event.getEntity()).getOrCreateFactionEntityData();
            FactionEntityData targetCap = ((IFactionEntityDataHolder)event.getSource()).getOrCreateFactionEntityData();
            if (sameFaction(targetCap, sourceCap) || sourceCap.getFaction().isAllyOf(targetCap.getFaction())) {
                event.setCanceled(true);
            }
        }
    }

    private static boolean sameFaction(FactionEntityData targetCap, FactionEntityData sourceCap) {
        return !GAIA.equals(targetCap.getFaction()) && targetCap.getFaction() == sourceCap.getFaction();
    }

    // On player join event, check if Player Factions already has the correct player faction, if not, create it and add it to both the Player Factions and Factions
    @SubscribeEvent
    public static void onPlayerJoinEvent(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player.level() instanceof ServerLevel serverLevel) {
        PlayerFactions playerFactions = PlayerFactions.getOrCreate(serverLevel);
        if (!playerFactions.hasPlayerFaction(player)) {
            Faction faction = FCFactions.createPlayerFaction(player);
            playerFactions.addPlayerFaction(player, faction);
        }
        FactionEntityData factionEntityCapability = ((IFactionEntityDataHolder) player).getOrCreateFactionEntityData();
        if(!factionEntityCapability.hasFaction()) {
            factionEntityCapability.setFaction(playerFactions.getPlayerFaction(player));
        }
        }
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        LevelAccessor levelAccessor = event.getLevel();
        if(levelAccessor.isClientSide()) return;
        if(levelAccessor instanceof Level && ((Level) levelAccessor).dimension().equals(OVERWORLD)) {
            reloadPlayerFactions();
        }
    }
}
