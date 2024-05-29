package com.infamousmisadventures.factioncraft.dominion;

import com.infamousmisadventures.factioncraft.capabilities.ModCapabilities;
import com.infamousmisadventures.factioncraft.level.saveddata.Dominion;
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
            Dominion.getOrCreate(serverLevel).initAreaDominion(serverLevel, new AreaPos(event.getChunk().getPos()));
        }
    }

}
