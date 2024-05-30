package com.infamousmisadventures.factioncraft.mixins;

import com.infamousmisadventures.factioncraft.entity.data.AppliedBoostsData;
import com.infamousmisadventures.factioncraft.entity.data.FactionEntityData;
import com.infamousmisadventures.factioncraft.entity.data.holder.IAppliedBoostsDataHolder;
import com.infamousmisadventures.factioncraft.entity.data.holder.IFactionEntityDataHolder;
import com.infamousmisadventures.factioncraft.entity.data.holder.IMobPatrollerDataHolder;
import com.infamousmisadventures.factioncraft.entity.data.holder.IMobRaiderDataHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE;
import static net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_KNOCKBACK;

@Mixin(value = LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements IFactionEntityDataHolder, IAppliedBoostsDataHolder {

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
    private static void elementary$addModdedAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
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
        factionCraft$factionEntityData.tick();
    }

    @Inject(method = "die", at = @At("HEAD"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void die(DamageSource damageSource, CallbackInfo callbackInfo) {
        factionCraft$factionEntityData.die();
        if(((Object) this) instanceof Mob mob){
            ((IMobRaiderDataHolder) mob).getOrCreateMobRaiderData().onEntityDie(damageSource);
            ((IMobPatrollerDataHolder) mob).getOrCreateMobPatrollerData().onEntityDie();
        }
    }


}
