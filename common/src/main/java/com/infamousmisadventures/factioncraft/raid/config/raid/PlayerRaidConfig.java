package com.infamousmisadventures.factioncraft.raid.config.raid;

import com.infamousmisadventures.factioncraft.FCConstants;
import com.infamousmisadventures.factioncraft.event.CalculateStrengthEvent;
import com.infamousmisadventures.factioncraft.faction.Faction;
import com.infamousmisadventures.factioncraft.platform.Services;
import com.infamousmisadventures.factioncraft.raid.Raid;
import com.infamousmisadventures.factioncraft.registry.FCFactions;
import com.infamousmisadventures.factioncraft.registry.FCRaidConfigTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.infamousmisadventures.factioncraft.config.FactionCraftConfig.*;

public class PlayerRaidConfig implements RaidConfig {

    private final RaidConfigType type;

    private final RaidWaveConfig raidWaveConfig;
    private final RaidStrengthConfig raidStrengthConfig;
    private final RaidSpawnPosConfig raidSpawnPosConfig;
    private final Component raidBarNameComponent;
    private final Component raidBarVictoryComponent;
    private final Component raidBarDefeatComponent;
    private final Optional<Holder<SoundEvent>> waveSoundEvent;
    private final Optional<Holder<SoundEvent>> victorySoundEvent;
    private final Optional<Holder<SoundEvent>> defeatSoundEvent;

    //AdditionalData
    private Faction faction;
    private ServerPlayer player;
    private int targetStrength;

    public PlayerRaidConfig(RaidConfigType type, RaidWaveConfig raidWaveConfig, RaidStrengthConfig raidStrengthConfig, RaidSpawnPosConfig raidSpawnPosConfig, Component raidBarNameComponent, Component raidBarVictoryComponent, Component raidBarDefeatComponent, Optional<Holder<SoundEvent>> waveSoundEvent, Optional<Holder<SoundEvent>> victorySoundEvent, Optional<Holder<SoundEvent>> defeatSoundEvent) {
        this.type = type;
        this.raidWaveConfig = raidWaveConfig;
        this.raidStrengthConfig = raidStrengthConfig;
        this.raidSpawnPosConfig = raidSpawnPosConfig;
        this.raidBarNameComponent = raidBarNameComponent;
        this.raidBarVictoryComponent = raidBarVictoryComponent;
        this.raidBarDefeatComponent = raidBarDefeatComponent;
        this.waveSoundEvent = waveSoundEvent;
        this.victorySoundEvent = victorySoundEvent;
        this.defeatSoundEvent = defeatSoundEvent;
    }

    public void init(Faction faction, ServerPlayer player, ServerLevel level) {
        init(
                faction,
                player,
                calculateTargetStrength(player, level)
        );
    }

    public void init(Faction faction, ServerPlayer player, int targetStrength) {
        this.faction = this.faction;
        this.player = player;
        this.targetStrength = targetStrength;
    }

    private int calculateTargetStrength(ServerPlayer player, ServerLevel level) {
        int strength = PLAYER_RAID_TARGET_BASE_STRENGTH.get();
        CalculateStrengthEvent event = new CalculateStrengthEvent.Player(this, player, level, strength, strength);
        Services.EVENT_BUS.post(event);
        FCConstants.LOGGER.info("Strength = " + strength);
        return (int) Math.floor(event.getStrength());
    }

    @Override
    public BlockPos getTargetBlockPos() {
        return player.blockPosition();
    }

    @Override
    public void updateTargetBlockPos(ServerLevel level) {
        // noop
    }

    @Override
    public int getTargetStrength() {
        return targetStrength;
    }

    @Override
    public void increaseTargetStrength(int amount) {
        targetStrength += amount;
    }

    @Override
    public int getAdditionalWaves() {
        return (int) Math.floor(VILLAGE_RAID_ADDITIONAL_WAVE_CHANCE.get() * targetStrength);
    }

    @Override
    public boolean isDefeat(Raid raid, ServerLevel level) {
        return !player.isAlive();
    }

    @Override
    public boolean isValidSpawnPos(int outerAttempt, BlockPos.MutableBlockPos blockpos$mutable, ServerLevel level) {
        return (blockpos$mutable.distSqr(player.blockPosition()) > 30 || outerAttempt >= 2)
                && raidSpawnPosConfig.isValidSpawnPos(blockpos$mutable, level);
    }

    @Override
    public RaidWaveConfig getRaidWaveConfig() {
        return raidWaveConfig;
    }

    @Override
    public RaidStrengthConfig getRaidStrengthConfig() {
        return raidStrengthConfig;
    }

    @Override
    public RaidSpawnPosConfig getRaidSpawnPosConfig() {
        return raidSpawnPosConfig;
    }

    @Override
    public float getSpawnDistance() {
        return 32.0F;
    }


    @Override
    public RaidConfigType type() {
        return type;
    }

    @Override
    public Component getRaidBarNameComponent() {
        return raidBarNameComponent;
    }

    @Override
    public Component getRaidBarVictoryComponent() {
        return raidBarVictoryComponent;
    }

    @Override
    public Component getRaidBarDefeatComponent() {
        return raidBarDefeatComponent;
    }

    @Override
    public Optional<Holder<SoundEvent>> getWaveSoundEvent() {
        return waveSoundEvent;
    }

    @Override
    public Optional<Holder<SoundEvent>> getVictorySoundEvent() {
        return victorySoundEvent;
    }

    @Override
    public Optional<Holder<SoundEvent>> getDefeatSoundEvent() {
        return defeatSoundEvent;
    }

    @Override
    public Map<Faction, Integer> determineFactionFractions(int targetStrength) {
        Map<Faction, Integer> factionFractions = new HashMap<>();
        factionFractions.put(faction, targetStrength);
        return factionFractions;
    }

    @Override
    public CompoundTag saveAdditionalData(CompoundTag compoundNbt) {
        compoundNbt.putString("Type", FCRaidConfigTypes.getKey(this.type()).toString());
        compoundNbt.putString("Faction", faction.getName().toString());
        compoundNbt.putString("Player", player.getStringUUID());
        compoundNbt.putInt("TargetStrength", targetStrength);
        return compoundNbt;
    }

    @Override
    public void loadAdditionalData(ServerLevel level, CompoundTag compoundNBT) {
        ResourceLocation factionName = new ResourceLocation(compoundNBT.getString("Faction"));
        if (FCFactions.factionExists(factionName)) {
            Faction loadedFaction = FCFactions.getFaction(factionName);
            init(
                    loadedFaction,
                    level.players().stream().filter(serverPlayerEntity -> serverPlayerEntity.getStringUUID().equals(compoundNBT.getString("Player"))).findFirst().get(),
                    compoundNBT.getInt("TargetStrength")
            );
        }
    }
}
