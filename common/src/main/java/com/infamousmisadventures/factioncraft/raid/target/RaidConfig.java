package com.infamousmisadventures.factioncraft.raid.target;

import com.infamousmisadventures.factioncraft.faction.Faction;
import com.infamousmisadventures.factioncraft.raid.Raid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;

import java.util.Map;
import java.util.Optional;

public interface RaidConfig {
    RaidConfigType type();

    BlockPos getTargetBlockPos();

    void updateTargetBlockPos(ServerLevel level);

    int getTargetStrength();

    void increaseTargetStrength(int amount);

    int getAdditionalWaves();

    boolean isDefeat(Raid raid, ServerLevel level);

    boolean isValidSpawnPos(int outerAttempt, BlockPos.MutableBlockPos blockpos$mutable, ServerLevel level);

    RaidWaveConfig getWaveRaidConfig();

    float getSpawnDistance();

    Component getRaidBarNameComponent();

    Component getRaidBarDefeatComponent();

    Component getRaidBarVictoryComponent();

    Optional<Holder<SoundEvent>> getWaveSoundEvent();

    Optional<Holder<SoundEvent>> getVictorySoundEvent();

    Optional<Holder<SoundEvent>> getDefeatSoundEvent();

    RaidStrengthConfig getRaidStrengthConfig();

    CompoundTag saveAdditionalData(CompoundTag compoundNbt);

    void loadAdditionalData(ServerLevel level, CompoundTag compoundNBT);

    Map<Faction, Integer> determineFactionFractions(int targetStrength);

    default int getWaveTargetStrength(Raid raid) {
        float waveMultiplier = getRaidStrengthConfig().getBaseMultiplier() + (raid.getGroupsSpawned() * getRaidStrengthConfig().getMultiplierAdjustmentPerWave());
        float spreadMultiplier = ((raid.getLevel().random.nextFloat() * 2) - 1) * getRaidStrengthConfig().getMultiplierSpread();
        float difficultyMultiplier = getRaidStrengthConfig().getDifficultyMultiplier(raid.getLevel().getDifficulty());
        float badOmenMultiplier = getRaidStrengthConfig().getMultiplierPerOmenLevel() * raid.getBadOmenLevel();
        float totalMultiplier = waveMultiplier + spreadMultiplier + difficultyMultiplier + badOmenMultiplier;
        int targetStrength = (int) Math.floor(getTargetStrength() * totalMultiplier);
        return targetStrength;
    }
}
