package com.infamousmisadventures.factioncraft.level.saveddata;

import com.google.common.collect.Maps;
import com.infamousmisadventures.factioncraft.config.FactionCraftConfig;
import com.infamousmisadventures.factioncraft.raid.Raid;
import com.infamousmisadventures.factioncraft.raid.target.RaidConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Iterator;
import java.util.Map;

import static com.infamousmisadventures.factioncraft.FCConstants.MOD_ID;

public class RaidManager extends SavedData {
    private final Map<Integer, Raid> raidMap = Maps.newHashMap();
    private final ServerLevel level;
    private int nextAvailableID = 1;
    private int tick;

    public static RaidManager getOrCreate(ServerLevel level)
    {
        return level.getDataStorage().computeIfAbsent((tag) -> load(tag, level), () -> create(level), MOD_ID + "-raid_manager");
    }

    public static RaidManager load(CompoundTag tag, ServerLevel level)
    {
        RaidManager raidManager = create(level);
        raidManager.load(tag);
        return raidManager;
    }

    public static RaidManager create(ServerLevel level)
    {
        return new RaidManager(level);
    }

    public RaidManager(ServerLevel level) {
        this.level = level;
    }

    public void tick() {
        ++this.tick;
        Iterator<Raid> iterator = this.raidMap.values().iterator();

        while (iterator.hasNext()) {
            Raid raid = iterator.next();
            if (this.level.getGameRules().getBoolean(GameRules.RULE_DISABLE_RAIDS)) {
                raid.stop();
            }

            if (raid.isStopped()) {
                iterator.remove();
            } else {
                raid.tick();
            }
            this.setDirty();
        }
    }

    public static boolean canJoinRaid(Mob pRaider, Raid pRaid) {
        if (pRaider != null && pRaid != null && pRaid.getLevel() != null) {
            return pRaider.isAlive() && pRaider.getNoActionTime() <= 2400 && pRaider.level().dimensionType() == pRaid.getLevel().dimensionType();
        } else {
            return false;
        }
    }

    public Map<Integer, Raid> getRaids() {
        return raidMap;
    }

    private int getUniqueId() {
        return ++this.nextAvailableID;
    }

    public Raid getRaidAt(BlockPos blockPos) {
        return this.getNearbyRaid(blockPos, 9216);
    }

    public Raid getNearbyRaid(BlockPos blockPos, int distance) {
        Raid raid = null;
        double d0 = distance;

        for (Raid raid1 : this.raidMap.values()) {
            double d1 = raid1.getCenter().distSqr(blockPos);
            if (raid1.isActive() && d1 < d0) {
                raid = raid1;
                d0 = d1;
            }
        }

        return raid;
    }

    public Raid createRaid(RaidConfig raidConfig) {
        if (FactionCraftConfig.DISABLE_FACTION_RAIDS.get()) {
            return null;
        } else {
            Raid raid = this.getRaidAt(raidConfig.getTargetBlockPos());
            if (raid == null) {
                raid = new Raid(this.getUniqueId(), this.level, raidConfig);
                if (!this.raidMap.containsKey(raid.getId())) {
                    this.raidMap.put(raid.getId(), raid);
                }
            }
            return raid;
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("NextAvailableID", this.nextAvailableID);
        tag.putInt("Tick", this.tick);
        ListTag listnbt = new ListTag();

        for (Raid raid : this.raidMap.values()) {
            CompoundTag compoundnbt = new CompoundTag();
            raid.save(compoundnbt);
            listnbt.add(compoundnbt);
        }

        tag.put("Raids", listnbt);
        return tag;
    }

    public void load(CompoundTag tag) {
        this.nextAvailableID = tag.getInt("NextAvailableID");
        this.tick = tag.getInt("Tick");
        ListTag listnbt = tag.getList("Raids", 10);

        for (int i = 0; i < listnbt.size(); ++i) {
            CompoundTag compoundnbt = listnbt.getCompound(i);
            Raid raid = new Raid(this.level, compoundnbt);
            this.raidMap.put(raid.getId(), raid);
        }
    }
}
