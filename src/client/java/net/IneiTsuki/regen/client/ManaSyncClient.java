package net.IneiTsuki.regen.client;

import net.IneiTsuki.regen.magic.network.ManaSyncPacket;
import net.IneiTsuki.regen.magic.components.ManaComponent;
import net.IneiTsuki.regen.magic.components.ModComponents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;

public class ManaSyncClient {
    public static void registerClientReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(ManaSyncPacket.ID, (packet, context) -> context.client().execute(() -> {
            PlayerEntity player = context.player();
            if (player != null) {
                ManaComponent mana = ModComponents.MANA.get(player);

                int receivedMaxMana = Math.max(0, packet.maxMana());
                int receivedMana = Math.min(Math.max(0, packet.mana()), receivedMaxMana);

                mana.setMaxMana(receivedMaxMana);
                mana.setMana(receivedMana);
            }
        }));
    }
}
