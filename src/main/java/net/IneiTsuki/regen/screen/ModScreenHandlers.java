package net.IneiTsuki.regen.screen;

import net.IneiTsuki.regen.screen.SpellInscriber.SpellInscriberScreenHandler;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ModScreenHandlers {
    public static ScreenHandlerType<SpellInscriberScreenHandler> SPELL_INSCRIBER;

    public static void registerAll() {
        SPELL_INSCRIBER = Registry.register(
                Registries.SCREEN_HANDLER,
                Identifier.of("regen", "spell_inscriber"),
                new ScreenHandlerType<>(SpellInscriberScreenHandler::new, FeatureFlags.DEFAULT_ENABLED_FEATURES)
        );
    }
}
