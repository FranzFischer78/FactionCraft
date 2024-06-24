package com.infamousmisadventures.factioncraft.raid.target;

import com.infamousmisadventures.factioncraft.registry.FCRaidConfigBaseTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

import java.util.Optional;

public class PlayerRaidConfigType extends RaidConfigType {
    public static final PlayerRaidConfigType DEFAULT = new PlayerRaidConfigType(RaidWaveConfig.DEFAULT, RaidStrengthConfig.DEFAULT, "event.minecraft.raid", "event.minecraft.raid.victory", "event.minecraft.raid.defeat", Optional.of(SoundEvents.RAID_HORN), Optional.empty(), Optional.of(SoundEvents.RAID_HORN));
    public static final Codec<PlayerRaidConfigType> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    RaidWaveConfig.CODEC.optionalFieldOf("raid_wave_config", RaidWaveConfig.DEFAULT).forGetter(PlayerRaidConfigType::getWaveRaidConfig),
                    RaidStrengthConfig.CODEC.optionalFieldOf("raid_wave_config", RaidStrengthConfig.DEFAULT).forGetter(PlayerRaidConfigType::getRaidStrengthConfig),
                    Codec.STRING.optionalFieldOf("raid_bar_name", "event.minecraft.raid").forGetter((PlayerRaidConfigType config) -> config.getRaidBarNameComponent().getString()),
                    Codec.STRING.optionalFieldOf("raid_bar_victory", "event.minecraft.raid.victory").forGetter((PlayerRaidConfigType config) -> config.getRaidBarVictoryComponent().getString()),
                    Codec.STRING.optionalFieldOf("raid_bar_defeat", "event.minecraft.raid.defeat").forGetter((PlayerRaidConfigType config) -> config.getRaidBarDefeatComponent().getString()),
                    SoundEvent.CODEC.optionalFieldOf("wave_sound").forGetter(PlayerRaidConfigType::getWaveSoundEvent),
                    SoundEvent.CODEC.optionalFieldOf("victory_sound").forGetter(PlayerRaidConfigType::getVictorySoundEvent),
                    SoundEvent.CODEC.optionalFieldOf("defeat_sound").forGetter(PlayerRaidConfigType::getDefeatSoundEvent)
            ).apply(builder, PlayerRaidConfigType::new));

    private final RaidWaveConfig raidWaveConfig;
    private final RaidStrengthConfig raidStrengthConfig;
    private final Component raidBarNameComponent;
    private final Component raidBarVictoryComponent;
    private final Component raidBarDefeatComponent;
    private final Optional<Holder<SoundEvent>> waveSoundEvent;
    private final Optional<Holder<SoundEvent>> victorySoundEvent;
    private final Optional<Holder<SoundEvent>> defeatSoundEvent;

    public PlayerRaidConfigType(RaidWaveConfig raidWaveConfig, RaidStrengthConfig raidStrengthConfig, String raidBarName, String raidBarVictory, String raidBarDefeat, Optional<Holder<SoundEvent>> waveSoundEvent, Optional<Holder<SoundEvent>> victorySoundEvent, Optional<Holder<SoundEvent>> defeatSoundEvent) {
        this.raidWaveConfig = raidWaveConfig;
        this.raidStrengthConfig = raidStrengthConfig;
        this.raidBarNameComponent = Component.translatable(raidBarName);
        this.raidBarVictoryComponent = raidBarNameComponent.copy().append(" - ").append(Component.translatable(raidBarVictory));
        this.raidBarDefeatComponent = raidBarNameComponent.copy().append(" - ").append(Component.translatable(raidBarDefeat));
        this.waveSoundEvent = waveSoundEvent;
        this.victorySoundEvent = victorySoundEvent;
        this.defeatSoundEvent = defeatSoundEvent;
    }

    public RaidWaveConfig getWaveRaidConfig() {
        return raidWaveConfig;
    }

    public RaidStrengthConfig getRaidStrengthConfig() {
        return raidStrengthConfig;
    }

    public Component getRaidBarNameComponent() {
        return raidBarNameComponent;
    }

    public Component getRaidBarVictoryComponent() {
        return raidBarVictoryComponent;
    }

    public Component getRaidBarDefeatComponent() {
        return raidBarDefeatComponent;
    }

    public Optional<Holder<SoundEvent>> getWaveSoundEvent() {
        return waveSoundEvent;
    }

    public Optional<Holder<SoundEvent>> getVictorySoundEvent() {
        return victorySoundEvent;
    }

    public Optional<Holder<SoundEvent>> getDefeatSoundEvent() {
        return defeatSoundEvent;
    }

    @Override
    public RaidConfig create() {
        return new PlayerRaidConfig(
                this,
                raidWaveConfig,
                raidStrengthConfig,
                raidBarNameComponent,
                raidBarVictoryComponent,
                raidBarDefeatComponent,
                waveSoundEvent,
                victorySoundEvent,
                defeatSoundEvent
        );
    }

    @Override
    public RaidConfigBaseType<? extends RaidConfigType> baseType() {
        return FCRaidConfigBaseTypes.PLAYER.get();
    }
}
