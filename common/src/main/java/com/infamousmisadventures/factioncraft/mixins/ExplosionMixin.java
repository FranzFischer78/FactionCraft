package com.infamousmisadventures.factioncraft.mixins;

import com.infamousmisadventures.factioncraft.config.FactionCraftConfig;
import com.infamousmisadventures.factioncraft.entity.data.holder.IFactionEntityDataHolder;
import com.infamousmisadventures.factioncraft.faction.Faction;
import com.infamousmisadventures.factioncraft.level.saveddata.RaidManager;
import com.infamousmisadventures.factioncraft.raid.Raid;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Set;

import static com.infamousmisadventures.factioncraft.block.ReconstructBlock.setReconstructBlock;

// Covers BlockEvents onExplosionDetonateEvent
@Mixin(Explosion.class)
public class ExplosionMixin {

    @Shadow
    @Final
    private double x;
    @Shadow
    @Final
    private double y;
    @Shadow
    @Final
    private double z;
    @Shadow
    @Final
    private Level level;
    @Shadow
    @Final
    private DamageSource damageSource;

    @Inject(method = "explode", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/level/Level;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onExplode(CallbackInfo ci, Set $$0, float $$18, int $$19, int $$20, int $$21, int $$22, int $$23, int $$24) {
        if(level instanceof ServerLevel serverLevel && damageSource.getEntity() instanceof LivingEntity livingEntity) {
            RaidManager raidManager = RaidManager.getOrCreate(serverLevel);
            Raid raid = raidManager.getRaidAt(BlockPos.containing(new Vec3(x, y, z)));
            if (canBecomeReconstructBlock(raid, livingEntity)) {
                List<BlockPos> blockPosList = getToBlow();
                blockPosList.forEach(blockPos -> {
                    setReconstructBlock(serverLevel, blockPos, serverLevel.getBlockState(blockPos), raid, livingEntity);
                });
                getToBlow().clear();
            }
        }
    }

    @Shadow
    public List<BlockPos> getToBlow(){return null;}

    private static boolean canBecomeReconstructBlock(Raid raid, LivingEntity livingEntity) {
        if(!FactionCraftConfig.ENABLE_RECONSTRUCT_BLOCKS.get()) {
            return false;
        }
        return (raid != null && !raid.isOver()) || ((FactionCraftConfig.ENABLE_RECONSTRUCT_BLOCKS_OUTSIDE_RAIDS.get()) &&
                checkGaiaFaction(livingEntity) && isPlayer(livingEntity));
    }

    private static boolean isPlayer(LivingEntity livingEntity) {
        return FactionCraftConfig.ENABLE_RECONSTRUCT_BLOCKS_FROM_PLAYERS.get() || !(livingEntity instanceof Player);
    }

    private static boolean checkGaiaFaction(LivingEntity livingEntity) {
        if(livingEntity == null) return FactionCraftConfig.ENABLE_RECONSTRUCT_BLOCKS_FROM_GAIA.get();
        Faction faction = ((IFactionEntityDataHolder)livingEntity).getOrCreateFactionEntityData().getFaction();
        return FactionCraftConfig.ENABLE_RECONSTRUCT_BLOCKS_FROM_GAIA.get() || (faction != null && faction != Faction.GAIA);
    }
}
