package com.infamousmisadventures.factioncraft.mixins;

import com.infamousmisadventures.factioncraft.entity.data.FactionEntityData;
import com.infamousmisadventures.factioncraft.entity.data.MobPatrollerData;
import com.infamousmisadventures.factioncraft.entity.data.holder.IFactionEntityDataHolder;
import com.infamousmisadventures.factioncraft.entity.data.holder.IMobPatrollerDataHolder;
import com.infamousmisadventures.factioncraft.entity.data.holder.IMobRaiderDataHolder;
import com.infamousmisadventures.factioncraft.entity.data.MobRaiderData;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.infamousmisadventures.factioncraft.faction.Faction.GAIA;

@Mixin(value = Mob.class)
public abstract class MobMixin extends LivingEntity implements IMobRaiderDataHolder, IMobPatrollerDataHolder {

    @Shadow
    private LivingEntity target;

    protected MobMixin(EntityType<? extends LivingEntity> $$0, Level $$1) {
        super($$0, $$1);
    }

    @Unique
    private MobRaiderData factionCraft$mobRaiderData = null;
    @Unique
    private MobPatrollerData factionCraft$mobPatrollerData = null;

    public MobRaiderData getOrCreateMobRaiderData() {
        if (factionCraft$mobRaiderData == null) {
            factionCraft$mobRaiderData = new MobRaiderData((Mob) (Entity) this);
        }
        return factionCraft$mobRaiderData;
    }

    public MobPatrollerData getOrCreateMobPatrollerData() {
        if (factionCraft$mobPatrollerData == null) {
            factionCraft$mobPatrollerData = new MobPatrollerData((Mob) (Entity) this);
        }
        return factionCraft$mobPatrollerData;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void addAdditionalSaveData(CompoundTag nbt, CallbackInfo ci) {
        nbt.put("MobRaiderData", this.getOrCreateMobRaiderData().serializeNBT());
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void readAdditionalSaveData(CompoundTag nbt, CallbackInfo ci) {
        getOrCreateMobRaiderData().deserializeNBT(nbt.getCompound("MobRaiderData"));
    }

    @ModifyReturnValue(method = "Lnet/minecraft/world/entity/Mob;requiresCustomPersistence()Z", at = @At("RETURN"))
    public boolean requiresCustomPersistence(boolean returnValue) {
        return returnValue || getOrCreateMobRaiderData().hasActiveRaid() || getOrCreateMobPatrollerData().isPatrolling();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo callbackInfo) {
    }

    @Inject(method = "Lnet/minecraft/world/entity/Mob;setTarget(Lnet/minecraft/world/entity/LivingEntity;)V", at = @At("HEAD"), cancellable = true)
    public void factionCraft$setTarget(LivingEntity livingEntity, CallbackInfo callbackInfo) {
        if (!this.level().isClientSide() && livingEntity != null) {
            FactionEntityData sourceCap = ((IFactionEntityDataHolder) this).getOrCreateFactionEntityData();
            FactionEntityData targetCap = ((IFactionEntityDataHolder) livingEntity).getOrCreateFactionEntityData();
            if (sameFaction(targetCap, sourceCap) || sourceCap.getFaction().isAllyOf(targetCap.getFaction())) {
                callbackInfo.cancel();
            }
        }
    }

    private static boolean sameFaction(FactionEntityData targetCap, FactionEntityData sourceCap) {
        return !GAIA.equals(targetCap.getFaction()) && targetCap.getFaction() == sourceCap.getFaction();
    }
}
