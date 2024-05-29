package com.infamousmisadventures.factioncraft.level.saveddata;

import com.infamousmisadventures.factioncraft.FCConstants;
import com.infamousmisadventures.factioncraft.faction.Faction;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

import static com.infamousmisadventures.factioncraft.FCConstants.MOD_ID;

public class PlayerFactions extends SavedData {

    public static final Codec<PlayerFactions> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    PlayerFaction.CODEC.listOf().fieldOf("player_factions").forGetter(data -> new ArrayList<>(data.getPlayerFactions().values()))
            ).apply(builder, PlayerFactions::new));

    public static PlayerFactions getOrCreate(ServerLevel level)
    {
        return level.getServer().overworld().getDataStorage().computeIfAbsent((tag) -> loadStatic(tag, level), () -> create(level), MOD_ID + "-player_factions");
    }

    public static PlayerFactions loadStatic(CompoundTag tag, ServerLevel level)
    {
        PlayerFactions playerFactionsData = create(level);
        playerFactionsData.load(tag);
        return playerFactionsData;
    }

    public static PlayerFactions create(ServerLevel level)
    {
        return new PlayerFactions();
    }

    private Map<UUID, PlayerFaction> playerFactions = new HashMap<>();

    public PlayerFactions() {
    }

    public PlayerFactions(List<PlayerFaction> playerFactions) {
        playerFactions.forEach(playerFaction -> this.playerFactions.put(playerFaction.getPlayer(), playerFaction));
    }

    public Map<UUID, PlayerFaction> getPlayerFactions() {
        return playerFactions;
    }

    public PlayerFactions setPlayerFactions(Map<UUID, PlayerFaction> playerFactions) {
        this.playerFactions = playerFactions;
        return this;
    }

    public PlayerFactions setPlayerFactions(List<PlayerFaction> playerFactions) {
        playerFactions.forEach(playerFaction -> this.playerFactions.put(playerFaction.getPlayer(), playerFaction));
        return this;
    }

    public void addPlayerFaction(Player player, Faction faction) {
        this.playerFactions.put(player.getUUID(), new PlayerFaction(player.getUUID(), faction));
    }


    public boolean hasPlayerFaction(Player player){
        return this.playerFactions.containsKey(player.getUUID());
    }

    public CompoundTag save(CompoundTag compoundTag) {
        CODEC.encodeStart(NbtOps.INSTANCE, this).resultOrPartial(FCConstants.LOGGER::error).ifPresent((p_216906_) -> {
            compoundTag.put("PlayerFactions", p_216906_);
        });
        return compoundTag;
    }

    public void load(CompoundTag pCompound) {
        if (pCompound.contains("PlayerFactions", 10)) {
            DataResult<PlayerFactions> dataresult = PlayerFactions.CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, pCompound.get("PlayerFactions")));
            dataresult.resultOrPartial(FCConstants.LOGGER::error).ifPresent(playerFactions -> this.setPlayerFactions(playerFactions.getPlayerFactions()));
        }
    }

    public Faction getPlayerFaction(Player player) {
        return this.playerFactions.get(player.getUUID()).getFaction();
    }
}
