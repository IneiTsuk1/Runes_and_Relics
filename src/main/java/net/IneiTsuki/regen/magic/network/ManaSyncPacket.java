package net.IneiTsuki.regen.magic.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public record ManaSyncPacket(int mana, int maxMana) implements CustomPayload {
    public static final Id<ManaSyncPacket> ID = new Id<>(Identifier.of("regen", "mana_sync"));

    // Corrected usage: use getters inside lambda
    public static final PacketCodec<PacketByteBuf, ManaSyncPacket> CODEC = PacketCodec.of(
            (buf, packet) -> {
                // Writing data
                packet.writeVarInt(buf.mana());
                packet.writeVarInt(buf.maxMana());
            },
            buf -> new ManaSyncPacket(
                    // Reading data
                    buf.readVarInt(),
                    buf.readVarInt()
            )
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void sendToClient(ServerPlayerEntity player, int mana, int maxMana) {
        ServerPlayNetworking.send(player, new ManaSyncPacket(mana, maxMana));
    }
}