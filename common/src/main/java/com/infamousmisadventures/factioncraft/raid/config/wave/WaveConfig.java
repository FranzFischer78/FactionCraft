package com.infamousmisadventures.factioncraft.raid.config.wave;

import com.infamousmisadventures.factioncraft.faction.Faction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;

import java.util.Map;
import java.util.Optional;

public interface WaveConfig {

    float getSpawnDistance();

    Component getRaidBarNameComponent();

    Optional<Holder<SoundEvent>> getWaveSoundEvent();

    Map<Faction, Integer> determineFactionFractions(int targetStrength);

    default int getSpawnPosAmount() {
        return 1;
    }

    float getMobsFraction();
}