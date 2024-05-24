package com.infamousmisadventures.factioncraft.faction.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public enum FactionEntityRank {
    LEADER("leader", 5, null),
    SUPPORT("support", 0, null),
    MOUNT("mount", 0, null),
    DIGGER("digger", 1, null),
    GENERAL("general", 3, LEADER),
    CAPTAIN("captain", 2, GENERAL),
    SOLDIER("soldier", 1, CAPTAIN);

    public static final Codec<FactionEntityRank> CODEC = Codec.STRING.flatComapMap(s -> FactionEntityRank.byName(s, null), d -> DataResult.success(d.getName()));

    private final String name;
    private final int grade;
    private final FactionEntityRank promotion;

    FactionEntityRank(String name, int grade, FactionEntityRank promotion) {
        this.name = name;
        this.grade = grade;
        this.promotion = promotion;
    }

    public static FactionEntityRank byName(String name, FactionEntityRank defaultRank) {
        for (FactionEntityRank factionEntityRank : values()){
            if (factionEntityRank.name.equalsIgnoreCase(name)) {
                return factionEntityRank;
            }
        }

        return defaultRank;
    }

    public FactionEntityRank promote() {
        return promotion;
    }

    public String getName() {
        return name;
    }

    public int getGrade() {
        return grade;
    }
}
