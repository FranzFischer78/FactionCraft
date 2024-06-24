package com.infamousmisadventures.factioncraft.raid;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.infamousmisadventures.factioncraft.entity.data.FactionEntityData;
import com.infamousmisadventures.factioncraft.entity.data.MobRaiderData;
import com.infamousmisadventures.factioncraft.entity.data.holder.IFactionEntityDataHolder;
import com.infamousmisadventures.factioncraft.entity.data.holder.IMobRaiderDataHolder;
import com.infamousmisadventures.factioncraft.event.FactionRaidEvent;
import com.infamousmisadventures.factioncraft.faction.EntityWeightMapProperties;
import com.infamousmisadventures.factioncraft.faction.Faction;
import com.infamousmisadventures.factioncraft.faction.FactionGroupSpawner;
import com.infamousmisadventures.factioncraft.faction.entity.FactionEntityRank;
import com.infamousmisadventures.factioncraft.faction.entity.FactionEntityType;
import com.infamousmisadventures.factioncraft.level.saveddata.RaidManager;
import com.infamousmisadventures.factioncraft.platform.Services;
import com.infamousmisadventures.factioncraft.raid.target.RaidConfigHelper;
import com.infamousmisadventures.factioncraft.raid.target.RaidConfig;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.function.Predicate;

import static com.infamousmisadventures.factioncraft.config.FactionCraftConfig.*;
import static com.infamousmisadventures.factioncraft.util.GeneralUtils.getRandomEntry;

public class Raid {
    private final int id;
    private final ServerLevel level;
    private final RaidConfig raidConfig;
    private final ServerBossEvent raidEvent = new ServerBossEvent(Component.literal(""), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);
    private final int numGroups;
    private final Queue<BlockPos> waveSpawnPos = new LinkedList<>();
    private final Map<Integer, Mob> groupToLeaderMap = Maps.newHashMap();
    private final Map<Integer, Set<Mob>> groupRaiderMap = Maps.newHashMap();
    private final Set<UUID> heroesOfTheVillage = Sets.newHashSet();
    private int badOmenLevel;
    private float totalHealth;
    private int groupsSpawned = 0;
    private boolean started;
    private boolean active;
    private Status status;
    private long ticksActive;
    private int raidCooldownTicks;
    private int postRaidTicks;
    private int celebrationTicks;

    public Raid(int uniqueId, ServerLevel level, RaidConfig raidConfig) {
        this.id = uniqueId;
        this.level = level;
        this.raidConfig = raidConfig;
        this.numGroups = this.getNumGroups(level.getDifficulty(), raidConfig);
        this.groupsSpawned = raidConfig.getWaveRaidConfig().getStartingWave();
        this.active = true;
        this.raidEvent.setName(getRaidEventName(raidConfig));
        this.raidEvent.setProgress(0.0F);
        this.status = Status.ONGOING;
    }


    public Raid(ServerLevel level, CompoundTag compoundNBT) {
        this.level = level;
        this.raidConfig = RaidConfigHelper.load(level, compoundNBT.getCompound("RaidTarget"));
        this.raidEvent.setName(getRaidEventName(this.raidConfig));
        this.id = compoundNBT.getInt("Id");
        this.started = compoundNBT.getBoolean("Started");
        this.active = compoundNBT.getBoolean("Active");
        this.ticksActive = compoundNBT.getLong("TicksActive");
        this.badOmenLevel = compoundNBT.getInt("BadOmenLevel");
        this.groupsSpawned = compoundNBT.getInt("GroupsSpawned");
        this.raidCooldownTicks = compoundNBT.getInt("PreRaidTicks");
        this.postRaidTicks = compoundNBT.getInt("PostRaidTicks");
        this.totalHealth = compoundNBT.getFloat("TotalHealth");
        this.numGroups = compoundNBT.getInt("NumGroups");
        this.status = Status.getByName(compoundNBT.getString("Status"));
        this.heroesOfTheVillage.clear();
        if (compoundNBT.contains("HeroesOfTheVillage", 9)) {
            ListTag listnbt = compoundNBT.getList("HeroesOfTheVillage", 11);
            for (int i = 0; i < listnbt.size(); ++i) {
                this.heroesOfTheVillage.add(NbtUtils.loadUUID(listnbt.get(i)));
            }
        }
    }

