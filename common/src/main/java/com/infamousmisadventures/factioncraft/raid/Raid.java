package com.infamousmisadventures.factioncraft.raid;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.infamousmisadventures.factioncraft.entity.data.MobRaiderData;
import com.infamousmisadventures.factioncraft.entity.data.holder.IFactionEntityDataHolder;
import com.infamousmisadventures.factioncraft.entity.data.holder.IMobRaiderDataHolder;
import com.infamousmisadventures.factioncraft.event.FactionRaidEvent;
import com.infamousmisadventures.factioncraft.faction.EntityWeightMapProperties;
import com.infamousmisadventures.factioncraft.faction.Faction;
import com.infamousmisadventures.factioncraft.faction.entity.FactionEntityRank;
import com.infamousmisadventures.factioncraft.faction.entity.FactionEntityType;
import com.infamousmisadventures.factioncraft.level.saveddata.RaidManager;
import com.infamousmisadventures.factioncraft.platform.Services;
import com.infamousmisadventures.factioncraft.raid.config.raid.RaidConfig;
import com.infamousmisadventures.factioncraft.raid.config.raid.RaidConfigHelper;
import com.infamousmisadventures.factioncraft.raid.config.wave.WaveConfig;
import com.infamousmisadventures.factioncraft.raid.config.wave.WaveConfigHelper;
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
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.function.Predicate;

import static com.infamousmisadventures.factioncraft.util.GeneralUtils.getRandomEntry;

public class Raid {
    private final int id;
    private final ServerLevel level;
    private final RaidConfig raidConfig;
    private final ServerBossEvent raidEvent = new ServerBossEvent(Component.literal(""), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);
    private final int numberOfWaves;
    private final Queue<BlockPos> waveSpawnPos = new LinkedList<>();
    private final Map<Integer, Mob> groupToLeaderMap = Maps.newHashMap();
    private final Map<Integer, Set<Mob>> groupRaiderMap = Maps.newHashMap();
    private final Set<UUID> playerParticipants = Sets.newHashSet();
    private int badOmenLevel;
    private float totalHealth;
    private int currentWave = 0;
    private boolean active;
    private Status status;
    private long ticksActive;
    private int raidCooldownTicks;
    private int postRaidTicks;
    private int celebrationTicks;
    private WaveConfig currentWaveConfig;
    private final RaidSpawner raidSpawner;

    public Raid(int uniqueId, ServerLevel level, RaidConfig raidConfig) {
        this.id = uniqueId;
        this.level = level;
        this.raidConfig = raidConfig;
        this.numberOfWaves = raidConfig.getNumberOfWaves(level.getDifficulty());
        this.currentWave = raidConfig.getRaidWaveConfig().getStartingWave();
        this.active = true;
        this.raidEvent.setName(getRaidEventName());
        this.raidEvent.setProgress(0.0F);
        this.status = Status.STARTING;
        this.raidSpawner = new RaidSpawner(this);
        this.currentWaveConfig = raidConfig;
    }


