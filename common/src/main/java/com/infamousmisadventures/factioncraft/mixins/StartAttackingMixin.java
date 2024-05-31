package com.infamousmisadventures.factioncraft.mixins;

import com.infamousmisadventures.factioncraft.util.BrainHelper;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(StartAttacking.class)
public class StartAttackingMixin {

    @ModifyReturnValue(method = "create(Ljava/util/function/Predicate;Ljava/util/function/Function;)Lnet/minecraft/world/entity/ai/behavior/BehaviorControl;", at = @At("RETURN"))
    private static <E extends Mob> BehaviorControl<E> factionCraft$modifyStartAttacking(BehaviorControl<E> original) {
        BrainHelper.startAttackingCache.add(original);
        return original;
    }
}
