package com.infamousmisadventures.factioncraft.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static com.infamousmisadventures.factioncraft.faction.FactionSpawnHandler.addDominionSpawns;

@Mixin(NaturalSpawner.class)
public class NaturalSpawnerMixin {

    @ModifyReturnValue(method = "mobsAt", at = @At("RETURN"))
    private static WeightedRandomList<MobSpawnSettings.SpawnerData> factioncraft$mobsAt(WeightedRandomList<MobSpawnSettings.SpawnerData> original, ServerLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, MobCategory pCategory, BlockPos pPos, Holder<Biome> pBiome) {
        return addDominionSpawns(pLevel, original, pCategory, pPos);
    }
}
