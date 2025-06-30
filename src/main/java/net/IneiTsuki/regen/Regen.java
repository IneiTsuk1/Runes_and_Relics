package net.IneiTsuki.regen;

import net.IneiTsuki.regen.block.ModBlocks;
import net.IneiTsuki.regen.block.entity.ModBlockEntities;
import net.IneiTsuki.regen.item.ModItems;
import net.IneiTsuki.regen.magic.item.MagicScrollItems;
import net.IneiTsuki.regen.recipe.ModRecipes;
import net.IneiTsuki.regen.screen.ModScreenHandlers;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Regen implements ModInitializer {

    public static final String MOD_ID = "regen";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Regen Mod");

        ModBlocks.registerModBlocks();
        ModItems.registerModItems();
        ModBlockEntities.registerAll();
        ModRecipes.registerRecipes();
        ModScreenHandlers.registerAll();
        MagicScrollItems.registerItems();

        // Add items to creative tab
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(MagicScrollItems::addItemsToItemGroup);

        LOGGER.info("Regen Mod initialized successfully!");
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
