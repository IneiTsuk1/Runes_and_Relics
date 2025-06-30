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

/**
 * Handles registration of custom blocks and their corresponding items for the mod.
 */
public class ModBlocks {

    /**
     * The Spell Inscriber block instance.
     */
    public static final Block SPELL_INSCRIBER_BLOCK = registerBlock("spell_inscriber",
            new SpellInscriberBlock(AbstractBlock.Settings.create().nonOpaque()));

    /**
     * Registers a block with the given name and returns it.
     * Also registers its corresponding {@link BlockItem}.
     *
     * @param name  The block's registry name (without namespace).
     * @param block The block instance.
     * @return The registered block.
     */
    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, Identifier.of(Regen.MOD_ID, name), block);
    }

    /**
     * Registers the {@link BlockItem} for the given block.
     *
     * @param name  The item name (same as the block name).
     * @param block The block instance to associate with the item.
     */
    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, Identifier.of(Regen.MOD_ID, name),
                new BlockItem(block, new Item.Settings()));
    }

    /**
     * Registers all mod blocks and adds them to the relevant creative tab(s).
     * Call this method during the mod initialization phase.
     */
    public static void registerModBlocks() {
        Regen.LOGGER.info("Registering Mod Blocks for " + Regen.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(entries -> {
            entries.add(ModBlocks.SPELL_INSCRIBER_BLOCK);
        });
    }
}