    public Raid(ServerLevel level, CompoundTag compoundNBT) {
        this.level = level;
        this.raidConfig = RaidConfigHelper.load(level, compoundNBT.getCompound("RaidConfig"));
        this.currentWaveConfig = WaveConfigHelper.load(level, this, compoundNBT.getCompound("WaveConfig"));
        this.raidEvent.setName(getRaidEventName());
        this.id = compoundNBT.getInt("Id");
        this.active = compoundNBT.getBoolean("Active");
        this.ticksActive = compoundNBT.getLong("TicksActive");
        this.badOmenLevel = compoundNBT.getInt("BadOmenLevel");
        this.currentWave = compoundNBT.getInt("CurrentWave");
        this.raidCooldownTicks = compoundNBT.getInt("PreRaidTicks");
        this.postRaidTicks = compoundNBT.getInt("PostRaidTicks");
        this.totalHealth = compoundNBT.getFloat("TotalHealth");
        this.numberOfWaves = compoundNBT.getInt("NumberOfWaves");
        this.status = Status.getByName(compoundNBT.getString("Status"));
        this.playerParticipants.clear();
        if (compoundNBT.contains("HeroesOfTheVillage", 9)) {
            ListTag listnbt = compoundNBT.getList("HeroesOfTheVillage", 11);
            for (int i = 0; i < listnbt.size(); ++i) {
                this.playerParticipants.add(NbtUtils.loadUUID(listnbt.get(i)));
            }
        }
        this.raidSpawner = new RaidSpawner(this);
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

    public int getBadOmenLevel() {
        return badOmenLevel;
    }

    public void tick() {
        if (!this.isStopped()) {
            if (this.status.isOngoing()) {
                ongoingRaidTick();
            } else if (this.isOver()) {
                finalizeRaidTick();
            }
        }
    }

    private void ongoingRaidTick() {
        if (!isStillActive()) return;

        raidConfig.updateTargetBlockPos(level);

        if (raidConfig.isDefeat(this, level)) {
            handleDefeat();
        }

        ++this.ticksActive;
        if (isRaidTooOld()) return;


        int i = this.getTotalRaidersAlive();
        if (raidCooldownTick(i)) {
            return;
        }

        if (this.ticksActive % 20L == 0L) {
            this.updatePlayers();
            this.updateRaiders();
            if (i > 0) {
                if (i <= 2) {
                    this.raidEvent.setName(getRaidEventName().copy().append(" - ").append(Component.translatable("event.minecraft.raid.raiders_remaining", i)));
                } else {
                    this.raidEvent.setName(getRaidEventName());
                }
            } else {
                this.raidEvent.setName(getRaidEventName());
            }
        }

        checkShouldStartWave();

        if (this.isStarted() && !this.hasMoreWaves() && i == 0) {
            postRaidTick();
        }
    }

    private void checkShouldStartWave() {
        if(shouldSpawnGroup()) {
            this.totalHealth = 0.0F;
            if (raidSpawner.attemptSpawnGroup(this.currentWave + 1)) {
                startWave();
            } else {
                this.stop();
            }
        }
    }

    private void startWave() {
        this.status = Status.ONGOING;
        FactionRaidEvent.Wave event = new FactionRaidEvent.Wave(this);
        Services.EVENT_BUS.post(event);
        ++this.currentWave;
        this.updateBossbar();
        this.playSound(raidSpawner.getLastSpawnPos(), currentWaveConfig.getWaveSoundEvent());
    }

    private void postRaidTick() {
        if (this.postRaidTicks < 40) {
            ++this.postRaidTicks;
        } else {
            handleVictory();
        }
    }

    private boolean isRaidTooOld() {
        if (this.ticksActive >= 48000L) {
            this.stop();
            return true;
        }
        return false;
    }

    private void handleVictory() {
        this.status = Status.VICTORY;
        FactionRaidEvent.Victory event = new FactionRaidEvent.Victory(this);
        Services.EVENT_BUS.post(event);
        this.playSound(raidConfig.getTargetBlockPos(), raidConfig.getVictorySoundEvent());
        this.raidEvent.setName(getRaidEventNameVictory());

        for (UUID uuid : this.playerParticipants) {
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

    private void handleDefeat() {
        if (this.currentWave > 0) {
            FactionRaidEvent.Defeat event = new FactionRaidEvent.Defeat(this);
            Services.EVENT_BUS.post(event);
            this.status = Status.LOSS;
            this.playSound(raidConfig.getTargetBlockPos(), raidConfig.getDefeatSoundEvent());
            this.raidEvent.setName(getRaidEventNameDefeat());
        } else {
            this.stop();
        }
    }

    private boolean isStillActive() {
        boolean flag = this.active;
        this.active = this.level.hasChunkAt(this.raidConfig.getTargetBlockPos());
        if (this.level.getDifficulty() == Difficulty.PEACEFUL) {
            this.stop();
            return false;
        }
        if (flag != this.active) {
            this.raidEvent.setVisible(this.active);
        }
        return isActive();
    }

    private void finalizeRaidTick() {
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
                this.raidEvent.setName(getRaidEventNameVictory());
            } else {
                this.raidEvent.setName(getRaidEventNameDefeat());
            }
        }
    }

    private void playSound(BlockPos p_221293_1_, Optional<Holder<SoundEvent>> soundEvent) {
        if (soundEvent.isEmpty()) return;
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

    private boolean raidCooldownTick(int totalRaidersAlive) {
        if (totalRaidersAlive != 0 || !this.hasMoreWaves()) return false;
        if (this.raidCooldownTicks <= 0) {
            updateCurrentWaveConfig();
            this.raidCooldownTicks = 300;
            this.raidEvent.setName(getRaidEventName());
            return true;
        } else {
            raidSpawner.cooldownTick(this.raidCooldownTicks);

            if (this.raidCooldownTicks == 300 || this.raidCooldownTicks % 20 == 0) {
                this.updatePlayers();
            }

            --this.raidCooldownTicks;
            this.raidEvent.setProgress(Mth.clamp((float) (300 - this.raidCooldownTicks) / 300.0F, 0.0F, 1.0F));
        }
        return false;
    }

    private void updateCurrentWaveConfig() {
        this.currentWaveConfig = this.raidConfig;
        raidSpawner.reset(currentWaveConfig);
    }

    public boolean shouldSpawnGroup() {
        return this.raidCooldownTicks == 0 && this.hasMoreWaves() && this.getTotalRaidersAlive() == 0;
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

    private boolean hasMoreWaves() {
        return !this.isFinalWave();
    }

    private boolean isFinalWave() {
        return this.getCurrentWave() >= this.numberOfWaves + this.raidConfig.getRaidWaveConfig().getStartingWave();
    }

    public int getCurrentWave() {
        return currentWave;
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
        Set<Mob> raidersInWave = getRaidersInWave(getCurrentWave());
        if (raidersInWave != null && !this.isLoss()) {
            new HashSet<>(raidersInWave).forEach(LivingEntity::kill);
        }
        this.status = Status.STOPPED;
    }

    public void addHeroOfTheVillage(Entity p_221311_1_) {
        this.playerParticipants.add(p_221311_1_.getUUID());
    }


    public boolean isBetweenWaves() {
        return this.hasFirstWaveSpawned() && this.getTotalRaidersAlive() == 0 && this.raidCooldownTicks > 0;
    }

    public boolean hasFirstWaveSpawned() {
        return this.currentWave > 0;
    }

    public boolean isStarted() {
        return this.status != Status.STARTING;
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

    private Component getRaidEventName() {
        return this.currentWaveConfig.getRaidBarNameComponent();
    }

    private Component getRaidEventNameDefeat() {
        return this.raidConfig.getRaidBarDefeatComponent();
    }

    private Component getRaidEventNameVictory() {
        return this.raidConfig.getRaidBarVictoryComponent();
    }

    public void endWave() {
        Set<Mob> raidersInWave = getRaidersInWave(getCurrentWave());
        if (raidersInWave != null) {
            new HashSet<Mob>(raidersInWave).forEach(LivingEntity::kill);
        }
    }

    public void spawnDigger(Faction faction, BlockPos spawnBlockPos, Mob mob) {
        if (((IFactionEntityDataHolder) mob).getOrCreateFactionEntityData().getFactionEntityType().hasRank(FactionEntityRank.DIGGER))
            return;
        this.spawnDigger(faction, spawnBlockPos);
    }

    public void spawnDigger(Faction faction, BlockPos spawnBlockPos) {
        // check how many diggers are already spawned
        if (getDiggersInWave() > Mth.ceil(this.getRaidersInWave(this.getCurrentWave()).size() * 0.10)) return;
        EntityWeightMapProperties entityWeightMapProperties = new EntityWeightMapProperties().setAllowedRanks(List.of(FactionEntityRank.DIGGER)).setBlockPos(spawnBlockPos);
        Map<FactionEntityType, Integer> weightMap = faction.getWeightMap(entityWeightMapProperties);
        if (weightMap.isEmpty()) return;
        FactionEntityType randomEntry = getRandomEntry(weightMap, level.random);
        Entity entity = randomEntry.createEntity(level, faction, spawnBlockPos, false, FactionEntityRank.DIGGER, MobSpawnType.PATROL);
        if (entity instanceof Mob mob) {
            this.joinRaid(this.getCurrentWave(), mob);
        }
    }

    private long getDiggersInWave() {
        return this.getRaidersInWave(this.getCurrentWave()).stream()
                .filter(entity -> ((IFactionEntityDataHolder) entity).getOrCreateFactionEntityData().hasRank(FactionEntityRank.DIGGER)).count();
    }

    public CompoundTag save(CompoundTag pNbt) {
        pNbt.putInt("Id", this.id);
        pNbt.putBoolean("Active", this.active);
        pNbt.putLong("TicksActive", this.ticksActive);
        pNbt.putInt("BadOmenLevel", this.badOmenLevel);
        pNbt.putInt("CurrentWave", this.currentWave);
        pNbt.putInt("PreRaidTicks", this.raidCooldownTicks);
        pNbt.putInt("PostRaidTicks", this.postRaidTicks);
        pNbt.putFloat("TotalHealth", this.totalHealth);
        pNbt.putInt("NumberOfWaves", this.numberOfWaves);
        pNbt.putString("Status", this.status.getName());

        CompoundTag NewRaidTargetNbt = new CompoundTag();
        raidConfig.saveAdditionalData(NewRaidTargetNbt);
        pNbt.put("NewRaidTarget", NewRaidTargetNbt);

        ListTag listnbt = new ListTag();

        for (UUID uuid : this.playerParticipants) {
            listnbt.add(NbtUtils.createUUID(uuid));
        }
        pNbt.put("HeroesOfTheVillage", listnbt);
        return pNbt;
    }

    private enum Status {
        STARTING,
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

        public boolean isOngoing() {
            return this == ONGOING || this == STARTING;
        }
    }
}
