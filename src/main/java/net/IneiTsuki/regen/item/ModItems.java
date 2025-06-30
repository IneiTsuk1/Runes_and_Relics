package net.IneiTsuki.regen.item;

import net.IneiTsuki.regen.Regen;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Class responsible for registering mod items.
 *
 * <p>Items are registered statically on class loading.
 */
public class ModItems {

    /** Example test staff item. */
    public static final Item STAFF_TEST = registerItem("staff_test", new Item(new Item.Settings()));

    /**
     * Registers an item with the given name in the Minecraft registry.
     *
     * @param name the registry name of the item
     * @param item the item instance to register
     * @return the registered item
     */
    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(Regen.MOD_ID, name), item);
    }

    /**
     * Called to trigger item registration logging.
     * (Items are actually registered statically.)
     */
    public static void registerModItems() {
        Regen.LOGGER.info("Registering items for " + Regen.MOD_ID);
    }
}
