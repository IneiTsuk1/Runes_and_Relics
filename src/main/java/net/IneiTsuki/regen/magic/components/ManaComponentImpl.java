package net.IneiTsuki.regen.magic.components;

import net.IneiTsuki.regen.magic.core.scheduler.TickScheduler;
import net.IneiTsuki.regen.magic.item.ManaRegenTask;
import net.IneiTsuki.regen.magic.network.ManaSyncPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;

public class ManaComponentImpl implements ManaComponent {
    private int mana = 100;
    private int maxMana = 100;
    private final PlayerEntity player;

    private boolean isRegenerating = false;

    public ManaComponentImpl(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public int getMana() {
        return mana;
    }

    @Override
    public void setMana(int mana) {
        int newMana = Math.max(0, Math.min(mana, maxMana));
        if (this.mana != newMana) {
            this.mana = newMana;
            syncToClient();
        }
    }

    @Override
    public int getMaxMana() {
        return maxMana;
    }

    @Override
    public void setMaxMana(int maxMana) {
        int newMaxMana = Math.max(0, maxMana);
        if (this.maxMana != newMaxMana) {
            this.maxMana = newMaxMana;
            this.mana = Math.min(this.mana, newMaxMana);
            syncToClient();
        }
    }

    @Override
    public void addMana(int amount) {
        setMana(this.mana + amount);
    }

    @Override
    public void consumeMana(int amount) {
        if (hasEnoughMana(amount)) {
            setMana(this.mana - amount);

            // ✅ Start regen loop only if not already running
            if (this.mana < this.maxMana && !isRegenerating) {
                setRegenerating(true);
                TickScheduler.schedule(20, new ManaRegenTask(this, 2, 20));
            }
        }
    }

    @Override
    public boolean hasEnoughMana(int amount) {
        return mana >= amount;
    }

    @Override
    public void syncToClient() {
        if (player.getWorld().isClient) return;
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
        ManaSyncPacket.sendToClient(serverPlayer, mana, maxMana);
    }

    @Override
    public void readFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        this.mana = nbt.getInt("mana");
        this.maxMana = nbt.getInt("maxMana");
        this.mana = Math.min(this.mana, this.maxMana);
    }

    @Override
    public void writeToNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putInt("mana", this.mana);
        nbt.putInt("maxMana", this.maxMana);
    }

    public boolean isRegenerating() {
        return isRegenerating;
    }

    public void setRegenerating(boolean regenerating) {
        this.isRegenerating = regenerating;
    }
}
