package com.infamousmisadventures.factioncraft.raid.config.raid;

import com.infamousmisadventures.factioncraft.FCConstants;
import com.infamousmisadventures.factioncraft.entity.data.holder.IFactionEntityDataHolder;
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
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.Blocks;

import java.util.*;
import java.util.stream.Collectors;

import static com.infamousmisadventures.factioncraft.config.FactionCraftConfig.*;

public class FactionBattleConfig implements RaidConfig {

    private final RaidConfigType type;

    private final RaidWaveConfig raidWaveConfig;
    private final Component raidBarNameComponent;
    private final Component raidBarVictoryComponent;
    private final Component raidBarDefeatComponent;
    private final RaidStrengthConfig raidStrengthConfig;
    private final Optional<Holder<SoundEvent>> waveSoundEvent;
    private final Optional<Holder<SoundEvent>> victorySoundEvent;
    private final Optional<Holder<SoundEvent>> defeatSoundEvent;

    private int targetStrength;
    private BlockPos targetBlockPos;
    private Faction faction1;
    private Faction faction2;
    private int startingWave;


    public FactionBattleConfig(RaidConfigType type, RaidWaveConfig raidWaveConfig, String raidBarName, String raidBarVictory, String raidBarDefeat, RaidStrengthConfig raidStrengthConfig, Optional<Holder<SoundEvent>> waveSoundEvent, Optional<Holder<SoundEvent>> victorySoundEvent, Optional<Holder<SoundEvent>> defeatSoundEvent) {
        this.type = type;
        this.raidWaveConfig = raidWaveConfig;
        this.raidBarNameComponent = Component.translatable(raidBarName);
        this.raidBarVictoryComponent = raidBarNameComponent.copy().append(" - ").append(Component.translatable(raidBarVictory));
        this.raidBarDefeatComponent = raidBarNameComponent.copy().append(" - ").append(Component.translatable(raidBarDefeat));
        this.raidStrengthConfig = raidStrengthConfig;
        this.waveSoundEvent = waveSoundEvent;
        this.victorySoundEvent = victorySoundEvent;
        this.defeatSoundEvent = defeatSoundEvent;
    }

    public void init(BlockPos targetBlockPos, Faction faction1, Faction faction2, ServerLevel level) {
        this.targetBlockPos = targetBlockPos;
        this.faction1 = faction1;
        this.faction2 = faction2;
        this.startingWave = getWeightedRandom(BATTLE_STARTING_WAVE_MIN.get(), BATTLE_STARTING_WAVE_MAX.get());
        this.targetStrength = calculateTargetStrength(level, this.startingWave);
    }

    private int calculateTargetStrength(ServerLevel level, int startingWave) {
        int strength = FACTION_BATTLE_RAID_TARGET_BASE_STRENGTH_PER_WAVE.get() * startingWave;
        CalculateStrengthEvent event = new CalculateStrengthEvent.FactionBattle(this, targetBlockPos, level, strength, strength, faction1, faction2);
        Services.EVENT_BUS.post(event);
        FCConstants.LOGGER.info("Strength = " + strength);
        return (int) Math.floor(event.getStrength());
    }

    public void init(int targetStrength, BlockPos targetBlockPos, Faction faction1, Faction faction2, int startingWave) {
        this.targetStrength = targetStrength;
        this.targetBlockPos = targetBlockPos;
        this.faction1 = faction1;
        this.faction2 = faction2;
        this.startingWave = startingWave;
    }

    @Override
    public BlockPos getTargetBlockPos() {
        return this.targetBlockPos;
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
        if (raid.getCurrentWave() <= raidWaveConfig.getStartingWave()) {
            return false;
        }
        Set<Mob> raidersInWave = raid.getRaidersInWave(raid.getCurrentWave());
        if (raidersInWave == null) return true;
        return raidersInWave.stream().map(mobEntity -> ((IFactionEntityDataHolder) mobEntity).getOrCreateFactionEntityData().getFaction()).collect(Collectors.toSet()).size() <= 1;
    }

    @Override
    public boolean isValidSpawnPos(int outerAttempt, BlockPos.MutableBlockPos blockpos$mutable, ServerLevel level) {
        return (blockpos$mutable.distSqr(targetBlockPos) > 30 || outerAttempt >= 2)
                && level.hasChunksAt(blockpos$mutable.getX() - 10, blockpos$mutable.getY() - 10, blockpos$mutable.getZ() - 10, blockpos$mutable.getX() + 10, blockpos$mutable.getY() + 10, blockpos$mutable.getZ() + 10)
                && level.isPositionEntityTicking(blockpos$mutable)
                && (NaturalSpawner.isSpawnPositionOk(SpawnPlacements.Type.ON_GROUND, level, blockpos$mutable, EntityType.RAVAGER)
                || level.getBlockState(blockpos$mutable.below()).is(Blocks.SNOW) && level.getBlockState(blockpos$mutable).isAir());
    }

    @Override
    public RaidWaveConfig getRaidWaveConfig() {
        return raidWaveConfig;
    }

    private int getWeightedRandom(int min, int max) {
        if (max <= min) return min;
        ArrayList<WeightedEntry.Wrapper<Integer>> weightedEntries = new ArrayList<>();
        for (int i = 0; i <= max - min; i++) {
            weightedEntries.add(WeightedEntry.wrap(i + min, max - i));
        }
        Optional<WeightedEntry.Wrapper<Integer>> randomItem = WeightedRandom.getRandomItem(RandomSource.create(), weightedEntries);
        return randomItem.map(WeightedEntry.Wrapper::getData).orElse(min);
    }


    @Override
    public float getSpawnDistance() {
        return 8.0F;
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
    public RaidStrengthConfig getRaidStrengthConfig() {
        return raidStrengthConfig;
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
        int perFactionStrength = (int) Math.floor(targetStrength / 2);
        factionFractions.put(faction1, perFactionStrength);
        factionFractions.put(faction2, perFactionStrength);
        return factionFractions;
    }

    @Override
    public CompoundTag saveAdditionalData(CompoundTag compoundNbt) {
        compoundNbt.putString("Type", FCRaidConfigTypes.getKey(this.type()).toString());
        compoundNbt.putInt("X", targetBlockPos.getX());
        compoundNbt.putInt("Y", targetBlockPos.getY());
        compoundNbt.putInt("Z", targetBlockPos.getZ());
        compoundNbt.putInt("TargetStrength", targetStrength);
        compoundNbt.putString("Faction1", faction1.getName().toString());
        compoundNbt.putString("Faction2", faction2.getName().toString());
        compoundNbt.putInt("StartingWave", startingWave);
        return compoundNbt;
    }

    @Override
    public void loadAdditionalData(ServerLevel level, CompoundTag compoundNBT) {
        init(
                compoundNBT.getInt("TargetStrength"),
                new BlockPos(compoundNBT.getInt("X"), compoundNBT.getInt("Y"), compoundNBT.getInt("Z")),
                FCFactions.getFaction(new ResourceLocation(compoundNBT.getString("Faction1"))),
                FCFactions.getFaction(new ResourceLocation(compoundNBT.getString("Faction2"))),
                compoundNBT.getInt("StartingWave")
        );
    }
}
