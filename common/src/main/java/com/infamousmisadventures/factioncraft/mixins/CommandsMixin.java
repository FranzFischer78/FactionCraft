package com.infamousmisadventures.factioncraft.mixins;

import com.infamousmisadventures.factioncraft.commands.*;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Commands.class)
public class CommandsMixin {

    @Final
    @Shadow
    private CommandDispatcher<CommandSourceStack> dispatcher;

    @Inject(method = "Lnet/minecraft/commands/Commands;<init>(Lnet/minecraft/commands/Commands$CommandSelection;Lnet/minecraft/commands/CommandBuildContext;)V", at = @At("RETURN"))
    private void onRegisterCommands(CallbackInfo ci) {
        FactionRaidCommand.register(dispatcher);
        FactionPatrolCommand.register(dispatcher);
        FactionBattleCommand.register(dispatcher);
        FactionSummonCommand.register(dispatcher);
        DominionCommand.register(dispatcher);
    }
}
