package com.infamousmisadventures.factioncraft.capabilities.dominion;

import com.infamousmisadventures.factioncraft.capabilities.ModCapabilities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

import static com.infamousmisadventures.factioncraft.FactionCraft.MODID;

@Mod.EventBusSubscriber(modid = MODID)
public class DominionEvents {
    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        LevelAccessor level = event.getLevel();
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            Optional<Dominion> dominionOptional = serverLevel.getCapability(ModCapabilities.DOMINION_CAPABILITY).resolve();
            if (dominionOptional.isPresent()) {
                Dominion dominion = dominionOptional.get();
                dominion.initAreaDominion(serverLevel, new AreaPos(event.getChunk().getPos()));
            }
        }
    }

}
