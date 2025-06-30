package net.IneiTsuki.regen.datagen;

import net.IneiTsuki.regen.Regen;
import net.IneiTsuki.regen.block.ModBlocks;
import net.IneiTsuki.regen.item.ModItems;
import net.IneiTsuki.regen.magic.item.MagicScrollItem;
import net.IneiTsuki.regen.magic.item.MagicScrollItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.*;
import net.minecraft.util.Identifier;
import net.minecraft.data.client.Models;

public class ModModelProvider extends FabricModelProvider {

    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        blockStateModelGenerator.registerNorthDefaultHorizontalRotation(ModBlocks.SPELL_INSCRIBER_BLOCK);
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        itemModelGenerator.register(ModItems.STAFF_TEST, Models.HANDHELD);

        for (MagicScrollItem scrollItem : MagicScrollItems.MAGIC_SCROLLS.values()) {
            Identifier baseTexture = Regen.id("item/scroll_base");
            String overlayName = scrollItem.getMagicTypes().isEmpty()
                    ? "default"
                    : scrollItem.getMagicTypes().getFirst().getName();
            Identifier overlayTexture = Regen.id("item/type_overlay_" + overlayName);

            // Use the uploadArmor method for layered textures (it's public and available)
            Identifier itemModelId = ModelIds.getItemModelId(scrollItem);
            itemModelGenerator.uploadArmor(itemModelId, baseTexture, overlayTexture);
        }
    }
}