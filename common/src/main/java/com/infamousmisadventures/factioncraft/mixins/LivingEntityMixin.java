package com.infamousmisadventures.factioncraft.mixins;

import com.infamousmisadventures.factioncraft.entity.data.AppliedBoostsData;
import com.infamousmisadventures.factioncraft.entity.data.FactionEntityData;
import com.infamousmisadventures.factioncraft.entity.data.MobRaiderData;
import com.infamousmisadventures.factioncraft.entity.data.holder.IAppliedBoostsDataHolder;
import com.infamousmisadventures.factioncraft.entity.data.holder.IFactionEntityDataHolder;
import com.infamousmisadventures.factioncraft.entity.data.holder.IMobPatrollerDataHolder;
import com.infamousmisadventures.factioncraft.entity.data.holder.IMobRaiderDataHolder;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static com.infamousmisadventures.factioncraft.faction.Faction.GAIA;
import static net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE;
import static net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_KNOCKBACK;

@Mixin(value = LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements IFactionEntityDataHolder, IAppliedBoostsDataHolder {

    @Shadow public abstract boolean canAttack(LivingEntity pLivingentity, TargetingConditions pCondition);

    protected LivingEntityMixin(EntityType<? extends LivingEntity> $$0, Level $$1) {
        super($$0, $$1);
    }

    @Unique
    private FactionEntityData factionCraft$factionEntityData = null;

    @Unique
    private AppliedBoostsData factionCraft$appliedBoostsData = null;

    public AppliedBoostsData getOrCreateAppliedBoostsData() {
        if (factionCraft$appliedBoostsData == null) {
            factionCraft$appliedBoostsData = new AppliedBoostsData();
        }
        return factionCraft$appliedBoostsData;
    }

    public FactionEntityData getOrCreateFactionEntityData() {
        if (factionCraft$factionEntityData == null) {
            factionCraft$factionEntityData = new FactionEntityData((LivingEntity) (Entity) this);
        }
        return factionCraft$factionEntityData;
    }

    @Inject(method = "createLivingAttributes", at = @At("RETURN"))
    private static void factionCraft$addModdedAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.getReturnValue().add(ATTACK_DAMAGE, 0);
        cir.getReturnValue().add(ATTACK_KNOCKBACK, 0);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void addAdditionalSaveData(CompoundTag nbt, CallbackInfo ci) {
        nbt.put("FactionEntityData", this.getOrCreateFactionEntityData().serializeNBT());
        nbt.put("AppliedBoostsData", this.getOrCreateAppliedBoostsData().serializeNBT());
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void readAdditionalSaveData(CompoundTag nbt, CallbackInfo ci) {
        getOrCreateFactionEntityData().deserializeNBT(nbt.getCompound("FactionEntityData"));
        getOrCreateAppliedBoostsData().deserializeNBT(nbt.getCompound("AppliedBoostsData"));
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo callbackInfo) {
        getOrCreateFactionEntityData().tick();
    }

    @Inject(method = "die", at = @At("HEAD"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void die(DamageSource damageSource, CallbackInfo callbackInfo) {
        getOrCreateFactionEntityData().die();
        if(((Object) this) instanceof Mob mob){
            ((IMobRaiderDataHolder) mob).getOrCreateMobRaiderData().onEntityDie(damageSource);
            ((IMobPatrollerDataHolder) mob).getOrCreateMobPatrollerData().onEntityDie();
        }
    }

    @Inject(method = "Lnet/minecraft/world/entity/LivingEntity;actuallyHurt(Lnet/minecraft/world/damagesource/DamageSource;F)V", at = @At("TAIL"))
    public void factionCraft$actuallyHurt(DamageSource damageSource, float amount, CallbackInfo callbackInfo) {
        if (!this.level().isClientSide() && ((Object) this) instanceof Mob mob) {
            MobRaiderData cap = ((IMobRaiderDataHolder) mob).getOrCreateMobRaiderData();
            if (cap.hasActiveRaid()) {
                cap.getRaid().updateBossbar();
            }
        }
    }

    @ModifyReturnValue(method = "Lnet/minecraft/world/entity/LivingEntity;canAttack(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("RETURN"))
    public boolean factionCraft$canAttack(boolean returnValue, LivingEntity $$0) {
        return returnValue && canAttackSub($$0);
    }

    private boolean canAttackSub(LivingEntity target){
        if (!this.level().isClientSide() && target != null) {
            FactionEntityData sourceCap = ((IFactionEntityDataHolder) target).getOrCreateFactionEntityData();
            FactionEntityData targetCap = this.getOrCreateFactionEntityData();
            return sameFaction(targetCap, sourceCap) || sourceCap.getFaction().isAllyOf(targetCap.getFaction());
        }
        return true;
    }

    private static boolean sameFaction(FactionEntityData targetCap, FactionEntityData sourceCap) {
        return !GAIA.equals(targetCap.getFaction()) && targetCap.getFaction() == sourceCap.getFaction();
    }

}
