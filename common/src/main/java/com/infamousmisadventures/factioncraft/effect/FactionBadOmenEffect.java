package com.infamousmisadventures.factioncraft.effect;

import com.infamousmisadventures.factioncraft.level.saveddata.RaidManager;
import com.infamousmisadventures.factioncraft.raid.target.VillageRaidTarget;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

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
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if (pLivingEntity instanceof ServerPlayer player && player.level() instanceof ServerLevel serverlevel && !pLivingEntity.isSpectator()) {
            if (serverlevel.getDifficulty() == Difficulty.PEACEFUL) {
                return;
            }

            if (serverlevel.isVillage(pLivingEntity.blockPosition())) {
                RaidManager raidManagerCapability = RaidManager.getOrCreate(serverlevel);
                raidManagerCapability.createBadOmenRaid(new VillageRaidTarget(player.blockPosition(), serverlevel), player, pAmplifier);
            }
        }
    }
}
