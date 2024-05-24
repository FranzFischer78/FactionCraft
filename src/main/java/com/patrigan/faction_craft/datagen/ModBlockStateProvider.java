package com.infamousmisadventures.factioncraft.datagen;


import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import static com.infamousmisadventures.factioncraft.FactionCraft.MODID;
import static com.infamousmisadventures.factioncraft.registry.ModBlocks.RECONSTRUCT_BLOCK;

public class ModBlockStateProvider extends BlockStateProvider {

    public ModBlockStateProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlock(RECONSTRUCT_BLOCK.get());
    }

}