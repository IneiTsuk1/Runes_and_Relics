package net.IneiTsuki.regen;

import net.IneiTsuki.regen.block.ModBlocks;
import net.IneiTsuki.regen.block.entity.ModBlockEntities;
import net.IneiTsuki.regen.item.ModItems;
import net.IneiTsuki.regen.recipe.ModRecipes;
import net.IneiTsuki.regen.screen.ModScreenHandlers;
import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Regen implements ModInitializer {

    public static final String MOD_ID = "regen";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {

        ModBlocks.registerModBlocks();
        ModItems.registerModItems();
        ModBlockEntities.registerAll();
        ModRecipes.registerRecipes();
        ModScreenHandlers.registerAll();
    }
}
