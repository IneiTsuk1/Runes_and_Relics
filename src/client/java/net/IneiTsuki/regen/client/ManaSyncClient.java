package net.IneiTsuki.regen.client;

import net.IneiTsuki.regen.magic.network.ManaSyncPacket;
import net.IneiTsuki.regen.magic.components.ManaComponent;
import net.IneiTsuki.regen.magic.components.ModComponents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

public class ManaSyncClient {
    public static void registerClientReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(ManaSyncPacket.ID, (packet, context) -> context.client().execute(() -> {
            PlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null) {
                ManaComponent mana = ModComponents.MANA.get(player);
                mana.setMana(packet.mana());
                mana.setMaxMana(packet.maxMana());
            }
        }));
    }
}
