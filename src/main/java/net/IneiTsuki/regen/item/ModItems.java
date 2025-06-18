package net.IneiTsuki.regen.item;

import net.IneiTsuki.regen.Regen;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item STAFF_TEST = registerItem("staff_test", new Item(new Item.Settings()));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(Regen.MOD_ID, name), item);
    }

    public static void registerModItems() {
        Regen.LOGGER.info("Registering items for " + Regen.MOD_ID);
    }
}