    public ServerLevel getLevel() {
        return level;
    }

    public RaidConfig getRaidConfig() {
        return raidConfig;
    }

    public int getId() {
        return id;
    }

    public BlockPos getCenter() {
        return this.raidConfig.getTargetBlockPos();
    }

    public void tick() {
        if (!this.isStopped()) {
            if (this.status == Status.ONGOING) {
                boolean flag = this.active;
                this.active = this.level.hasChunkAt(this.raidConfig.getTargetBlockPos());
                if (this.level.getDifficulty() == Difficulty.PEACEFUL) {
                    this.stop();
                    return;
                }
                if (flag != this.active) {
                    this.raidEvent.setVisible(this.active);
                }
                if (!this.active) {
                    return;
                }

                raidConfig.updateTargetBlockPos(level);

                if (raidConfig.isDefeat(this, level)) {
                    if (this.groupsSpawned > 0) {
                        FactionRaidEvent.Defeat event = new FactionRaidEvent.Defeat(this);
                        Services.EVENT_BUS.post(event);
                        this.status = Status.LOSS;
                        this.playSound(raidConfig.getTargetBlockPos(), raidConfig.getDefeatSoundEvent());
                        this.raidEvent.setName(getRaidEventNameDefeat(raidConfig));
                    } else {
                        this.stop();
                    }
                }

                ++this.ticksActive;
                if (this.ticksActive >= 48000L) {
                    this.stop();
                    return;
                }


                int i = this.getTotalRaidersAlive();
                if (i == 0 && this.hasMoreWaves()) {
                    if (raidCooldownTick()) {
                        return;
                    }
                }

                if (this.ticksActive % 20L == 0L) {
                    this.updatePlayers();
                    this.updateRaiders();
                    if (i > 0) {
                        if (i <= 2) {
                            this.raidEvent.setName(getRaidEventName(raidConfig).copy().append(" - ").append(Component.translatable("event.minecraft.raid.raiders_remaining", i)));
                        } else {
                            this.raidEvent.setName(getRaidEventName(raidConfig));
                        }
                    } else {
                        this.raidEvent.setName(getRaidEventName(raidConfig));
                    }
                }

                boolean flag3 = false;
                int k = 0;

                while (this.shouldSpawnGroup()) {
                    for (int j = this.waveSpawnPos.size(); j < getSpawnPosAmount(); j++) {
                        BlockPos randomSpawnPos = this.findRandomSpawnPos(k, 20);
                        if (randomSpawnPos != null) {
                            this.waveSpawnPos.add(randomSpawnPos);
                        }
                    }
                    if (this.waveSpawnPos.size() >= getSpawnPosAmount()) {
                        this.started = true;
                        this.spawnGroup();
                        if (!flag3) {
                            FactionRaidEvent.Wave event = new FactionRaidEvent.Wave(this);
                            Services.EVENT_BUS.post(event);
                            flag3 = true;
                        }
                    } else {
                        ++k;
                    }

                    if (k > 3) {
                        this.stop();
                        break;
                    }
                }

                if (this.isStarted() && !this.hasMoreWaves() && i == 0) {
                    if (this.postRaidTicks < 40) {
                        ++this.postRaidTicks;
                    } else {
                        this.status = Status.VICTORY;
                        FactionRaidEvent.Victory event = new FactionRaidEvent.Victory(this);
                        Services.EVENT_BUS.post(event);
                        this.playSound(raidConfig.getTargetBlockPos(), raidConfig.getVictorySoundEvent());
                        this.raidEvent.setName(getRaidEventNameVictory(raidConfig));

                        for (UUID uuid : this.heroesOfTheVillage) {
                            Entity entity = this.level.getEntity(uuid);
                            if (entity instanceof LivingEntity && !entity.isSpectator()) {
                                LivingEntity livingentity = (LivingEntity) entity;
                                livingentity.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 48000, this.badOmenLevel - 1, false, false, true));
                                if (livingentity instanceof ServerPlayer) {
                                    ServerPlayer serverplayerentity = (ServerPlayer) livingentity;
                                    serverplayerentity.awardStat(Stats.RAID_WIN);
                                    CriteriaTriggers.RAID_WIN.trigger(serverplayerentity);
                                }
                            }
                        }
                    }
                }
            } else if (this.isOver()) {
                ++this.celebrationTicks;
                if (this.celebrationTicks >= 600) {
                    this.stop();
                    return;
                }

                if (this.celebrationTicks % 20 == 0) {
                    this.updatePlayers();
                    this.raidEvent.setVisible(true);
                    if (this.isVictory()) {
                        this.raidEvent.setProgress(0.0F);
                        this.raidEvent.setName(getRaidEventNameVictory(raidConfig));
                    } else {
                        this.raidEvent.setName(getRaidEventNameDefeat(raidConfig));
                    }
                }
            }
        }
    }

    private int getSpawnPosAmount() {
        return 1;
    }

    private void playSound(BlockPos p_221293_1_, Optional<Holder<SoundEvent>> soundEvent) {
        if(soundEvent.isEmpty()) return;
        float f = 13.0F;
        int i = 64;
        Collection<ServerPlayer> collection = this.raidEvent.getPlayers();

        for (ServerPlayer serverplayerentity : this.level.players()) {
            Vec3 vector3d = serverplayerentity.position();
            Vec3 vector3d1 = Vec3.atCenterOf(p_221293_1_);
            double f1 = Math.sqrt((vector3d1.x - vector3d.x) * (vector3d1.x - vector3d.x) + (vector3d1.z - vector3d.z) * (vector3d1.z - vector3d.z));
            double d0 = vector3d.x + (double) (13.0F / f1) * (vector3d1.x - vector3d.x);
            double d1 = vector3d.z + (double) (13.0F / f1) * (vector3d1.z - vector3d.z);
            if (f1 <= 64.0F || collection.contains(serverplayerentity)) {
                serverplayerentity.connection.send(new ClientboundSoundPacket(soundEvent.get(), SoundSource.NEUTRAL, d0, serverplayerentity.getY(), d1, 64.0F, 1.0F, serverplayerentity.getRandom().nextLong()));
            }
        }
    }

    private boolean raidCooldownTick() {
        if (this.raidCooldownTicks <= 0) {
            if (this.raidCooldownTicks == 0 && this.groupsSpawned > 0) {
                this.raidCooldownTicks = 300;
                this.raidEvent.setName(getRaidEventName(raidConfig));
                return true;
            }
        } else {
            boolean flag1 = this.waveSpawnPos.size() >= getSpawnPosAmount();
            boolean flag2 = !flag1 && this.raidCooldownTicks % 5 == 0;
            if (flag1 && !this.level.isPositionEntityTicking(this.waveSpawnPos.peek())) {
                this.waveSpawnPos.poll();
                flag2 = true;
            }

            if (flag2) {
                int j = 0;
                if (this.raidCooldownTicks < 100) {
                    j = 1;
                } else if (this.raidCooldownTicks < 40) {
                    j = 2;
                }

                Optional<BlockPos> validSpawnPos = this.getValidSpawnPos(j);
                if (validSpawnPos.isPresent()) {
                    this.waveSpawnPos.add(validSpawnPos.get());
                }
            }

            if (this.raidCooldownTicks == 300 || this.raidCooldownTicks % 20 == 0) {
                this.updatePlayers();
            }

            --this.raidCooldownTicks;
            this.raidEvent.setProgress(Mth.clamp((float) (300 - this.raidCooldownTicks) / 300.0F, 0.0F, 1.0F));
        }
        return false;
    }

    private boolean shouldSpawnGroup() {
        return this.raidCooldownTicks == 0 && (this.groupsSpawned < this.numGroups) && this.getTotalRaidersAlive() == 0;
    }

    private void spawnGroup() {
        int waveNumber = this.groupsSpawned + 1;
        this.totalHealth = 0.0F;

        double waveMultiplier = BASE_WAVE_MULTIPLIER.get() + (this.groupsSpawned * MULTIPLIER_INCREASE_PER_WAVE.get());
        double spreadMultiplier = ((level.random.nextFloat() * 2) - 1) * WAVE_TARGET_STRENGTH_SPREAD.get();
        double difficultyMultiplier = getDifficultyMultiplier(level.getDifficulty());
        double badOmenMultiplier = MULTIPLIER_INCREASE_PER_BAD_OMEN.get();
        double totalMultiplier = waveMultiplier + spreadMultiplier + difficultyMultiplier + badOmenMultiplier;
        int targetStrength = (int) Math.floor(raidConfig.getTargetStrength() * totalMultiplier);
        Map<Faction, Integer> factionFractions = raidConfig.determineFactionFractions(targetStrength);
        factionFractions.entrySet().forEach(entry -> spawnGroupForFaction(this.waveSpawnPos.poll(), waveNumber, entry.getValue(), entry.getKey()));

        this.waveSpawnPos.clear();
        ++this.groupsSpawned;
        this.updateBossbar();
    }

    private void spawnGroupForFaction(BlockPos spawnBlockPos, int waveNumber, int targetStrength, Faction faction) {
        FactionGroupSpawner factionGroupSpawner = new FactionGroupSpawner(level, spawnBlockPos, waveNumber, targetStrength, raidConfig.getMobsFraction(), faction);
        factionGroupSpawner.spawnGroup();
        factionGroupSpawner.getEntities().forEach(mobEntity -> {
            FactionEntityData factionEntityCapability = ((IFactionEntityDataHolder) mobEntity).getOrCreateFactionEntityData();
            if (factionEntityCapability.getFaction() != null && factionEntityCapability.getFactionEntityType() != null) {
                this.joinRaid(waveNumber, mobEntity);
            }
        });
        this.playSound(spawnBlockPos, raidConfig.getWaveSoundEvent());
    }

    public double getDifficultyMultiplier(Difficulty difficulty) {
        switch (difficulty) {
            case EASY:
                return TARGET_STRENGTH_DIFFICULTY_MULTIPLIER_EASY.get();
            case NORMAL:
                return TARGET_STRENGTH_DIFFICULTY_MULTIPLIER_NORMAL.get();
            case HARD:
                return TARGET_STRENGTH_DIFFICULTY_MULTIPLIER_HARD.get();
            default:
                return 0;
        }
    }

    public void setLeader(int pRaidId, Mob mobEntity) {
        this.groupToLeaderMap.put(pRaidId, mobEntity);
    }

    public void removeLeader(int wave) {
        this.groupToLeaderMap.remove(wave);
    }

    public void joinRaid(int pWave, Mob mobEntity) {
        this.addWaveMob(pWave, mobEntity, true);
        ((IMobRaiderDataHolder) mobEntity).getOrCreateMobRaiderData().addToRaid(pWave, this);
    }

    public void addWaveMob(int wave, Mob mobEntity, boolean fresh) {
        this.groupRaiderMap.computeIfAbsent(wave, p_221323_0_ -> Sets.newHashSet());
        Set<Mob> set = this.groupRaiderMap.get(wave);
        Mob abstractraiderentity = null;

        for (Mob abstractraiderentity1 : set) {
            if (abstractraiderentity1.getUUID().equals(mobEntity.getUUID())) {
                abstractraiderentity = abstractraiderentity1;
                break;
            }
        }

        if (abstractraiderentity != null) {
            set.remove(abstractraiderentity);
        }

        set.add(mobEntity);
        if (fresh) {
            this.totalHealth += mobEntity.getHealth();
        }

        this.updateBossbar();
    }

    public void removeFromRaid(Mob mobEntity, int wave, boolean p_221322_2_) {
        Set<Mob> set = this.groupRaiderMap.get(wave);
        if (set != null) {
            boolean flag = set.remove(mobEntity);
            if (flag) {
                if (p_221322_2_) {
                    this.totalHealth -= mobEntity.getHealth();
                }

               ((IMobRaiderDataHolder) mobEntity).getOrCreateMobRaiderData().setRaid(null);
                this.updateBossbar();
            }
        }
    }

    private void updateRaiders() {
        Iterator<Map.Entry<Integer, Set<Mob>>> iterator = this.groupRaiderMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Set<Mob> set = Sets.newHashSet();
            Map.Entry<Integer, Set<Mob>> waveEntry = iterator.next();

            for (Mob mobEntity : waveEntry.getValue()) {
                BlockPos blockpos = mobEntity.blockPosition();
                if (mobEntity.isAlive() && mobEntity.level().dimension() == this.level.dimension() && !(this.getCenter().distSqr(blockpos) >= 12544.0D)) {
                    if (mobEntity.tickCount > 600) {
                        MobRaiderData raiderCapability = ((IMobRaiderDataHolder) mobEntity).getOrCreateMobRaiderData();
                        if (this.level.getEntity(mobEntity.getUUID()) == null) {
                            set.add(mobEntity);
                        }

                        if (mobEntity.getNoActionTime() > 2400) {
                            raiderCapability.setTicksOutsideRaid(raiderCapability.getTicksOutsideRaid() + 1); //TODO: RaiderCapability: TickOutsideRaid
                        }

                        if (raiderCapability.getTicksOutsideRaid() >= 30) {
                            set.add(mobEntity);
                        }
                    }
                } else {
                    set.add(mobEntity);
                }
            }
            for (Mob abstractraiderentity1 : set) {
                this.removeFromRaid(abstractraiderentity1, waveEntry.getKey(), true);
            }
        }
    }

    public void updateBossbar() {
        this.raidEvent.setProgress(Mth.clamp(this.getHealthOfLivingRaiders() / this.totalHealth, 0.0F, 1.0F));
    }

    public float getHealthOfLivingRaiders() {
        float f = 0.0F;

        for (Set<Mob> set : this.groupRaiderMap.values()) {
            for (Mob mobEntity : set) {
                f += mobEntity.getHealth();
            }
        }

        return f;
    }

    private Optional<BlockPos> getValidSpawnPos(int p_221313_1_) {
        for (int i = 0; i < 3; ++i) {
            BlockPos blockpos = this.findRandomSpawnPos(p_221313_1_, 1);
            if (blockpos != null) {
                return Optional.of(blockpos);
            }
        }

        return Optional.empty();
    }

    private BlockPos findRandomSpawnPos(int outerAttempt, int maxInnerAttempts) {
        int i = 2 - outerAttempt;
        BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos();

        for (int i1 = 0; i1 < maxInnerAttempts; ++i1) {
            float f = this.level.random.nextFloat() * ((float) Math.PI * 2F);
            int j = this.raidConfig.getTargetBlockPos().getX() + Mth.floor(Mth.cos(f) * raidConfig.getSpawnDistance() * (float) i) + this.level.random.nextInt(5);
            int l = this.raidConfig.getTargetBlockPos().getZ() + Mth.floor(Mth.sin(f) * raidConfig.getSpawnDistance() * (float) i) + this.level.random.nextInt(5);
            int k = this.level.getHeight(Heightmap.Types.WORLD_SURFACE, j, l);
            blockpos$mutable.set(j, k, l);
            if (isValidSpawnPos(blockpos$mutable) && raidConfig.isValidSpawnPos(outerAttempt, blockpos$mutable, this.level)) {
                return blockpos$mutable;
            }
        }

        return null;
    }

    private boolean isValidSpawnPos(BlockPos.MutableBlockPos blockpos$mutable) {
        return this.waveSpawnPos.stream().map(existingWaveSpawnPos -> blockpos$mutable.distSqr(existingWaveSpawnPos) > 40).reduce((aBoolean, aBoolean2) -> aBoolean && aBoolean2).orElse(true);
    }

    public int getNumGroups(Difficulty difficulty, RaidConfig raidConfig) {
        int numberOfWaves = 0;
        switch (difficulty) {
            case EASY:
                numberOfWaves = raidConfig.getWaveRaidConfig().getNumberWavesEasy();
                break;
            case NORMAL:
                numberOfWaves = raidConfig.getWaveRaidConfig().getNumberWavesNormal();
                break;
            case HARD:
                numberOfWaves = raidConfig.getWaveRaidConfig().getNumberWavesHard();
                break;
            default:
                numberOfWaves = 0;
        }
        numberOfWaves = numberOfWaves + raidConfig.getAdditionalWaves();
        return Math.min(numberOfWaves, raidConfig.getWaveRaidConfig().getMaxNumberWaves());
    }

    private boolean hasMoreWaves() {
        return !this.isFinalWave();
    }

    private boolean isFinalWave() {
        return this.getGroupsSpawned() >= this.numGroups;
    }

    public int getGroupsSpawned() {
        return groupsSpawned;
    }

    private Predicate<ServerPlayer> validPlayer() {
        return (serverPlayerEntity) -> {
            BlockPos blockpos = serverPlayerEntity.blockPosition();
            RaidManager raidManager = RaidManager.getOrCreate(this.level);
            return serverPlayerEntity.isAlive() && raidManager.getRaidAt(blockpos) == this;
        };
    }

    private void updatePlayers() {
        Set<ServerPlayer> set = Sets.newHashSet(this.raidEvent.getPlayers());
        List<ServerPlayer> list = this.level.getPlayers(this.validPlayer());

        for (ServerPlayer serverplayerentity : list) {
            if (!set.contains(serverplayerentity)) {
                this.raidEvent.addPlayer(serverplayerentity);
            }
        }

        for (ServerPlayer serverplayerentity1 : set) {
            if (!list.contains(serverplayerentity1)) {
                this.raidEvent.removePlayer(serverplayerentity1);
            }
        }
    }

    public int getTotalRaidersAlive() {
        return this.groupRaiderMap.values().stream().mapToInt(Set::size).sum();
    }

    public Set<Mob> getRaidersInWave(int wave) {
        return this.groupRaiderMap.get(wave);
    }

    public void stop() {
        this.active = false;
        this.raidEvent.removeAllPlayers();
        Set<Mob> raidersInWave = getRaidersInWave(getGroupsSpawned());
        if (raidersInWave != null && !this.isLoss()) {
            new HashSet<>(raidersInWave).forEach(LivingEntity::kill);
        }
        this.status = Status.STOPPED;
    }

    public void addHeroOfTheVillage(Entity p_221311_1_) {
        this.heroesOfTheVillage.add(p_221311_1_.getUUID());
    }


    public boolean isBetweenWaves() {
        return this.hasFirstWaveSpawned() && this.getTotalRaidersAlive() == 0 && this.raidCooldownTicks > 0;
    }

    public boolean hasFirstWaveSpawned() {
        return this.groupsSpawned > 0;
    }

    public boolean isStarted() {
        return this.started;
    }

    public boolean isActive() {
        return this.active;
    }

    public boolean isStopped() {
        return this.status == Status.STOPPED;
    }

    public boolean isVictory() {
        return this.status == Status.VICTORY;
    }

    public boolean isLoss() {
        return this.status == Status.LOSS;
    }

    public boolean isOver() {
        return this.isVictory() || this.isLoss();
    }

    private Component getRaidEventName(RaidConfig raidConfig) {
        return this.raidConfig.getRaidBarNameComponent();
    }

    private Component getRaidEventNameDefeat(RaidConfig raidConfig) {
        return this.raidConfig.getRaidBarDefeatComponent();
    }

    private Component getRaidEventNameVictory(RaidConfig raidConfig) {
        return this.raidConfig.getRaidBarVictoryComponent();
    }

    public void endWave() {
        Set<Mob> raidersInWave = getRaidersInWave(getGroupsSpawned());
        if (raidersInWave != null) {
            new HashSet<Mob>(raidersInWave).forEach(LivingEntity::kill);
        }
    }

    public void spawnDigger(Faction faction, BlockPos spawnBlockPos, Mob mob) {
        if(((IFactionEntityDataHolder) mob).getOrCreateFactionEntityData().getFactionEntityType().hasRank(FactionEntityRank.DIGGER)) return;
        this.spawnDigger(faction, spawnBlockPos);
    }

    public void spawnDigger(Faction faction, BlockPos spawnBlockPos) {
        // check how many diggers are already spawned
        if(getDiggersInWave() > Mth.ceil(this.getRaidersInWave(this.getGroupsSpawned()).size() * 0.10)) return;
        EntityWeightMapProperties entityWeightMapProperties = new EntityWeightMapProperties().setAllowedRanks(List.of(FactionEntityRank.DIGGER)).setBlockPos(spawnBlockPos);
        Map<FactionEntityType, Integer> weightMap = faction.getWeightMap(entityWeightMapProperties);
        if (weightMap.isEmpty()) return;
        FactionEntityType randomEntry = getRandomEntry(weightMap, level.random);
        Entity entity = randomEntry.createEntity(level, faction, spawnBlockPos, false, FactionEntityRank.DIGGER, MobSpawnType.PATROL);
        if(entity instanceof Mob mob) {
            this.joinRaid(this.getGroupsSpawned(), mob);
        }
    }

    private long getDiggersInWave() {
        return this.getRaidersInWave(this.getGroupsSpawned()).stream()
                .filter(entity -> ((IFactionEntityDataHolder) entity).getOrCreateFactionEntityData().hasRank(FactionEntityRank.DIGGER)).count();
    }

    public CompoundTag save(CompoundTag pNbt) {
        pNbt.putInt("Id", this.id);
        pNbt.putBoolean("Started", this.started);
        pNbt.putBoolean("Active", this.active);
        pNbt.putLong("TicksActive", this.ticksActive);
        pNbt.putInt("BadOmenLevel", this.badOmenLevel);
        pNbt.putInt("GroupsSpawned", this.groupsSpawned);
        pNbt.putInt("PreRaidTicks", this.raidCooldownTicks);
        pNbt.putInt("PostRaidTicks", this.postRaidTicks);
        pNbt.putFloat("TotalHealth", this.totalHealth);
        pNbt.putInt("NumGroups", this.numGroups);
        pNbt.putString("Status", this.status.getName());

        CompoundTag NewRaidTargetNbt = new CompoundTag();
        raidConfig.saveAdditionalData(NewRaidTargetNbt);
        pNbt.put("NewRaidTarget", NewRaidTargetNbt);

        ListTag listnbt = new ListTag();

        for (UUID uuid : this.heroesOfTheVillage) {
            listnbt.add(NbtUtils.createUUID(uuid));
        }
        pNbt.put("HeroesOfTheVillage", listnbt);
        return pNbt;
    }

    private enum Status {
        ONGOING,
        VICTORY,
        LOSS,
        STOPPED;

        private static final Status[] VALUES = values();

        private static Status getByName(String pName) {
            for (Status raid$status : VALUES) {
                if (pName.equalsIgnoreCase(raid$status.name())) {
                    return raid$status;
                }
            }

            return ONGOING;
        }

        public String getName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }
}
