package com.infamousmisadventures.factioncraft.effect;

import com.infamousmisadventures.factioncraft.faction.Faction;
import com.infamousmisadventures.factioncraft.level.saveddata.RaidManager;
import com.infamousmisadventures.factioncraft.raid.Raid;
import com.infamousmisadventures.factioncraft.raid.target.RaidConfig;
import com.infamousmisadventures.factioncraft.raid.target.RaidConfigType;
import com.infamousmisadventures.factioncraft.registry.FCMobEffects;
import com.infamousmisadventures.factioncraft.registry.FCRaidConfigBaseTypes;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class FactionBadOmenEffect extends MobEffect {
    public FactionBadOmenEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }


    /**
     * checks if Potion effect is ready to be applied this tick.
     */
    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int amplifier) {
        if (pLivingEntity instanceof ServerPlayer player && player.level() instanceof ServerLevel serverlevel && !pLivingEntity.isSpectator()) {
            if (serverlevel.getDifficulty() == Difficulty.PEACEFUL) {
                return;
            }
            if (serverlevel.isVillage(pLivingEntity.blockPosition())) {
                RaidManager raidManagerCapability = RaidManager.getOrCreate(serverlevel);
                Faction badOmenFaction = getBadOmenFaction(player, amplifier);
                List<RaidConfigType> raidConfigTypes = badOmenFaction.getRaidConfigs();
                List<RaidConfigType> list = raidConfigTypes.stream().filter(raidConfig -> FCRaidConfigBaseTypes.VILLAGE.get().equals(raidConfig.baseType())).toList();
                if (list.isEmpty()) {
                    return;
                }
                RaidConfigType raidConfigType = list.get(0);
                RaidConfig raidConfig = raidConfigType.create();
                Raid raid = raidManagerCapability.createRaid(raidConfig);
                clearBadOmen(player, raid);
            }
        }
    }

    private Faction getBadOmenFaction(ServerPlayer player, int amplifier) {
        return null;
    }

    private void clearBadOmen(ServerPlayer player, Raid raid) {
        player.removeEffect(FCMobEffects.FACTION_BAD_OMEN.get());
        player.connection.send(new ClientboundEntityEventPacket(player, (byte) 43));
        if (!raid.hasFirstWaveSpawned()) {
            player.awardStat(Stats.RAID_TRIGGER);
            CriteriaTriggers.BAD_OMEN.trigger(player);
        }
    }
}
