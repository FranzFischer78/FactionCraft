package com.infamousmisadventures.factioncraft.mixins;

import com.infamousmisadventures.factioncraft.entity.data.FactionEntityData;
import com.infamousmisadventures.factioncraft.entity.data.holder.IFactionEntityDataHolder;
import com.infamousmisadventures.factioncraft.faction.Faction;
import com.infamousmisadventures.factioncraft.level.saveddata.PlayerFactions;
import com.infamousmisadventures.factioncraft.registry.FCFactions;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.infamousmisadventures.factioncraft.registry.FCFactions.reloadPlayerFactions;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    //Inject at TAIL of placeNewPlayer
    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    private void factioncraft$placeNewPlayer(Connection $$0, ServerPlayer player, CallbackInfo ci) {
        if (player.level() instanceof ServerLevel serverLevel) {
            reloadPlayerFactions();
            PlayerFactions playerFactions = PlayerFactions.getOrCreate(serverLevel);
            if (!playerFactions.hasPlayerFaction(player)) {
                Faction faction = FCFactions.createPlayerFaction(player);
                playerFactions.addPlayerFaction(player, faction);
            }
            FactionEntityData factionEntityCapability = ((IFactionEntityDataHolder) player).getOrCreateFactionEntityData();
            if (!factionEntityCapability.hasFaction()) {
                factionEntityCapability.setFaction(playerFactions.getPlayerFaction(player));
            }
        }
    }
}
