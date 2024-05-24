package com.infamousmisadventures.factioncraft.registry;

import com.infamousmisadventures.factioncraft.block.ReconstructBlock;
import com.infamousmisadventures.factioncraft.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Supplier;

import static com.infamousmisadventures.factioncraft.util.ResourceLocationHelper.modLoc;

public class FCBlocks {

    public static final Supplier<Block> RECONSTRUCT_BLOCK = registerBlock("reconstruct_block", () ->
            new ReconstructBlock(BlockBehaviour.Properties.of()
                    .forceSolidOff()
                    .noCollission().noOcclusion().noLootTable()));

    public static void register() {
    }

    private static Supplier<Block> registerBlock(String id, Supplier<Block> attribSup) {
        return Services.REGISTRAR.registerObject(modLoc(id), attribSup, BuiltInRegistries.BLOCK);
    }
}
