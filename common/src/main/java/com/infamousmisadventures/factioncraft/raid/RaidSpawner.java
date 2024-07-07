package com.infamousmisadventures.factioncraft.raid;

import com.infamousmisadventures.factioncraft.entity.data.FactionEntityData;
import com.infamousmisadventures.factioncraft.entity.data.holder.IFactionEntityDataHolder;
import com.infamousmisadventures.factioncraft.faction.Faction;
import com.infamousmisadventures.factioncraft.faction.FactionGroupSpawner;
import com.infamousmisadventures.factioncraft.raid.config.raid.RaidConfig;
import com.infamousmisadventures.factioncraft.raid.config.wave.WaveConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class RaidSpawner {
    
    private final Raid raid;
    private final Queue<BlockPos> waveSpawnPos = new LinkedList<>();
    private BlockPos lastSpawnPos;
    private WaveConfig waveConfig;

    public RaidSpawner(Raid raid) {
        this.raid = raid;
    }

    private RaidConfig getRaidConfig() {
        return raid.getRaidConfig();
    }

    public BlockPos getLastSpawnPos() {
        return lastSpawnPos;
    }

    void cooldownTick(int raidCooldownTicks) {
        boolean flag1 = waveSpawnPos.size() >= waveConfig.getSpawnPosAmount();
        boolean flag2 = !flag1 && raidCooldownTicks % 5 == 0;
        if (flag1 && !raid.getLevel().isPositionEntityTicking(waveSpawnPos.peek())) {
            waveSpawnPos.poll();
            flag2 = true;
        }

        if (flag2) {
            int urgencyModififer = 0;
            if (raidCooldownTicks < 100) {
                urgencyModififer = 1;
            } else if (raidCooldownTicks < 40) {
                urgencyModififer = 2;
            }

            updateSpawnPosition(urgencyModififer);
        }
    }

    public boolean attemptSpawnGroup(int wave){
        int spawnAttempts = 0;
        while (raid.shouldSpawnGroup()) {
            updateSpawnPositions(spawnAttempts);
            if (this.waveSpawnPos.size() >= waveConfig.getSpawnPosAmount()) {
                this.spawnGroup(wave);
                return true;
            } else {
                ++spawnAttempts;
            }

            if (spawnAttempts > 3) {
                return false;
            }
        }
        return false;
    }

    public void updateSpawnPositions(int urgency) {
        for (int j = waveSpawnPos.size(); j < waveConfig.getSpawnPosAmount(); j++) {
            updateSpawnPosition(urgency);
        }
    }

    private void updateSpawnPosition(int urgency) {
        BlockPos randomSpawnPos = findRandomSpawnPos(urgency, 20);
        if (randomSpawnPos != null) {
            waveSpawnPos.add(randomSpawnPos);
        }
    }

    private BlockPos findRandomSpawnPos(int urgencyModifier, int maxInnerAttempts) {
        int i = 2 - urgencyModifier;
        BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos();

        for (int i1 = 0; i1 < maxInnerAttempts; ++i1) {
            float f = raid.getLevel().random.nextFloat() * ((float) Math.PI * 2F);
            int j = getRaidConfig().getTargetBlockPos().getX() + Mth.floor(Mth.cos(f) * waveConfig.getSpawnDistance() * (float) i) + raid.getLevel().random.nextInt(5);
            int l = getRaidConfig().getTargetBlockPos().getZ() + Mth.floor(Mth.sin(f) * waveConfig.getSpawnDistance() * (float) i) + raid.getLevel().random.nextInt(5);
            int k = raid.getLevel().getHeight(Heightmap.Types.WORLD_SURFACE, j, l);
            blockpos$mutable.set(j, k, l);
            if (isValidSpawnPos(blockpos$mutable) && getRaidConfig().isValidSpawnPos(urgencyModifier, blockpos$mutable, raid.getLevel())) {
                return blockpos$mutable;
            }
        }

        return null;
    }

    private boolean isValidSpawnPos(BlockPos.MutableBlockPos blockpos$mutable) {
        return this.waveSpawnPos.stream().map(existingWaveSpawnPos -> blockpos$mutable.distSqr(existingWaveSpawnPos) > 40).reduce((aBoolean, aBoolean2) -> aBoolean && aBoolean2).orElse(true);
    }

    private void spawnGroup(int waveNumber) {
        int targetStrength = getRaidConfig().getWaveTargetStrength(raid);
        Map<Faction, Integer> factionFractions = waveConfig.determineFactionFractions(targetStrength);
        factionFractions.forEach((key, value) -> spawnGroupForFaction(waveSpawnPos.poll(), waveNumber, value, key));
    }

    private void spawnGroupForFaction(BlockPos spawnBlockPos, int waveNumber, int targetStrength, Faction faction) {
        FactionGroupSpawner factionGroupSpawner = new FactionGroupSpawner(raid.getLevel(), spawnBlockPos, waveNumber, targetStrength, getRaidConfig().getRaidStrengthConfig().getMobsFraction(), faction);
        factionGroupSpawner.spawnGroup();
        factionGroupSpawner.getEntities().forEach(mobEntity -> {
            FactionEntityData factionEntityCapability = ((IFactionEntityDataHolder) mobEntity).getOrCreateFactionEntityData();
            if (factionEntityCapability.getFaction() != null && factionEntityCapability.getFactionEntityType() != null) {
                raid.joinRaid(waveNumber, mobEntity);
            }
        });
        lastSpawnPos = spawnBlockPos;
    }

    public void reset(WaveConfig currentWaveConfig) {
        waveConfig = currentWaveConfig;
        waveSpawnPos.clear();
    }
}
