package net.IneiTsuki.regen.magic.handler;

import net.IneiTsuki.regen.magic.effect.active.ActiveSpellTracker;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class SpellTickHandler {

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (!world.isClient) {
                ActiveSpellTracker.tick(world);
            }
        });
    }
}
