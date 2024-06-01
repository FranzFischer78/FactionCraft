package com.infamousmisadventures.factioncraft.mixins;

import com.infamousmisadventures.factioncraft.config.FactionCraftConfig;
import com.infamousmisadventures.factioncraft.level.saveddata.Dominion;
import com.infamousmisadventures.factioncraft.level.saveddata.FactionRelationsData;
import com.infamousmisadventures.factioncraft.level.saveddata.RaidManager;
import com.infamousmisadventures.factioncraft.level.spawner.BattleSpawner;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

import static net.minecraft.world.level.Level.OVERWORLD;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void factioncraft$init(CallbackInfo ci) {
        ServerLevelAccessor serverLevel = (ServerLevelAccessor) this;
        customSpawners(serverLevel);
    }

    @Inject(method = "Lnet/minecraft/server/level/ServerLevel;tick(Ljava/util/function/BooleanSupplier;)V", at = @At("TAIL"))
    private void factioncraft$tick(BooleanSupplier $$0, CallbackInfo ci) {
        ServerLevel level = (ServerLevel) (Object) this;
        RaidManager.getOrCreate(level).tick();
        Dominion.getOrCreate(level).tick();
        if(level.equals(level.getServer().getLevel(OVERWORLD))) {
            FactionRelationsData.getOrCreate(level).tick();
        }
    }

    private void customSpawners(ServerLevelAccessor serverLevel) {
        List<CustomSpawner> customSpawners = serverLevel.getCustomSpawners();
        List<CustomSpawner> newCustomSpawners = customSpawners.stream()
                .filter(this::filterVanillaPatrols)
                .collect(Collectors.toList());
        newCustomSpawners.add(new com.infamousmisadventures.factioncraft.level.spawner.PatrolSpawner());
        newCustomSpawners.add(new BattleSpawner());
        serverLevel.setCustomSpawners(newCustomSpawners);
    }

    public boolean filterVanillaPatrols(CustomSpawner iSpecialSpawner){
        return !(iSpecialSpawner instanceof PatrolSpawner) || !FactionCraftConfig.DISABLE_VANILLA_PATROLS.get();

    }
}
