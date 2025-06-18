package net.IneiTsuki.regen.client;

import net.IneiTsuki.regen.screen.ModScreenHandlers;
import net.IneiTsuki.regen.client.screen.SpellInscriberScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class RegenClient implements ClientModInitializer {


    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreenHandlers.SPELL_INSCRIBER, SpellInscriberScreen::new);
    }
}
