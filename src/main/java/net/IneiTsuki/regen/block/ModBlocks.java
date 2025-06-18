package net.IneiTsuki.regen.block;

import net.IneiTsuki.regen.Regen;
import net.IneiTsuki.regen.block.custom.SpellInscriberBlock;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlocks {

    public static final Block SPELL_INSCRIBER_BLOCK = registerBlock("spell_inscriber",
            new SpellInscriberBlock(AbstractBlock.Settings.create().nonOpaque()));

    private  static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, Identifier.of(Regen.MOD_ID, name), block);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, Identifier.of(Regen.MOD_ID, name),
                new BlockItem(block, new Item.Settings()));
    }

    public static void registerModBlocks() {
        Regen.LOGGER.info("Registering Mod Blocks for " + Regen.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(fabricItemGroupEntries -> {
            fabricItemGroupEntries.add(ModBlocks.SPELL_INSCRIBER_BLOCK);
        });
    }
}
