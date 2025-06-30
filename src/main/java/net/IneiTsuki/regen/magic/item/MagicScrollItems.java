package net.IneiTsuki.regen.magic.item;

import net.IneiTsuki.regen.Regen;
import net.IneiTsuki.regen.magic.MagicEnums.Clarification;
import net.IneiTsuki.regen.magic.MagicEnums.MagicType;
import net.IneiTsuki.regen.magic.MagicScrollEffects;
import net.IneiTsuki.regen.magic.interfaces.MagicEffect;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MagicScrollItems {

    public static final Map<String, MagicScrollItem> MAGIC_SCROLLS = new HashMap<>();

    public static void registerItems() {
        // Register basic 1x1 scrolls
        for (Clarification clarification : Clarification.values()) {
            for (MagicType magicType : MagicType.values()) {

                List<Clarification> clarifications = List.of(clarification);
                List<MagicType> types = List.of(magicType);

                MagicEffect effect = (world, user, cls, tys) -> {
                    user.sendMessage(Text.literal("You cast " +
                            clarification.getFormattedName() + " " +
                            magicType.getFormattedName() + " magic!"), false);
                };

                // Example custom effect
                if (clarification == Clarification.CONTROL && magicType == MagicType.FIRE) {
                    effect = MagicScrollEffects::fireSpell;
                }

                registerMagicScroll(clarifications, types, effect);
            }
        }

        // Example compound scroll
        registerMagicScroll(
                List.of(Clarification.AREA, Clarification.MANY),
                List.of(MagicType.FIRE),
                MagicScrollEffects::fireSpell
        );

        Regen.LOGGER.info("Registered {} magic scrolls", MAGIC_SCROLLS.size());
    }

    private static void registerMagicScroll(List<Clarification> clarifications, List<MagicType> types, MagicEffect effect) {
        String name = "scroll_" +
                clarifications.stream().map(Clarification::getName).collect(Collectors.joining("_")) +
                "_" +
                types.stream().map(MagicType::getName).collect(Collectors.joining("_"));

        MagicScrollItem item = new MagicScrollItem(
                new Item.Settings().maxCount(16),
                clarifications,
                types,
                effect
        );

        Registry.register(Registries.ITEM, Regen.id(name), item);
        MAGIC_SCROLLS.put(name, item);
    }

    public static void addItemsToItemGroup(FabricItemGroupEntries entries) {
        for (MagicScrollItem scroll : MAGIC_SCROLLS.values()) {
            entries.add(scroll);
        }
    }

    public static MagicScrollItem getScroll(List<Clarification> clarifications, List<MagicType> types) {
        String name = "scroll_" +
                clarifications.stream().map(Clarification::getName).collect(Collectors.joining("_")) +
                "_" +
                types.stream().map(MagicType::getName).collect(Collectors.joining("_"));
        return MAGIC_SCROLLS.get(name);
    }
}
