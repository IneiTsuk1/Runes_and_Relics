package net.IneiTsuki.regen.datagen.model;

import net.IneiTsuki.regen.Regen;
import net.IneiTsuki.regen.block.ModBlocks;
import net.IneiTsuki.regen.item.ModItems;
import net.IneiTsuki.regen.magic.item.MagicScrollItem;
import net.IneiTsuki.regen.magic.item.MagicScrollItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.*;
import net.minecraft.util.Identifier;

/**
 * Data provider responsible for generating block state and item models.
 *
 * <p>This provider is used by the Fabric data generation system to create JSON model files
 * for blocks and items automatically, reducing manual resource file maintenance.
 */
public class ModModelProvider extends FabricModelProvider {

    /**
     * Constructs a new model provider.
     *
     * @param output The data output where generated files will be saved.
     */
    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    /**
     * Generates block state models.
     *
     * <p>Registers the Spell Inscriber block with a model that rotates horizontally
     * to face the player.
     *
     * @param blockStateModelGenerator the generator used to register block models.
     */
    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        blockStateModelGenerator.registerNorthDefaultHorizontalRotation(ModBlocks.SPELL_INSCRIBER_BLOCK);
    }

    /**
     * Generates item models.
     *
     * <p>Registers the staff test item with the handheld model type, and
     * generates layered item models for all magic scrolls using a base texture
     * plus an overlay texture corresponding to the scroll's magic type.
     *
     * @param itemModelGenerator the generator used to register item models.
     */
    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        // Register staff item with handheld model
        itemModelGenerator.register(ModItems.STAFF_TEST, Models.HANDHELD);

        // Register layered models for all magic scrolls
        for (MagicScrollItem scrollItem : MagicScrollItems.getAllScrolls().values()) {
            Identifier baseTexture = Regen.id("item/scroll_base");

            String overlayName = scrollItem.getMagicTypes().isEmpty()
                    ? "default"
                    : scrollItem.getMagicTypes().getFirst().getName();

            Identifier overlayTexture = Regen.id("item/type_overlay_" + overlayName);

            // Use uploadArmor (a public method) to upload layered model textures
            Identifier itemModelId = ModelIds.getItemModelId(scrollItem);
            itemModelGenerator.uploadArmor(itemModelId, baseTexture, overlayTexture);
        }
    }
}
