package com.infamousmisadventures.factioncraft.event;

import com.infamousmisadventures.factioncraft.faction.Faction;
import com.infamousmisadventures.factioncraft.raid.config.raid.RaidConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class CalculateStrengthEvent implements FCEvent {
    RaidConfig config;
    BlockPos blockPos;
    ServerLevel level;
    int originalStrength;
    int strength;

    public CalculateStrengthEvent(RaidConfig config, BlockPos blockPos, ServerLevel level, int originalStrength, int strength) {
        this.config = config;
        this.blockPos = blockPos;
        this.level = level;
        this.originalStrength = originalStrength;
        this.strength = strength;
    }

    public RaidConfig getConfig() {
        return config;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public ServerLevel getLevel() {
        return level;
    }

    public int getOriginalStrength() {
        return originalStrength;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public static class Player extends CalculateStrengthEvent
    {
        ServerPlayer player;

        public Player(RaidConfig config, ServerPlayer player, ServerLevel level, int originalStrength, int strength) {
            super(config, player.blockPosition(), level, originalStrength, strength);
            this.player = player;
        }

        public ServerPlayer getPlayer() {
            return player;
        }
    }

    public static class FactionBattle extends CalculateStrengthEvent
    {
        private final Faction faction1;
        private final Faction faction2;

        public FactionBattle(RaidConfig config, BlockPos blockPos, ServerLevel level, int originalStrength, int strength, Faction faction1, Faction faction2) {
            super(config, blockPos, level, originalStrength, strength);
            this.faction1 = faction1;
            this.faction2 = faction2;
        }

        public Faction getFaction1() {
            return faction1;
        }

        public Faction getFaction2() {
            return faction2;
        }
    }
}
